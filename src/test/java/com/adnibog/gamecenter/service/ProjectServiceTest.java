package com.adnibog.gamecenter.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import com.adnibog.gamecenter.dto.request.CreateProjectRequest;
import com.adnibog.gamecenter.dto.request.UpdateProjectRequest;
import com.adnibog.gamecenter.dto.model.ProjectDto;
import com.adnibog.gamecenter.entity.Project;
import com.adnibog.gamecenter.entity.Role;
import com.adnibog.gamecenter.entity.User;
import com.adnibog.gamecenter.exception.ConflictException;
import com.adnibog.gamecenter.exception.ForbiddenException;
import com.adnibog.gamecenter.exception.NotFoundException;
import com.adnibog.gamecenter.mapper.ProjectMapper;
import com.adnibog.gamecenter.repository.AppStatsRepository;
import com.adnibog.gamecenter.repository.ProjectRepository;

import org.springframework.context.ApplicationEventPublisher;
import com.adnibog.gamecenter.event.ProjectDeletedEvent;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

  @Mock
  private ProjectRepository projectRepository;

  @Mock
  private ApplicationEventPublisher eventPublisher;

  @Mock
  private UserService userService;

  @Mock
  private ProjectMapper projectMapper;

  @Mock
  private AppStatsRepository appStatsRepository;

  private ProjectService projectService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    projectService = new ProjectService(projectRepository, userService, projectMapper, eventPublisher,
        appStatsRepository);
  }

  @Test
  void createProject_SuperAdmin_Success() {
    User admin = new User();
    admin.setId("admin1");
    admin.setRole(Role.SUPER_ADMIN);

    CreateProjectRequest req = new CreateProjectRequest();
    req.setName("Test Project");

    ProjectDto expectedDto = new ProjectDto();
    expectedDto.setName("Test Project");

    when(userService.getUserEntityById("admin1")).thenReturn(admin);
    when(projectMapper.toDto(any(Project.class))).thenReturn(expectedDto);

    ProjectDto result = projectService.createProject("admin1", req);

    assertNotNull(result);
    assertEquals("Test Project", result.getName());
    verify(projectRepository).save(any(Project.class));
    verify(userService).addProjectToAdmin(eq("admin1"), any(String.class));
  }

  @Test
  void createProject_SubAdmin_Forbidden() {
    User admin = new User();
    admin.setId("subadmin1");
    admin.setRole(Role.SUB_ADMIN);

    CreateProjectRequest req = new CreateProjectRequest();
    req.setName("Test Project");

    when(userService.getUserEntityById("subadmin1")).thenReturn(admin);

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

    when(userService.getUserEntityById("admin1")).thenReturn(admin);
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

    when(projectRepository.findById("proj1")).thenReturn(Optional.of(project));
    when(projectRepository.findByName("New Name")).thenReturn(Optional.of(other));

    assertThrows(ConflictException.class, () -> projectService.updateProject("proj1", req));
    verify(projectRepository, never()).save(any(Project.class));
  }

  @Test
  @SuppressWarnings("null")
  void deleteProject_SuperAdmin_Success() {
    User admin = new User();
    admin.setId("admin1");
    admin.setRole(Role.SUPER_ADMIN);

    Project project = new Project();
    project.setId("proj1");

    when(userService.getUserEntityById("admin1")).thenReturn(admin);
    when(projectRepository.findById("proj1")).thenReturn(Optional.of(project));

    projectService.deleteProject("admin1", "proj1");

    ArgumentCaptor<ProjectDeletedEvent> captor = ArgumentCaptor.forClass(ProjectDeletedEvent.class);
    verify(eventPublisher).publishEvent(captor.capture());
    verify(projectRepository).deleteById("proj1");
    verify(userService).removeProjectFromAllAdmins("proj1");
  }

  @Test
  void deleteProject_SubAdmin_Forbidden() {
    User admin = new User();
    admin.setId("subadmin1");
    admin.setRole(Role.SUB_ADMIN);

    when(userService.getUserEntityById("subadmin1")).thenReturn(admin);

    assertThrows(ForbiddenException.class, () -> projectService.deleteProject("subadmin1", "proj1"));
    verify(projectRepository, never()).deleteById(any());
  }

  @Test
  void deleteProject_NotFound() {
    User admin = new User();
    admin.setId("admin1");
    admin.setRole(Role.SUPER_ADMIN);

    when(userService.getUserEntityById("admin1")).thenReturn(admin);
    when(projectRepository.findById("missing")).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> projectService.deleteProject("admin1", "missing"));
    verify(projectRepository, never()).deleteById(any());
  }
}
