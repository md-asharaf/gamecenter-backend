package com.vocabkicker.service;

import com.vocabkicker.dto.response.AuthResult;
import com.vocabkicker.dto.response.UserDto;
import com.vocabkicker.entity.User;
import com.vocabkicker.exception.BadRequestException;
import com.vocabkicker.exception.ConflictException;
import com.vocabkicker.exception.NotFoundException;
import com.vocabkicker.exception.UnauthorizedException;
import com.vocabkicker.repository.UserRepository;

import io.jsonwebtoken.Claims;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthService {

  private final UserRepository userRepository;
  private final BCryptPasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  public AuthService(UserRepository userRepository, JwtService jwtService) {
    this.userRepository = userRepository;
    this.jwtService = jwtService;
    this.passwordEncoder = new BCryptPasswordEncoder();
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

  public long getAdminCount() {
    return userRepository.count();
  }

  public List<UserDto> getAllAdmins() {
    return userRepository.findAll().stream().map(user -> UserDto.builder()
        .id(user.getId())
        .email(user.getEmail())
        .createdAt(user.getCreatedAt())
        .updatedAt(user.getUpdatedAt())
        .build()).collect(Collectors.toList());
  }

  public UserDto updateAdmin(String id, String email, String password) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("Admin not found"));

    if (email != null && !email.isBlank()) {
      String newEmail = email.toLowerCase();
      if (!newEmail.equals(user.getEmail())) {
        if (userRepository.findByEmail(newEmail).isPresent()) {
          throw new ConflictException("An admin with this email already exists");
        }
        user.setEmail(newEmail);
      }
    }

    if (password != null && !password.isBlank()) {
      user.setPasswordHash(passwordEncoder.encode(password));
    }

    user.setUpdatedAt(System.currentTimeMillis());
    userRepository.save(user);

    return UserDto.builder()
        .id(user.getId())
        .email(user.getEmail())
        .createdAt(user.getCreatedAt())
        .updatedAt(user.getUpdatedAt())
        .build();
  }

  public void deleteAdmin(String id) {
    userRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("Admin not found"));
    userRepository.deleteById(id);
  }

  public void createAdmin(String email, String password) {
    if (userRepository.findByEmail(email).isPresent()) {
      throw new ConflictException("An admin user with this email already exists.");
    }

    final long now = System.currentTimeMillis();
    final User user = User.builder()
        .id(UUID.randomUUID().toString())
        .email(email.toLowerCase())
        .passwordHash(passwordEncoder.encode(password))
        .createdAt(now)
        .updatedAt(now)
        .build();

    userRepository.save(user);

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

    long refreshExpiration = 1000L * 60 * 60 * 24 * 7;

    user.setRefreshTokenHash(passwordEncoder.encode(refreshToken));
    user.setRefreshTokenExpiry(System.currentTimeMillis() + refreshExpiration);
    userRepository.save(user);

    return new AuthResult(accessToken, refreshToken);
  }
}
