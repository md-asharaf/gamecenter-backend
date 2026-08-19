package com.adnibog.gamecenter.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.adnibog.gamecenter.dto.response.UserDto;
import com.adnibog.gamecenter.dto.response.UserPageResponse;
import com.adnibog.gamecenter.repository.UserPage;

import com.adnibog.gamecenter.entity.User;
import com.adnibog.gamecenter.exception.BadRequestException;
import com.adnibog.gamecenter.exception.ConflictException;
import com.adnibog.gamecenter.exception.NotFoundException;
import com.adnibog.gamecenter.mapper.UserMapper;
import com.adnibog.gamecenter.repository.UserRepository;
import java.util.Optional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import com.adnibog.gamecenter.entity.Role;
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

  public User getUserEntityById(String id) {
    return userRepository.findById(id).orElseThrow(() -> new NotFoundException("Admin not found"));
  }

  public UserDto getAdminById(String id) {
    return userMapper.toDto(getUserEntityById(id));
  }

  public Optional<User> findByEmail(String email) {
    return userRepository.findByEmail(email);
  }

  public Optional<User> findById(String id) {
    return userRepository.findById(id);
  }

  public void saveUser(User user) {
    userRepository.save(user);
  }

  public UserPageResponse getAllAdmins(int limit, String lastEvaluatedKey, String search) {
    UserPage page = userRepository.findUsers(limit, lastEvaluatedKey, search);
    List<UserDto> dtos = page.getItems().stream()
        .map(userMapper::toDto)
        .collect(Collectors.toList());
    return new UserPageResponse(dtos, page.getLastEvaluatedKey());
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

  public void addProjectToAdmin(String adminId, String projectId) {
    User admin = getUserEntityById(adminId);
    Set<String> projectIds = admin.getProjectIds();
    if (projectIds == null) {
      projectIds = new HashSet<>();
    }
    projectIds.add(projectId);
    admin.setProjectIds(projectIds);
    admin.setUpdatedAt(System.currentTimeMillis());
    userRepository.save(admin);
  }

  public void removeProjectFromAllAdmins(String projectId) {
    userRepository.findAll().forEach(user -> {
      Set<String> projectIds = user.getProjectIds();
      if (projectIds != null && projectIds.remove(projectId)) {
        user.setProjectIds(projectIds);
        user.setUpdatedAt(System.currentTimeMillis());
        userRepository.save(user);
      }
    });
  }
}
