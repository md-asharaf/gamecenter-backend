package com.adnibog.gamecenter.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.adnibog.gamecenter.dto.request.CreateProjectRequest;
import com.adnibog.gamecenter.dto.request.UpdateProjectRequest;
import com.adnibog.gamecenter.dto.response.ApiResponse;
import com.adnibog.gamecenter.dto.model.ProjectDto;
import com.adnibog.gamecenter.dto.response.ProjectPageResponse;

import com.adnibog.gamecenter.service.ProjectService;
import com.adnibog.gamecenter.service.UserService;
import com.adnibog.gamecenter.entity.User;
import com.adnibog.gamecenter.entity.Role;
import com.adnibog.gamecenter.exception.ForbiddenException;
import lombok.extern.slf4j.Slf4j;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestAttribute;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.ModelAttribute;
import com.adnibog.gamecenter.dto.request.PaginationRequest;

@Slf4j
@Tag(name = "Projects", description = "Endpoints for managing projects")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/projects")
public class ProjectController {

  private final ProjectService projectService;
  private final UserService userService;

  public ProjectController(ProjectService projectService, UserService userService) {
    this.projectService = projectService;
    this.userService = userService;
  }

  @Operation(summary = "Create Project", description = "Creates a new project. Requires SUPER_ADMIN role.")
  @PostMapping
  public ResponseEntity<ApiResponse<ProjectDto>> createProject(
      @RequestAttribute("adminId") String adminId,
      @RequestBody(required = false) CreateProjectRequest req) {
    User admin = userService.getUserEntityById(adminId);
    if (admin.getRole() != Role.SUPER_ADMIN) {
      log.warn("Admin {} attempted to create a project but is not a SUPER_ADMIN", adminId);
      throw new ForbiddenException("Insufficient privileges to create a project.");
    }
    ProjectDto project = projectService.createProject(adminId, req);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(project, "Project created successfully."));
  }

  @Operation(summary = "List Projects", description = "Lists all projects accessible to the current admin.")
  @GetMapping
  public ResponseEntity<ApiResponse<ProjectPageResponse>> listProjects(
      @RequestAttribute("adminId") String adminId,
      @ModelAttribute PaginationRequest pageReq) {
    User admin = userService.getUserEntityById(adminId);
    boolean isSuperAdmin = admin.getRole() == Role.SUPER_ADMIN;
    ProjectPageResponse projects = projectService.listProjects(isSuperAdmin, admin.getProjectIds(), pageReq);
    return ResponseEntity.ok(ApiResponse.success(projects));
  }

  @Operation(summary = "Get Project", description = "Retrieves a project by its ID.")
  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<ProjectDto>> getProject(@PathVariable String id) {
    ProjectDto project = projectService.getProjectById(id);
    return ResponseEntity.ok(ApiResponse.success(project));
  }

  @Operation(summary = "Update Project", description = "Updates a project's settings.")
  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<ProjectDto>> updateProject(
      @PathVariable String id,
      @Valid @RequestBody UpdateProjectRequest req) {
    ProjectDto updated = projectService.updateProject(id, req);
    return ResponseEntity.ok(ApiResponse.success(updated, "Project updated successfully."));
  }

  @Operation(summary = "Delete Project", description = "Deletes a project and its questions. Requires SUPER_ADMIN role.")
  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> deleteProject(
      @RequestAttribute("adminId") String adminId, 
      @PathVariable String id) {
    User admin = userService.getUserEntityById(adminId);
    if (admin.getRole() != Role.SUPER_ADMIN) {
      log.warn("Admin {} attempted to delete project {} but is not a SUPER_ADMIN", adminId, id);
      throw new ForbiddenException("Insufficient privileges to delete a project.");
    }
    projectService.deleteProject(adminId, id);
    return ResponseEntity.ok(ApiResponse.success(null, "Project deleted successfully."));
  }
}
