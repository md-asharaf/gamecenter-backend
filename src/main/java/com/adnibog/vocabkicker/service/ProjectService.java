package com.adnibog.vocabkicker.service;

import org.springframework.stereotype.Service;

import com.adnibog.vocabkicker.dto.request.CreateProjectRequest;
import com.adnibog.vocabkicker.dto.request.UpdateProjectRequest;
import com.adnibog.vocabkicker.dto.response.ProjectDto;
import com.adnibog.vocabkicker.entity.Project;
import com.adnibog.vocabkicker.entity.User;
import com.adnibog.vocabkicker.exception.NotFoundException;
import com.adnibog.vocabkicker.mapper.ProjectMapper;
import com.adnibog.vocabkicker.repository.ProjectRepository;
import com.adnibog.vocabkicker.repository.UserRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.adnibog.vocabkicker.entity.Role;
import com.adnibog.vocabkicker.exception.UnauthorizedException;

@Service
public class ProjectService {

  private final ProjectRepository projectRepository;
  private final UserRepository userRepository;
  private final ProjectMapper projectMapper;

  public ProjectService(ProjectRepository projectRepository, UserRepository userRepository,
      ProjectMapper projectMapper) {
    this.projectRepository = projectRepository;
    this.userRepository = userRepository;
    this.projectMapper = projectMapper;
  }

  public ProjectDto createProject(String adminId, CreateProjectRequest req) {
    User admin = userRepository.findById(adminId)
        .orElseThrow(() -> new NotFoundException("Admin not found"));

    if (admin.getRole() != Role.SUPER_ADMIN) {
      throw new UnauthorizedException("Only SUPER_ADMIN can create a new project");
    }

    String projectId = UUID.randomUUID().toString();
    long now = System.currentTimeMillis();

    Project project = Project.builder()
        .projectId(projectId)
        .numberOfQuestionsInQuiz(10)
        .mainQuestionField("field1")
        .field1Label(req != null && req.getField1Label() != null ? req.getField1Label() : "Field 1")
        .field2Label(req != null && req.getField2Label() != null ? req.getField2Label() : "Field 2")
        .field3Label(req != null && req.getField3Label() != null ? req.getField3Label() : "Field 3")
        .createdAt(now)
        .updatedAt(now)
        .build();

    projectRepository.save(project);

    Set<String> projectIds = admin.getProjectIds();
    if (projectIds == null) {
      projectIds = new HashSet<>();
    }
    projectIds.add(projectId);
    admin.setProjectIds(projectIds);
    admin.setUpdatedAt(now);
    userRepository.save(admin);

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

  public ProjectDto updateProjectProjects(String projectId, UpdateProjectRequest req) {
    Project project = projectRepository.findByProjectId(projectId)
        .orElseThrow(() -> new NotFoundException("Project projects not found"));

    if (req.getNumberOfQuestionsInQuiz() != null) {
      project.setNumberOfQuestionsInQuiz(req.getNumberOfQuestionsInQuiz());
    }
    if (req.getMainQuestionLabel() != null) {
      if (req.getMainQuestionLabel().equals(project.getField1Label())
          || req.getMainQuestionLabel().equals(req.getField1Label())) {
        project.setMainQuestionField("field1");
      } else if (req.getMainQuestionLabel().equals(project.getField2Label())
          || req.getMainQuestionLabel().equals(req.getField2Label())) {
        project.setMainQuestionField("field2");
      } else {
        throw new IllegalArgumentException("mainQuestionLabel must match either field1Label or field2Label");
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

    return projectMapper.toDto(project);
  }
}
