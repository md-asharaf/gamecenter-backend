package com.adnibog.vocabkicker.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.adnibog.vocabkicker.dto.request.CreateProjectRequest;
import com.adnibog.vocabkicker.dto.request.UpdateProjectRequest;
import com.adnibog.vocabkicker.dto.response.ApiResponse;
import com.adnibog.vocabkicker.dto.response.ProjectDto;
import com.adnibog.vocabkicker.service.ProjectService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Projects", description = "Endpoints for managing projects")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/projects")
public class ProjectController {

  private final ProjectService projectService;

  public ProjectController(ProjectService projectService) {
    this.projectService = projectService;
  }

  @Operation(summary = "Create Project", description = "Creates a new project. Requires SUPER_ADMIN role.")
  @PostMapping
  public ResponseEntity<ApiResponse<ProjectDto>> createProject(
      HttpServletRequest request,
      @RequestBody(required = false) CreateProjectRequest req) {
    String adminId = (String) request.getAttribute("adminId");
    ProjectDto project = projectService.createProject(adminId, req);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(project, "Project created successfully"));
  }

  @Operation(summary = "List Projects", description = "Lists all projects accessible to the current admin.")
  @GetMapping
  public ResponseEntity<ApiResponse<List<ProjectDto>>> listProjects(HttpServletRequest request) {
    String adminId = (String) request.getAttribute("adminId");
    List<ProjectDto> projects = projectService.listProjectsForAdmin(adminId);
    return ResponseEntity.ok(ApiResponse.success(projects));
  }

  @Operation(summary = "Update Project", description = "Updates a project's settings.")
  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<ProjectDto>> updateProject(
      @PathVariable String id,
      @Valid @RequestBody UpdateProjectRequest req) {
    ProjectDto updated = projectService.updateProject(id, req);
    return ResponseEntity.ok(ApiResponse.success(updated, "Project updated successfully"));
  }
}
