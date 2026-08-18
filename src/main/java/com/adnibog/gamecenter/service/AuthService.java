package com.adnibog.gamecenter.service;

import io.jsonwebtoken.Claims;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.adnibog.gamecenter.dto.response.AuthResult;
import com.adnibog.gamecenter.entity.User;
import com.adnibog.gamecenter.exception.BadRequestException;
import com.adnibog.gamecenter.exception.NotFoundException;
import com.adnibog.gamecenter.exception.UnauthorizedException;
import com.adnibog.gamecenter.repository.UserRepository;

import java.util.Date;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  public AuthService(UserRepository userRepository, JwtService jwtService, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.jwtService = jwtService;
    this.passwordEncoder = passwordEncoder;
  }

  public AuthResult login(String email, String password) {
    Optional<User> userOpt = userRepository.findByEmail(email);
    if (userOpt.isEmpty()) {
      log.warn("Login failed: User not found for email {}", email);
      throw new UnauthorizedException("Invalid credentials");
    }

    User user = userOpt.get();
    if (!passwordEncoder.matches(password, user.getPasswordHash())) {
      log.warn("Login failed: Invalid password for email {}", email);
      throw new UnauthorizedException("Invalid credentials");
    }

    log.info("User {} logged in successfully", email);
    return generateTokensAndSave(user);
  }

  public AuthResult refresh(String refreshToken) {
    if (refreshToken == null || refreshToken.isEmpty()) {
      throw new BadRequestException("Refresh token is required");
    }

    Claims claims;
    try {
      claims = jwtService.parseToken(refreshToken);
    } catch (Exception e) {
      throw new UnauthorizedException("Invalid refresh token");
    }

    if (!"refresh".equals(claims.get("type"))) {
      throw new UnauthorizedException("Invalid token type");
    }

    if (claims.getExpiration().before(new Date())) {
      throw new UnauthorizedException("Refresh token expired");
    }

    String userId = claims.getSubject();
    Optional<User> userOpt = userRepository.findById(userId);

    if (userOpt.isEmpty()) {
      log.warn("Token refresh failed: User {} not found", userId);
      throw new NotFoundException("User not found");
    }

    User user = userOpt.get();
    if (user.getRefreshTokenHash() == null
        || !passwordEncoder.matches(refreshToken, user.getRefreshTokenHash())) {
      log.warn("Token refresh failed: Invalid refresh token hash for user {}", userId);
      throw new UnauthorizedException("Invalid refresh token");
    }

    if (user.getRefreshTokenExpiry() != null && user.getRefreshTokenExpiry() < System.currentTimeMillis()) {
      log.warn("Token refresh failed: Refresh token expired in DB for user {}", userId);
      throw new UnauthorizedException("Refresh token expired in db");
    }

    log.info("User {} refreshed tokens successfully", user.getEmail());
    return generateTokensAndSave(user);
  }

  private AuthResult generateTokensAndSave(User user) {
    String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail());
    String refreshToken = jwtService.generateRefreshToken(user.getId());

    long refreshExpiration = jwtService.getRefreshExpiration();

    user.setRefreshTokenHash(passwordEncoder.encode(refreshToken));
    user.setRefreshTokenExpiry(System.currentTimeMillis() + refreshExpiration);
    userRepository.save(user);

    return new AuthResult(accessToken, refreshToken);
  }

}
