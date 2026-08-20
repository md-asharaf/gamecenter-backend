package com.adnibog.gamecenter.service;

import org.springframework.stereotype.Service;

import com.adnibog.gamecenter.dto.request.CreateProjectRequest;
import com.adnibog.gamecenter.dto.request.PaginationRequest;
import com.adnibog.gamecenter.dto.request.UpdateProjectRequest;
import com.adnibog.gamecenter.dto.model.ProjectDto;
import com.adnibog.gamecenter.dto.response.ProjectPageResponse;
import com.adnibog.gamecenter.repository.pagination.ProjectPage;

import com.adnibog.gamecenter.entity.Project;
import com.adnibog.gamecenter.entity.User;
import com.adnibog.gamecenter.exception.BadRequestException;
import com.adnibog.gamecenter.exception.ConflictException;
import com.adnibog.gamecenter.exception.NotFoundException;
import com.adnibog.gamecenter.mapper.ProjectMapper;
import com.adnibog.gamecenter.repository.ProjectRepository;
import org.springframework.context.ApplicationEventPublisher;
import com.adnibog.gamecenter.event.ProjectDeletedEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.adnibog.gamecenter.entity.Role;
import com.adnibog.gamecenter.exception.ForbiddenException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ProjectService {

  private final ProjectRepository projectRepository;
  private final UserService userService;
  private final ProjectMapper projectMapper;
  private final ApplicationEventPublisher eventPublisher;
  private final AppStatsService appStatsService;

  public ProjectService(ProjectRepository projectRepository,
      UserService userService, ProjectMapper projectMapper,
      ApplicationEventPublisher eventPublisher, AppStatsService appStatsService) {
    this.projectRepository = projectRepository;
    this.userService = userService;
    this.projectMapper = projectMapper;
    this.eventPublisher = eventPublisher;
    this.appStatsService = appStatsService;
  }

  private void validateFieldLabel(String label) {
    if (label == null || label.trim().isEmpty()) {
      return;
    }
    String lower = label.trim().toLowerCase();
    if (lower.equals("id") || lower.equals("createdat") || lower.equals("updatedat") || lower.equals("projects")
        || lower.equals("dynamicfields")) {
      throw new BadRequestException("Field label '" + label + "' is a reserved keyword and cannot be used.");
    }
  }

  public Project getProjectEntityById(String projectId) {
    return projectRepository.findById(projectId)
        .orElseThrow(() -> new NotFoundException("Project not found."));
  }

  public ProjectDto getProjectById(String projectId) {
    return projectMapper.toDto(getProjectEntityById(projectId));
  }

  public ProjectDto createProject(String adminId, CreateProjectRequest req) {
    User admin = userService.getUserEntityById(adminId);

    if (admin.getRole() != Role.SUPER_ADMIN) {
      log.warn("Admin {} attempted to create a project but is not a SUPER_ADMIN", adminId);
      throw new ForbiddenException("Insufficient privileges to create a project.");
    }

    if (req != null && req.getName() != null && projectRepository.findByName(req.getName()).isPresent()) {
      throw new ConflictException("Project name is already in use.");
    }

    String id = UUID.randomUUID().toString();
    long now = System.currentTimeMillis();

    Project project = new Project();
    project.setId(id);
    project.setName(req != null ? req.getName() : null);
    project.setField1Label(req != null && req.getField1Label() != null ? req.getField1Label() : "Field 1");
    project.setNumberOfQuestionsInQuiz(10);
    project.setMainQuestionField("field1");
    project.setField2Label(req != null && req.getField2Label() != null ? req.getField2Label() : "Field 2");
    project.setField3Label(req != null && req.getField3Label() != null ? req.getField3Label() : "Field 3");

    validateFieldLabel(project.getField1Label());
    validateFieldLabel(project.getField2Label());
    validateFieldLabel(project.getField3Label());

    project.setCreatedAt(now);
    project.setUpdatedAt(now);

    projectRepository.save(project);
    eventPublisher.publishEvent(new com.adnibog.gamecenter.event.ProjectCreatedEvent(this, id));

    userService.addProjectToAdmin(adminId, id);

    log.info("Project '{}' ({}) successfully created by Admin {}", project.getName(), id, adminId);
    return projectMapper.toDto(project);
  }

  public ProjectPageResponse listProjects(String adminId, PaginationRequest pageReq) {
    User admin = userService.getUserEntityById(adminId);

    if (admin.getRole() == Role.SUPER_ADMIN) {
      ProjectPage page = projectRepository.findProjects(pageReq);
      List<ProjectDto> dtos = page.getItems().stream().map(projectMapper::toDto).collect(Collectors.toList());
      return new ProjectPageResponse(dtos, page.getLastEvaluatedKey());
    }

    if (admin.getProjectIds() == null || admin.getProjectIds().isEmpty()) {
      return new ProjectPageResponse(new ArrayList<>(), null);
    }

    List<ProjectDto> allAllowed = admin.getProjectIds().stream()
        .map(projectId -> projectRepository.findById(projectId).orElse(null))
        .filter(project -> project != null)
        .filter(project -> pageReq.getSearch() == null || pageReq.getSearch().trim().isEmpty()
            || project.getName().toLowerCase().contains(pageReq.getSearch().toLowerCase()))
        .map(projectMapper::toDto)
        .collect(Collectors.toList());

    int startIndex = 0;
    if (pageReq.getLastEvaluatedKey() != null && !pageReq.getLastEvaluatedKey().isEmpty()) {
      for (int i = 0; i < allAllowed.size(); i++) {
        if (allAllowed.get(i).getId().equals(pageReq.getLastEvaluatedKey())) {
          startIndex = i + 1;
          break;
        }
      }
    }

    int endIndex = Math.min(startIndex + pageReq.getLimit(), allAllowed.size());
    List<ProjectDto> paged = allAllowed.subList(startIndex, endIndex);
    String nextKey = endIndex < allAllowed.size() ? paged.get(paged.size() - 1).getId() : null;

    return new ProjectPageResponse(paged, nextKey);
  }


  public List<ProjectDto> getMostRecentProjectsForAdmin(String adminId, int limit) {
    User admin = userService.getUserEntityById(adminId);
    if (admin.getRole() == Role.SUPER_ADMIN) {
      return projectRepository.getMostRecentProjects(limit).stream()
          .map(projectMapper::toDto)
          .collect(Collectors.toList());
    }
    if (admin.getProjectIds() == null || admin.getProjectIds().isEmpty()) {
      return new ArrayList<>();
    }
    List<ProjectDto> projects = admin.getProjectIds().stream()
        .map(id -> projectRepository.findById(id))
        .filter(opt -> opt.isPresent())
        .map(opt -> projectMapper.toDto(opt.get()))
        .collect(Collectors.toList());

    projects.sort((p1, p2) -> {
      long t1 = p1.getCreatedAt() != null ? p1.getCreatedAt() : 0L;
      long t2 = p2.getCreatedAt() != null ? p2.getCreatedAt() : 0L;
      return Long.compare(t2, t1);
    });

    return projects.stream().limit(limit).collect(Collectors.toList());
  }

  public long getTotalProjectsForAdmin(String adminId) {
    User admin = userService.getUserEntityById(adminId);
    if (admin.getRole() == Role.SUPER_ADMIN) {
      return appStatsService.getTotalProjects();
    }
    return admin.getProjectIds() != null ? admin.getProjectIds().size() : 0;
  }

  public ProjectDto updateProject(String projectId, UpdateProjectRequest req) {
    Project project = getProjectEntityById(projectId);

    if (req.getNumberOfQuestionsInQuiz() != null) {
      project.setNumberOfQuestionsInQuiz(req.getNumberOfQuestionsInQuiz());
    }
    if (req.getName() != null) {
      if (!req.getName().equals(project.getName())) {
        projectRepository.findByName(req.getName())
            .filter(other -> !other.getId().equals(project.getId()))
            .ifPresent(other -> {
              throw new ConflictException("Project name is already in use.");
            });
      }
      project.setName(req.getName());
    }
    if (req.getMainQuestionLabel() != null) {
      if (req.getMainQuestionLabel().equals(project.getField1Label())
          || req.getMainQuestionLabel().equals(req.getField1Label())) {
        project.setMainQuestionField("field1");
      } else if (req.getMainQuestionLabel().equals(project.getField2Label())
          || req.getMainQuestionLabel().equals(req.getField2Label())) {
        project.setMainQuestionField("field2");
      } else {
        log.warn("Update project {} failed: main question label '{}' does not match any existing fields", projectId,
            req.getMainQuestionLabel());
        throw new BadRequestException("Main question label must match a defined field label.");
      }
    }
    if (req.getField1Label() != null) {
      validateFieldLabel(req.getField1Label());
      project.setField1Label(req.getField1Label());
    }
    if (req.getField2Label() != null) {
      validateFieldLabel(req.getField2Label());
      project.setField2Label(req.getField2Label());
    }
    if (req.getField3Label() != null) {
      validateFieldLabel(req.getField3Label());
      project.setField3Label(req.getField3Label());
    }
    if (req.getQuizFolderId() != null) {
      project.setQuizFolderId(req.getQuizFolderId());
    }

    project.setUpdatedAt(System.currentTimeMillis());
    projectRepository.save(project);

    log.info("Project '{}' ({}) successfully updated", project.getName(), project.getId());
    return projectMapper.toDto(project);
  }

  public void updateQuizFolderId(String projectId, String folderId) {
    Project project = getProjectEntityById(projectId);
    project.setQuizFolderId(folderId);
    project.setUpdatedAt(System.currentTimeMillis());
    projectRepository.save(project);
  }

  public void deleteProject(String adminId, String projectId) {
    User admin = userService.getUserEntityById(adminId);

    if (admin.getRole() != Role.SUPER_ADMIN) {
      log.warn("Admin {} attempted to delete project {} but is not a SUPER_ADMIN", adminId, projectId);
      throw new ForbiddenException("Insufficient privileges to delete a project.");
    }

    getProjectEntityById(projectId);

    eventPublisher.publishEvent(new ProjectDeletedEvent(this, projectId));

    projectRepository.deleteById(projectId);

    log.info("Project {} successfully deleted by Admin {}", projectId, adminId);
  }
}
