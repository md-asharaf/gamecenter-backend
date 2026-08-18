package com.vocabkicker.service;

import com.vocabkicker.exception.UnauthorizedException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

  private final String jwtSecret;

  public JwtService(@Value("${JWT_SECRET}") String jwtSecret) {
    this.jwtSecret = jwtSecret;
  }

  public SecretKey getSecretKey() {
    return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
  }

  public Claims validateAdminToken(String token) {
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
