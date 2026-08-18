package com.adnibog.gamecenter.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.adnibog.gamecenter.dto.request.CreateProjectRequest;
import com.adnibog.gamecenter.dto.request.UpdateProjectRequest;
import com.adnibog.gamecenter.dto.response.ProjectDto;
import com.adnibog.gamecenter.entity.Project;
import com.adnibog.gamecenter.entity.Role;
import com.adnibog.gamecenter.entity.User;
import com.adnibog.gamecenter.exception.ConflictException;
import com.adnibog.gamecenter.exception.ForbiddenException;
import com.adnibog.gamecenter.exception.NotFoundException;
import com.adnibog.gamecenter.mapper.ProjectMapper;
import com.adnibog.gamecenter.repository.ProjectRepository;
import com.adnibog.gamecenter.repository.QuestionRepository;
import com.adnibog.gamecenter.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

  @Mock
  private ProjectRepository projectRepository;

  @Mock
  private QuestionRepository questionRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private ProjectMapper projectMapper;

  private ProjectService projectService;

  @BeforeEach
  void setUp() {
    projectService = new ProjectService(projectRepository, questionRepository, userRepository, projectMapper);
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

  @Test
  void createProject_DuplicateName_Conflict() {
    User admin = new User();
    admin.setId("admin1");
    admin.setRole(Role.SUPER_ADMIN);

    CreateProjectRequest req = new CreateProjectRequest();
    req.setName("Test Project");

    Project existing = new Project();
    existing.setName("Test Project");

    when(userRepository.findById("admin1")).thenReturn(Optional.of(admin));
    when(projectRepository.findByName("Test Project")).thenReturn(Optional.of(existing));

    assertThrows(ConflictException.class, () -> projectService.createProject("admin1", req));
    verify(projectRepository, never()).save(any(Project.class));
  }

  @Test
  void updateProject_DuplicateName_Conflict() {
    Project project = new Project();
    project.setId("proj1");
    project.setName("Old Name");

    UpdateProjectRequest req = new UpdateProjectRequest();
    req.setName("New Name");

    Project other = new Project();
    other.setId("proj2");
    other.setName("New Name");

    when(projectRepository.findByProjectId("proj1")).thenReturn(Optional.of(project));
    when(projectRepository.findByName("New Name")).thenReturn(Optional.of(other));

    assertThrows(ConflictException.class, () -> projectService.updateProject("proj1", req));
    verify(projectRepository, never()).save(any(Project.class));
  }

  @Test
  void deleteProject_SuperAdmin_Success() {
    User admin = new User();
    admin.setId("admin1");
    admin.setRole(Role.SUPER_ADMIN);
    admin.setProjectIds(new java.util.HashSet<>(java.util.Set.of("proj1")));

    Project project = new Project();
    project.setId("proj1");

    User otherAdmin = new User();
    otherAdmin.setId("admin2");
    otherAdmin.setRole(Role.SUB_ADMIN);
    otherAdmin.setProjectIds(new java.util.HashSet<>(java.util.Set.of("proj1", "proj2")));

    when(userRepository.findById("admin1")).thenReturn(Optional.of(admin));
    when(projectRepository.findByProjectId("proj1")).thenReturn(Optional.of(project));
    when(userRepository.findAll()).thenReturn(java.util.List.of(admin, otherAdmin));

    projectService.deleteProject("admin1", "proj1");

    verify(questionRepository).deleteAllByProjectId("proj1");
    verify(projectRepository).deleteByProjectId("proj1");
    verify(userRepository, times(1)).save(otherAdmin);
  }

  @Test
  void deleteProject_SubAdmin_Forbidden() {
    User admin = new User();
    admin.setId("subadmin1");
    admin.setRole(Role.SUB_ADMIN);

    when(userRepository.findById("subadmin1")).thenReturn(Optional.of(admin));

    assertThrows(ForbiddenException.class, () -> projectService.deleteProject("subadmin1", "proj1"));
    verify(projectRepository, never()).deleteByProjectId(any());
  }

  @Test
  void deleteProject_NotFound() {
    User admin = new User();
    admin.setId("admin1");
    admin.setRole(Role.SUPER_ADMIN);

    when(userRepository.findById("admin1")).thenReturn(Optional.of(admin));
    when(projectRepository.findByProjectId("missing")).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> projectService.deleteProject("admin1", "missing"));
    verify(projectRepository, never()).deleteByProjectId(any());
  }
}
