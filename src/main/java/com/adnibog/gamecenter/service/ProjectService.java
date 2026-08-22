package com.adnibog.gamecenter.service;

import org.springframework.stereotype.Service;

import com.adnibog.gamecenter.dto.request.CreateProjectRequest;
import com.adnibog.gamecenter.dto.request.PaginationRequest;
import com.adnibog.gamecenter.dto.request.UpdateProjectRequest;
import com.adnibog.gamecenter.dto.model.ProjectDto;
import com.adnibog.gamecenter.dto.response.ProjectPageResponse;
import com.adnibog.gamecenter.repository.pagination.ProjectPage;

import com.adnibog.gamecenter.entity.Project;
import com.adnibog.gamecenter.exception.BadRequestException;
import com.adnibog.gamecenter.exception.ConflictException;
import com.adnibog.gamecenter.exception.NotFoundException;
import com.adnibog.gamecenter.mapper.ProjectMapper;
import com.adnibog.gamecenter.repository.ProjectRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;

import com.adnibog.gamecenter.event.FolderCreatedEvent;
import com.adnibog.gamecenter.event.FolderDeletedEvent;
import com.adnibog.gamecenter.event.ProjectDeletedEvent;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ProjectService {

  private final ProjectRepository projectRepository;
  private final ProjectMapper projectMapper;
  private final ApplicationEventPublisher eventPublisher;

  public ProjectService(ProjectRepository projectRepository,
      ProjectMapper projectMapper,
      ApplicationEventPublisher eventPublisher) {
    this.projectRepository = projectRepository;
    this.projectMapper = projectMapper;
    this.eventPublisher = eventPublisher;
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
    eventPublisher.publishEvent(new com.adnibog.gamecenter.event.ProjectCreatedEvent(this, id, adminId));

    log.info("Project '{}' ({}) successfully created by Admin {}", project.getName(), id, adminId);
    return projectMapper.toDto(project);
  }

  public ProjectPageResponse listProjects(boolean isSuperAdmin, java.util.Set<String> allowedProjectIds,
      PaginationRequest pageReq) {
    if (isSuperAdmin) {
      ProjectPage page = projectRepository.findProjects(pageReq);
      List<ProjectDto> dtos = page.getItems().stream().map(projectMapper::toDto).collect(Collectors.toList());
      return new ProjectPageResponse(dtos, page.getLastEvaluatedKey());
    }

    if (allowedProjectIds == null || allowedProjectIds.isEmpty()) {
      return new ProjectPageResponse(new ArrayList<>(), null);
    }

    List<ProjectDto> allAllowed = allowedProjectIds.stream()
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

  public List<ProjectDto> getMostRecentProjects(boolean isSuperAdmin, java.util.Set<String> allowedProjectIds,
      int limit) {
    if (isSuperAdmin) {
      return projectRepository.getMostRecentProjects(limit).stream()
          .map(projectMapper::toDto)
          .collect(Collectors.toList());
    }
    if (allowedProjectIds == null || allowedProjectIds.isEmpty()) {
      return new ArrayList<>();
    }
    List<ProjectDto> projects = allowedProjectIds.stream()
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

  public long getTotalProjects(java.util.Set<String> allowedProjectIds) {
    return allowedProjectIds != null ? allowedProjectIds.size() : 0;
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
    getProjectEntityById(projectId);

    eventPublisher.publishEvent(new ProjectDeletedEvent(this, projectId));

    projectRepository.deleteById(projectId);

    log.info("Project {} successfully deleted by Admin {}", projectId, adminId);
  }

  @EventListener
  public void handleFolderCreated(FolderCreatedEvent event) {
    if (event.isFirstFolder()) {
      updateQuizFolderId(event.getProjectId(), event.getFolderId());
      log.info("Set default quiz folder for project {} to newly created folder {}", event.getProjectId(),
          event.getFolderId());
    }
  }

  @EventListener
  public void handleFolderDeleted(FolderDeletedEvent event) {
    if (event.isLastFolder()) {
      updateQuizFolderId(event.getProjectId(), null);
      log.info("Unset quiz folder for project {} as the last folder was deleted", event.getProjectId());
    } else {
      Project project = projectRepository.findById(event.getProjectId()).orElse(null);
      if (project != null && event.getFolderId().equals(project.getQuizFolderId())) {
        updateQuizFolderId(event.getProjectId(), null);
        log.info("Unset quiz folder for project {} because the active quiz folder was deleted", event.getProjectId());
      }
    }
  }

  public byte[] getUploadTemplate(String projectId) {
    Project project = getProjectEntityById(projectId);
    String field1 = project.getField1Label() != null ? project.getField1Label() : "Field 1";
    String field2 = project.getField2Label() != null ? project.getField2Label() : "Field 2";
    String field3 = project.getField3Label() != null ? project.getField3Label() : "Field 3";
    String csvContent = String.format("%s,%s,%s\n", field1, field2, field3);
    return csvContent.getBytes(StandardCharsets.UTF_8);
  }

  public List<String> getUploadInstructions(String projectId) {
    Project project = getProjectEntityById(projectId);
    String field1 = project.getField1Label() != null ? project.getField1Label() : "Field 1";
    String field2 = project.getField2Label() != null ? project.getField2Label() : "Field 2";
    String field3 = project.getField3Label() != null ? project.getField3Label() : "Field 3";

    return Arrays.asList(
        "1. You can upload either a .csv file or a Word document (.docx) with a table.",
        String.format("2. Your file must include columns named: '%s', '%s', and '%s' in the first row.", field1, field2,
            field3),
        "3. The order of the columns does not matter (capitalization and extra spaces also do not matter).",
        "4. Any extra columns in your file will be safely ignored.",
        "5. Rows that do not have data for all required columns will be skipped to prevent incomplete questions.",
        "6. For best results, download the template CSV file and put your data under the provided headers.");
  }
}
