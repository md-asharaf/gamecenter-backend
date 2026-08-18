package com.adnibog.vocabkicker.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.adnibog.vocabkicker.dto.request.CreateProjectRequest;
import com.adnibog.vocabkicker.dto.response.ProjectDto;
import com.adnibog.vocabkicker.entity.Project;
import com.adnibog.vocabkicker.entity.Role;
import com.adnibog.vocabkicker.entity.User;
import com.adnibog.vocabkicker.exception.ForbiddenException;
import com.adnibog.vocabkicker.mapper.ProjectMapper;
import com.adnibog.vocabkicker.repository.ProjectRepository;
import com.adnibog.vocabkicker.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

  @Mock
  private ProjectRepository projectRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private ProjectMapper projectMapper;

  private ProjectService projectService;

  @BeforeEach
  void setUp() {
    projectService = new ProjectService(projectRepository, userRepository, projectMapper);
  }

  @Test
  void createProject_SuperAdmin_Success() {
    User admin = new User();
    admin.setId("admin1");
    admin.setRole(Role.SUPER_ADMIN);

    CreateProjectRequest req = new CreateProjectRequest();
    req.setName("Test Project");

    Project expectedProject = new Project();
    expectedProject.setName("Test Project");
    ProjectDto expectedDto = new ProjectDto();
    expectedDto.setName("Test Project");

    when(userRepository.findById("admin1")).thenReturn(Optional.of(admin));
    when(projectMapper.toDto(any(Project.class))).thenReturn(expectedDto);

    ProjectDto result = projectService.createProject("admin1", req);

    assertNotNull(result);
    assertEquals("Test Project", result.getName());
    verify(projectRepository).save(any(Project.class));
    verify(userRepository).save(admin);
  }

  @Test
  void createProject_SubAdmin_Forbidden() {
    User admin = new User();
    admin.setId("subadmin1");
    admin.setRole(Role.SUB_ADMIN);

    CreateProjectRequest req = new CreateProjectRequest();
    req.setName("Test Project");

    when(userRepository.findById("subadmin1")).thenReturn(Optional.of(admin));

    assertThrows(ForbiddenException.class, () -> projectService.createProject("subadmin1", req));
    verify(projectRepository, never()).save(any(Project.class));
  }
}
