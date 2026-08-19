package com.adnibog.gamecenter.interceptor;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import org.springframework.web.servlet.HandlerInterceptor;

import com.adnibog.gamecenter.entity.Role;
import com.adnibog.gamecenter.exception.ForbiddenException;
import com.adnibog.gamecenter.service.JwtService;
import com.adnibog.gamecenter.service.UserService;

import org.springframework.lang.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

  private final JwtService jwtService;
  private final UserService userService;

  public AdminAuthInterceptor(JwtService jwtService, UserService userService) {
    this.jwtService = jwtService;
    this.userService = userService;
  }

  @Override
  public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
      @NonNull Object handler) {
    if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
      return true;
    }

    String token = extractToken(request);
    Claims claims = jwtService.validateAdminToken(token);
    String adminId = claims.getSubject();
    request.setAttribute("adminId", adminId);

    String uri = request.getRequestURI();
    if (uri.startsWith("/admins") && !uri.startsWith("/admins/me")) {
      var admin = userService.getUserEntityById(adminId);
      if (admin.getRole() != Role.SUPER_ADMIN) {
        log.warn("Access denied for admin {}: Requires SUPER_ADMIN role to access {}", adminId, uri);
        throw new ForbiddenException("Access denied: Requires Super Admin role");
      }
    }

    return true;
  }

  private String extractToken(HttpServletRequest request) {
    String authHeader = request.getHeader("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      return authHeader.substring(7);
    }

    if (request.getCookies() != null) {
      for (Cookie cookie : request.getCookies()) {
        if ("admin_token".equals(cookie.getName())) {
          return cookie.getValue();
        }
      }
    }
    return null;
  }
}
