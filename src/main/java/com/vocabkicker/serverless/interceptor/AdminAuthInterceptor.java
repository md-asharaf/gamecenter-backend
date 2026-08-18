package com.vocabkicker.serverless.interceptor;

import com.vocabkicker.serverless.service.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

  private final JwtService jwtService;

  public AdminAuthInterceptor(JwtService jwtService) {
    this.jwtService = jwtService;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
      return true;
    }

    // Bypass interceptor for admin registration, letting AdminController handle the logic manually
    if ("POST".equalsIgnoreCase(request.getMethod()) && request.getRequestURI().endsWith("/admins")) {
      return true;
    }

    Map<String, String> headers = new HashMap<>();
    Enumeration<String> headerNames = request.getHeaderNames();
    while (headerNames.hasMoreElements()) {
      String headerName = headerNames.nextElement();
      headers.put(headerName, request.getHeader(headerName));
    }

    Claims claims = jwtService.validateAdminToken(headers);
    request.setAttribute("adminId", claims.getSubject());

    return true;
  }
}
