package com.adnibog.vocabkicker.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.adnibog.vocabkicker.dto.response.UserDto;
import com.adnibog.vocabkicker.entity.User;
import com.adnibog.vocabkicker.exception.BadRequestException;
import com.adnibog.vocabkicker.exception.ConflictException;
import com.adnibog.vocabkicker.exception.NotFoundException;
import com.adnibog.vocabkicker.mapper.UserMapper;
import com.adnibog.vocabkicker.repository.UserRepository;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import com.adnibog.vocabkicker.entity.Role;
import java.util.stream.Collectors;

@Service
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final UserMapper userMapper;

  public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, UserMapper userMapper) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.userMapper = userMapper;
  }

  public long getAdminCount() {
    return userRepository.count();
  }

  public List<UserDto> getAllAdmins() {
    return userRepository.findAll().stream().map(userMapper::toDto).collect(Collectors.toList());
  }

  public UserDto updateAdmin(String id, String email, String password, Set<String> projectIds) {
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

    if (projectIds != null) {
      user.setProjectIds(projectIds);
    }

    user.setUpdatedAt(System.currentTimeMillis());
    userRepository.save(user);

    return userMapper.toDto(user);
  }

  public void deleteAdmin(String currentAdminId, String id) {
    if (currentAdminId.equals(id)) {
      throw new BadRequestException("Cannot delete yourself");
    }
    userRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("Admin not found"));
    userRepository.deleteById(id);
  }

  public void createAdmin(String email, String password, Set<String> projectIds) {
    if (userRepository.findByEmail(email).isPresent()) {
      throw new ConflictException("An admin user with this email already exists");
    }

    final long now = System.currentTimeMillis();
    final User user = User.builder()
        .id(UUID.randomUUID().toString())
        .email(email.toLowerCase())
        .passwordHash(passwordEncoder.encode(password))
        .role(Role.SUB_ADMIN)
        .projectIds(projectIds)
        .createdAt(now)
        .updatedAt(now)
        .build();

    userRepository.save(user);
  }
}
