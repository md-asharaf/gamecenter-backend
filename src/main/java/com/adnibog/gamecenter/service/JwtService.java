package com.adnibog.gamecenter.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.adnibog.gamecenter.exception.UnauthorizedException;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

  private final long accessExpiration;
  private final long refreshExpiration;
  private final SecretKey cachedSecretKey;

  public JwtService(
      @Value("${jwt.secret}") String jwtSecret,
      @Value("${jwt.access.expiration}") long accessExpiration,
      @Value("${jwt.refresh.expiration}") long refreshExpiration) {
    this.accessExpiration = accessExpiration;
    this.refreshExpiration = refreshExpiration;
    this.cachedSecretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
  }

  public SecretKey getSecretKey() {
    return cachedSecretKey;
  }

  public long getRefreshExpiration() {
    return refreshExpiration;
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
      throw new UnauthorizedException("Unauthorized: Invalid token");
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
    return Jwts.builder()
        .subject(userId)
        .claim("type", "refresh")
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + refreshExpiration))
        .signWith(getSecretKey())
        .compact();
  }
}
