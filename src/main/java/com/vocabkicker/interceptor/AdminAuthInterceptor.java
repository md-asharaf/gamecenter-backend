package com.vocabkicker.interceptor;

import com.vocabkicker.service.AuthService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.lang.NonNull;

import com.vocabkicker.service.JwtService;

@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

  private final JwtService jwtService;
  private final AuthService authService;

  public AdminAuthInterceptor(JwtService jwtService, AuthService authService) {
    this.jwtService = jwtService;
    this.authService = authService;
  }

  @Override
  public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
    if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
      return true;
    }

    if ("POST".equalsIgnoreCase(request.getMethod()) && request.getRequestURI().endsWith("/admins")) {
      if (authService.getAdminCount() == 0) {
        return true;
      }
    }

    String token = extractToken(request);
    Claims claims = jwtService.validateAdminToken(token);
    request.setAttribute("adminId", claims.getSubject());

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
