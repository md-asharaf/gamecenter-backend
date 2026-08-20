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
import com.adnibog.gamecenter.exception.ConflictException;
import com.adnibog.gamecenter.exception.NotFoundException;
import com.adnibog.gamecenter.mapper.ProjectMapper;
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
  private ProjectMapper projectMapper;

  private ProjectService projectService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    projectService = new ProjectService(projectRepository, projectMapper, eventPublisher);
  }

  @Test
  void createProject_Success() {
    CreateProjectRequest req = new CreateProjectRequest();
    req.setName("Test Project");

    ProjectDto expectedDto = new ProjectDto();
    expectedDto.setName("Test Project");

    when(projectMapper.toDto(any(Project.class))).thenReturn(expectedDto);

    ProjectDto result = projectService.createProject("admin1", req);

    assertNotNull(result);
    assertEquals("Test Project", result.getName());
    verify(projectRepository).save(any(Project.class));
  }

  @Test
  void createProject_DuplicateName_Conflict() {
    CreateProjectRequest req = new CreateProjectRequest();
    req.setName("Test Project");

    Project existing = new Project();
    existing.setName("Test Project");

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
  void deleteProject_Success() {
    Project project = new Project();
    project.setId("proj1");

    when(projectRepository.findById("proj1")).thenReturn(Optional.of(project));

    projectService.deleteProject("admin1", "proj1");

    ArgumentCaptor<ProjectDeletedEvent> captor = ArgumentCaptor.forClass(ProjectDeletedEvent.class);
    verify(eventPublisher).publishEvent(captor.capture());
    verify(projectRepository).deleteById("proj1");
  }

  @Test
  void deleteProject_NotFound() {
    when(projectRepository.findById("missing")).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> projectService.deleteProject("admin1", "missing"));
    verify(projectRepository, never()).deleteById(any());
  }
}
