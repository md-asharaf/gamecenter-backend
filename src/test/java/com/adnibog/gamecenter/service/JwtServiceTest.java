package com.adnibog.gamecenter.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.adnibog.gamecenter.exception.UnauthorizedException;

import io.jsonwebtoken.Claims;

class JwtServiceTest {

  private JwtService jwtService;

  @BeforeEach
  void setUp() {
    jwtService = new JwtService("this-is-a-very-secure-secret-key-that-is-long-enough", 3600000, 86400000);
  }

  @Test
  void generateAndParseAccessToken() {
    String token = jwtService.generateAccessToken("user123", "test@test.com");
    assertNotNull(token);

    Claims claims = jwtService.parseToken(token);
    assertEquals("user123", claims.getSubject());
    assertEquals("test@test.com", claims.get("email"));
  }

  @Test
  void generateAndParseRefreshToken() {
    String token = jwtService.generateRefreshToken("user123");
    assertNotNull(token);

    Claims claims = jwtService.parseToken(token);
    assertEquals("user123", claims.getSubject());
    assertEquals("refresh", claims.get("type"));
  }

  @Test
  void validateAdminToken_Valid() {
    String token = jwtService.generateAccessToken("user123", "test@test.com");

    Claims claims = jwtService.validateAdminToken(token);
    assertEquals("user123", claims.getSubject());
  }

  @Test
  void validateAdminToken_InvalidType() {
    String token = jwtService.generateRefreshToken("user123");

    assertThrows(UnauthorizedException.class, () -> jwtService.validateAdminToken(token));
  }

  @Test
  void validateAdminToken_InvalidToken() {
    assertThrows(UnauthorizedException.class, () -> jwtService.validateAdminToken("invalid.token.string"));
  }

  @Test
  void validateAdminToken_NullToken() {
    assertThrows(UnauthorizedException.class, () -> jwtService.validateAdminToken(null));
  }
}
