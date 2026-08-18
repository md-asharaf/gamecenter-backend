package com.adnibog.vocabkicker.service;

import io.jsonwebtoken.Claims;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.adnibog.vocabkicker.dto.response.AuthResult;
import com.adnibog.vocabkicker.entity.User;
import com.adnibog.vocabkicker.exception.BadRequestException;
import com.adnibog.vocabkicker.exception.NotFoundException;
import com.adnibog.vocabkicker.exception.UnauthorizedException;
import com.adnibog.vocabkicker.repository.UserRepository;

import java.util.Date;
import java.util.Optional;

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
      throw new UnauthorizedException("Invalid credentials");
    }

    User user = userOpt.get();
    if (!passwordEncoder.matches(password, user.getPasswordHash())) {
      throw new UnauthorizedException("Invalid credentials");
    }

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
      throw new NotFoundException("User not found");
    }

    User user = userOpt.get();
    if (user.getRefreshTokenHash() == null
        || !passwordEncoder.matches(refreshToken, user.getRefreshTokenHash())) {
      throw new UnauthorizedException("Invalid refresh token");
    }

    if (user.getRefreshTokenExpiry() != null && user.getRefreshTokenExpiry() < System.currentTimeMillis()) {
      throw new UnauthorizedException("Refresh token expired in db");
    }

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
