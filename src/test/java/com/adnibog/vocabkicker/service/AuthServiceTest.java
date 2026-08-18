package com.adnibog.vocabkicker.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Date;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.adnibog.vocabkicker.dto.response.AuthResult;
import com.adnibog.vocabkicker.entity.Role;
import com.adnibog.vocabkicker.entity.User;
import com.adnibog.vocabkicker.exception.UnauthorizedException;
import com.adnibog.vocabkicker.repository.UserRepository;

import io.jsonwebtoken.Claims;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private JwtService jwtService;

  private AuthService authService;

  @BeforeEach
  void setUp() {
    authService = new AuthService(userRepository, jwtService, passwordEncoder);
  }

  @Test
  void login_Success() {
    User user = new User();
    user.setId("user1");
    user.setEmail("admin@example.com");
    user.setPasswordHash("encoded_password");
    user.setRole(Role.SUPER_ADMIN);

    when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("password", "encoded_password")).thenReturn(true);
    when(jwtService.generateAccessToken("user1", "admin@example.com")).thenReturn("access_token");
    when(jwtService.generateRefreshToken("user1")).thenReturn("refresh_token");
    when(passwordEncoder.encode("refresh_token")).thenReturn("encoded_refresh_token");

    AuthResult result = authService.login("admin@example.com", "password");

    assertNotNull(result);
    assertEquals("access_token", result.getAccessToken());
    assertEquals("refresh_token", result.getRefreshToken());
    verify(userRepository).save(user);
  }

  @Test
  void login_InvalidPassword() {
    User user = new User();
    user.setEmail("admin@example.com");
    user.setPasswordHash("encoded_password");

    when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("wrong", "encoded_password")).thenReturn(false);

    assertThrows(UnauthorizedException.class, () -> authService.login("admin@example.com", "wrong"));
  }

  @Test
  void refresh_Success() {
    User user = new User();
    user.setId("user1");
    user.setEmail("admin@example.com");
    user.setRefreshTokenHash("encoded_refresh_token");
    user.setRefreshTokenExpiry(System.currentTimeMillis() + 100000);
    user.setRole(Role.SUPER_ADMIN);

    Claims claims = mock(Claims.class);
    when(claims.getSubject()).thenReturn("user1");
    when(claims.get("type")).thenReturn("refresh");
    when(claims.getExpiration()).thenReturn(new Date(System.currentTimeMillis() + 100000));

    when(jwtService.parseToken("valid_refresh_token")).thenReturn(claims);
    when(userRepository.findById("user1")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("valid_refresh_token", "encoded_refresh_token")).thenReturn(true);

    when(jwtService.generateAccessToken("user1", "admin@example.com")).thenReturn("new_access_token");
    when(jwtService.generateRefreshToken("user1")).thenReturn("new_refresh_token");
    when(passwordEncoder.encode("new_refresh_token")).thenReturn("new_encoded_refresh_token");

    AuthResult result = authService.refresh("valid_refresh_token");

    assertNotNull(result);
    assertEquals("new_access_token", result.getAccessToken());
    assertEquals("new_refresh_token", result.getRefreshToken());
  }

  @Test
  void refresh_InvalidToken() {
    when(jwtService.parseToken("invalid")).thenThrow(new RuntimeException("invalid token"));

    assertThrows(UnauthorizedException.class, () -> authService.refresh("invalid"));
  }
}
