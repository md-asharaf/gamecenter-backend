package com.adnibog.vocabkicker.interceptor;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.adnibog.vocabkicker.service.JwtService;
import com.adnibog.vocabkicker.entity.User;
import com.adnibog.vocabkicker.entity.Role;
import com.adnibog.vocabkicker.exception.UnauthorizedException;
import com.adnibog.vocabkicker.repository.UserRepository;

import org.springframework.lang.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

  private final JwtService jwtService;
  private final UserRepository userRepository;

  public AdminAuthInterceptor(JwtService jwtService, UserRepository userRepository) {
    this.jwtService = jwtService;
    this.userRepository = userRepository;
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

    if (request.getRequestURI().startsWith("/admins")) {
      User admin = userRepository.findById(adminId)
          .orElseThrow(() -> {
            log.warn("Admin not found for ID: {}", adminId);
            return new UnauthorizedException("Admin not found");
          });
      if (admin.getRole() != Role.SUPER_ADMIN) {
        log.warn("Access denied for admin {}: Requires SUPER_ADMIN role to access {}", adminId, request.getRequestURI());
        throw new UnauthorizedException("Access denied: Requires Super Admin role");
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
