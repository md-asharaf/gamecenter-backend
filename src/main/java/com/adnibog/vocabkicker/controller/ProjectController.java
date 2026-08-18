package com.adnibog.vocabkicker.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.adnibog.vocabkicker.dto.request.CreateProjectRequest;
import com.adnibog.vocabkicker.dto.request.UpdateProjectRequest;
import com.adnibog.vocabkicker.dto.response.ProjectDto;
import com.adnibog.vocabkicker.service.ProjectService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/projects")
public class ProjectController {

  private final ProjectService projectService;

  public ProjectController(ProjectService projectService) {
    this.projectService = projectService;
  }

  @PostMapping
  public ResponseEntity<ProjectDto> createProject(
      HttpServletRequest request,
      @RequestBody(required = false) CreateProjectRequest req) {
    String adminId = (String) request.getAttribute("adminId");
    ProjectDto project = projectService.createProject(adminId, req);
    return ResponseEntity.ok(project);
  }

  @GetMapping
  public ResponseEntity<List<ProjectDto>> listProjects(HttpServletRequest request) {
    String adminId = (String) request.getAttribute("adminId");
    List<ProjectDto> projects = projectService.listProjectsForAdmin(adminId);
    return ResponseEntity.ok(projects);
  }

  @PutMapping("/{projectId}/projects")
  public ResponseEntity<ProjectDto> updateProjects(
      @PathVariable String projectId,
      @Valid @RequestBody UpdateProjectRequest req) {
    ProjectDto updated = projectService.updateProjectProjects(projectId, req);
    return ResponseEntity.ok(updated);
  }
}
