package com.adnibog.gamecenter.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import com.adnibog.gamecenter.dto.model.UserDto;
import com.adnibog.gamecenter.dto.request.PaginationRequest;
import com.adnibog.gamecenter.dto.request.RegisterAdminRequest;
import com.adnibog.gamecenter.dto.request.UpdateAdminRequest;
import com.adnibog.gamecenter.dto.response.UserPageResponse;
import com.adnibog.gamecenter.repository.pagination.UserPage;

import com.adnibog.gamecenter.entity.User;
import com.adnibog.gamecenter.exception.BadRequestException;
import com.adnibog.gamecenter.exception.ConflictException;
import com.adnibog.gamecenter.exception.NotFoundException;
import com.adnibog.gamecenter.exception.ForbiddenException;
import com.adnibog.gamecenter.mapper.UserMapper;
import com.adnibog.gamecenter.repository.UserRepository;
import com.adnibog.gamecenter.repository.AppStatsRepository;
import java.util.Optional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import com.adnibog.gamecenter.entity.Role;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {

  private final UserRepository userRepository;
  private final AppStatsRepository appStatsRepository;
  private final PasswordEncoder passwordEncoder;
  private final UserMapper userMapper;

  public UserService(UserRepository userRepository, AppStatsRepository appStatsRepository,
      PasswordEncoder passwordEncoder, UserMapper userMapper) {
    this.userRepository = userRepository;
    this.appStatsRepository = appStatsRepository;
    this.passwordEncoder = passwordEncoder;
    this.userMapper = userMapper;
  }

  public User getUserEntityById(String id) {
    return userRepository.findById(id).orElseThrow(() -> new NotFoundException("Sub-admin not found."));
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

  public UserPageResponse getAllAdmins(String currentAdminId, PaginationRequest pageReq) {
    UserPage page = userRepository.findUsers(pageReq);
    List<UserDto> dtos = page.getItems().stream()
        .filter(user -> !user.getId().equals(currentAdminId))
        .map(userMapper::toDto)
        .collect(Collectors.toList());
    return new UserPageResponse(dtos, page.getLastEvaluatedKey());
  }

  public int getTotalAdminCount() {
    return (int) appStatsRepository.getTotalAdmins();
  }

  public UserDto updateAdmin(String currentAdminId, String id, UpdateAdminRequest req) {
    if (currentAdminId.equals(id)) {
      throw new BadRequestException("Use the /me endpoint to update your own profile");
    }
    User user = userRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("Sub-admin not found."));
    if (user.getRole() == Role.SUPER_ADMIN) {
      log.warn("Admin {} attempted to edit Super Admin {}", currentAdminId, id);
      throw new ForbiddenException(
          "Modification of another Super Admin is prohibited.");
    }

    if (req.getEmail() != null && !req.getEmail().isBlank()) {
      String newEmail = req.getEmail().toLowerCase();
      if (!newEmail.equals(user.getEmail())) {
        if (userRepository.findByEmail(newEmail).isPresent()) {
          throw new ConflictException("Email is already in use.");
        }
        user.setEmail(newEmail);
      }
    }

    if (req.getPassword() != null && !req.getPassword().isBlank()) {
      user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
    }

    if (req.getRole() != null) {
      user.setRole(req.getRole());
    }

    if (user.getRole() == Role.SUPER_ADMIN) {
      user.setProjectIds(null);
    } else if (req.getProjectIds() != null) {
      user.setProjectIds(req.getProjectIds().isEmpty() ? null : req.getProjectIds());
    }

    user.setUpdatedAt(System.currentTimeMillis());
    userRepository.save(user);

    return userMapper.toDto(user);
  }

  public void updatePassword(String id, String newPassword) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("Sub-admin not found."));
    user.setPasswordHash(passwordEncoder.encode(newPassword));
    user.setUpdatedAt(System.currentTimeMillis());
    userRepository.save(user);
  }

  public void deleteAdmin(String currentAdminId, String id) {
    if (currentAdminId.equals(id)) {
      throw new BadRequestException("Self-deletion is not permitted.");
    }
    User user = userRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("Sub-admin not found."));
    if (user.getRole() == Role.SUPER_ADMIN) {
      log.warn("Admin {} attempted to delete Super Admin {}", currentAdminId, id);
      throw new ForbiddenException("Deletion of another Super Admin is prohibited.");
    }
    userRepository.deleteById(id);
    appStatsRepository.decrementTotalAdmins();
  }

  public void createAdmin(RegisterAdminRequest req) {
    if (userRepository.findByEmail(req.getEmail()).isPresent()) {
      throw new ConflictException("Email is already in use.");
    }

    Role assignedRole = req.getRole() != null ? req.getRole() : Role.SUB_ADMIN;
    Set<String> assignedProjectIds = assignedRole == Role.SUPER_ADMIN ? null
        : (req.getProjectIds() != null && req.getProjectIds().isEmpty() ? null : req.getProjectIds());

    final long now = System.currentTimeMillis();
    final User user = User.builder()
        .id(UUID.randomUUID().toString())
        .email(req.getEmail().toLowerCase())
        .passwordHash(passwordEncoder.encode(req.getPassword()))
        .role(assignedRole)
        .projectIds(assignedProjectIds)
        .createdAt(now)
        .updatedAt(now)
        .build();

    userRepository.save(user);
    appStatsRepository.incrementTotalAdmins();
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
