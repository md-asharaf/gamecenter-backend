package com.adnibog.gamecenter.service;

import org.springframework.stereotype.Service;

import com.adnibog.gamecenter.dto.request.CreateProjectRequest;
import com.adnibog.gamecenter.dto.request.UpdateProjectRequest;
import com.adnibog.gamecenter.dto.response.ProjectDto;
import com.adnibog.gamecenter.entity.Project;
import com.adnibog.gamecenter.entity.User;
import com.adnibog.gamecenter.exception.BadRequestException;
import com.adnibog.gamecenter.exception.ConflictException;
import com.adnibog.gamecenter.exception.NotFoundException;
import com.adnibog.gamecenter.mapper.ProjectMapper;
import com.adnibog.gamecenter.repository.ProjectRepository;
import com.adnibog.gamecenter.repository.QuestionRepository;
import com.adnibog.gamecenter.repository.UserRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.adnibog.gamecenter.entity.Role;
import com.adnibog.gamecenter.exception.ForbiddenException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ProjectService {

  private final ProjectRepository projectRepository;
  private final QuestionRepository questionRepository;
  private final UserRepository userRepository;
  private final ProjectMapper projectMapper;

  public ProjectService(ProjectRepository projectRepository, QuestionRepository questionRepository,
      UserRepository userRepository, ProjectMapper projectMapper) {
    this.projectRepository = projectRepository;
    this.questionRepository = questionRepository;
    this.userRepository = userRepository;
    this.projectMapper = projectMapper;
  }

  public ProjectDto createProject(String adminId, CreateProjectRequest req) {
    User admin = userRepository.findById(adminId)
        .orElseThrow(() -> new NotFoundException("Admin not found"));

    if (admin.getRole() != Role.SUPER_ADMIN) {
      log.warn("Admin {} attempted to create a project but is not a SUPER_ADMIN", adminId);
      throw new ForbiddenException("Only Super Admin can create a new project");
    }

    if (req != null && req.getName() != null && projectRepository.findByName(req.getName()).isPresent()) {
      throw new ConflictException("A project with this name already exists");
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
    project.setCreatedAt(now);
    project.setUpdatedAt(now);

    projectRepository.save(project);

    Set<String> projectIds = admin.getProjectIds();
    if (projectIds == null) {
      projectIds = new HashSet<>();
    }
    projectIds.add(id);
    admin.setProjectIds(projectIds);
    admin.setUpdatedAt(now);
    userRepository.save(admin);

    log.info("Project '{}' ({}) successfully created by Admin {}", project.getName(), id, adminId);
    return projectMapper.toDto(project);
  }

  public List<ProjectDto> listProjectsForAdmin(String adminId) {
    User admin = userRepository.findById(adminId)
        .orElseThrow(() -> new NotFoundException("Admin not found"));

    if (admin.getRole() == Role.SUPER_ADMIN) {
      return projectRepository.findAll().stream()
          .map(projectMapper::toDto)
          .collect(Collectors.toList());
    }

    if (admin.getProjectIds() == null || admin.getProjectIds().isEmpty()) {
      return new ArrayList<>();
    }

    return admin.getProjectIds().stream()
        .map(projectId -> projectRepository.findByProjectId(projectId).orElse(null))
        .filter(project -> project != null)
        .map(projectMapper::toDto)
        .collect(Collectors.toList());
  }

  public ProjectDto updateProject(String projectId, UpdateProjectRequest req) {
    Project project = projectRepository.findByProjectId(projectId)
        .orElseThrow(() -> new NotFoundException("Project not found"));

    if (req.getNumberOfQuestionsInQuiz() != null) {
      project.setNumberOfQuestionsInQuiz(req.getNumberOfQuestionsInQuiz());
    }
    if (req.getName() != null) {
      if (!req.getName().equals(project.getName())) {
        projectRepository.findByName(req.getName())
            .filter(other -> !other.getId().equals(project.getId()))
            .ifPresent(other -> {
              throw new ConflictException("A project with this name already exists");
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
        throw new BadRequestException("The main question label must match either the first or second field label");
      }
    }
    if (req.getField1Label() != null) {
      project.setField1Label(req.getField1Label());
    }
    if (req.getField2Label() != null) {
      project.setField2Label(req.getField2Label());
    }
    if (req.getField3Label() != null) {
      project.setField3Label(req.getField3Label());
    }

    project.setUpdatedAt(System.currentTimeMillis());
    projectRepository.save(project);

    log.info("Project '{}' ({}) successfully updated", project.getName(), project.getId());
    return projectMapper.toDto(project);
  }

  public void deleteProject(String adminId, String projectId) {
    User admin = userRepository.findById(adminId)
        .orElseThrow(() -> new NotFoundException("Admin not found"));

    if (admin.getRole() != Role.SUPER_ADMIN) {
      log.warn("Admin {} attempted to delete project {} but is not a SUPER_ADMIN", adminId, projectId);
      throw new ForbiddenException("Only Super Admin can delete a project");
    }

    projectRepository.findByProjectId(projectId)
        .orElseThrow(() -> new NotFoundException("Project not found"));

    questionRepository.deleteAllByProjectId(projectId);
    projectRepository.deleteByProjectId(projectId);
    removeProjectFromAdmins(projectId);

    log.info("Project {} successfully deleted by Admin {}", projectId, adminId);
  }

  private void removeProjectFromAdmins(String projectId) {
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
