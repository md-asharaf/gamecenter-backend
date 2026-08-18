package com.vocabkicker.serverless.service;

import com.vocabkicker.serverless.exception.UnauthorizedException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

  private final String jwtSecret;

  public JwtService(@Value("${JWT_SECRET}") String jwtSecret) {
    this.jwtSecret = jwtSecret;
  }

  public SecretKey getSecretKey() {
    return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
  }

  public Claims validateAdminToken(Map<String, String> headers) {
    String token = extractToken(headers);
    if (token == null) {
      throw new UnauthorizedException("Unauthorized: Missing admin_token");
    }

    try {
      Claims claims = Jwts.parser()
          .verifyWith(getSecretKey())
          .build()
          .parseSignedClaims(token)
          .getPayload();

      if (!"access".equals(claims.get("type"))) {
        throw new UnauthorizedException("Unauthorized: Invalid token type");
      }

      if (claims.getExpiration().before(new Date())) {
        throw new UnauthorizedException("Unauthorized: Token expired");
      }
      
      return claims;
    } catch (Exception e) {
      throw new UnauthorizedException("Unauthorized: " + e.getMessage());
    }
  }

  public String extractToken(Map<String, String> headers) {
    if (headers == null)
      return null;

    String authHeader = headers.get("Authorization");
    if (authHeader == null) {
      authHeader = headers.get("authorization");
    }
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      return authHeader.substring(7);
    }

    return extractCookie(headers, "admin_token");
  }

  private String extractCookie(Map<String, String> headers, String cookieName) {
    String cookieHeader = headers.get("cookie");
    if (cookieHeader == null) {
      cookieHeader = headers.get("Cookie");
    }
    if (cookieHeader == null) return null;
    
    for (String part : cookieHeader.split(";")) {
        String trimmed = part.trim();
        if (trimmed.startsWith(cookieName + "=")) {
            return trimmed.substring((cookieName + "=").length());
        }
    }
    return null;
  }

  public Claims parseToken(String token) {
    return Jwts.parser()
        .verifyWith(getSecretKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  public String generateAccessToken(String userId, String email) {
    final long accessExpiration = 1000L * 60 * 15;
    return Jwts.builder()
        .subject(userId)
        .claim("email", email)
        .claim("type", "access")
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + accessExpiration))
        .signWith(getSecretKey())
        .compact();
  }

  public String generateRefreshToken(String userId) {
    final long refreshExpiration = 1000L * 60 * 60 * 24 * 7;
    return Jwts.builder()
        .subject(userId)
        .claim("type", "refresh")
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + refreshExpiration))
        .signWith(getSecretKey())
        .compact();
  }
}
