package com.adnibog.gamecenter.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.adnibog.gamecenter.dto.request.CreateQuestionRequest;
import com.adnibog.gamecenter.dto.request.UpdateQuestionRequest;
import com.adnibog.gamecenter.dto.response.ApiResponse;
import com.adnibog.gamecenter.dto.model.QuestionDto;
import com.adnibog.gamecenter.dto.response.QuestionPageResponse;
import com.adnibog.gamecenter.service.QuestionService;
import com.adnibog.gamecenter.service.ProjectService;
import com.adnibog.gamecenter.dto.model.ProjectDto;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.adnibog.gamecenter.dto.request.PaginationRequest;

@Tag(name = "Questions", description = "Endpoints for managing project questions")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/projects/{projectId}/folders/{folderId}/questions")
public class QuestionController {

  private final QuestionService questionService;
  private final ProjectService projectService;

  public QuestionController(QuestionService questionService, ProjectService projectService) {
    this.questionService = questionService;
    this.projectService = projectService;
  }

  @Operation(summary = "Get Questions", description = "Retrieves a paginated list of questions for a project.")
  @GetMapping
  public ResponseEntity<ApiResponse<QuestionPageResponse>> getQuestions(
      @PathVariable String projectId,
      @PathVariable String folderId,
      @ModelAttribute PaginationRequest pageReq) {
    ProjectDto project = projectService.getProjectById(projectId);
    QuestionPageResponse response = questionService.getQuestions(project, folderId, pageReq);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @Operation(summary = "Create Question", description = "Creates a new question using dynamic fields based on project labels.")
  @PostMapping
  public ResponseEntity<ApiResponse<QuestionDto>> createQuestion(
      @PathVariable String projectId,
      @PathVariable String folderId,
      @Valid @RequestBody CreateQuestionRequest req) {
    ProjectDto project = projectService.getProjectById(projectId);
    QuestionDto created = questionService.createQuestion(project, folderId, req);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(created, "Question created successfully"));
  }

  @Operation(summary = "Get Question by ID", description = "Fetches a single question.")
  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<QuestionDto>> getQuestion(@PathVariable String projectId, @PathVariable String folderId, @PathVariable String id) {
    ProjectDto project = projectService.getProjectById(projectId);
    QuestionDto q = questionService.getQuestionById(project, id);
    return ResponseEntity.ok(ApiResponse.success(q));
  }

  @Operation(summary = "Update Question", description = "Updates a question using dynamic fields.")
  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<QuestionDto>> updateQuestion(
      @PathVariable String projectId,
      @PathVariable String folderId,
      @PathVariable String id,
      @Valid @RequestBody UpdateQuestionRequest req) {
    ProjectDto project = projectService.getProjectById(projectId);
    QuestionDto updated = questionService.updateQuestion(project, id, req);
    return ResponseEntity.ok(ApiResponse.success(updated, "Question updated successfully"));
  }

  @Operation(summary = "Delete Question", description = "Deletes a question by ID.")
  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> deleteQuestion(@PathVariable String projectId, @PathVariable String folderId, @PathVariable String id) {
    questionService.deleteQuestion(projectId, id);
    return ResponseEntity.ok(ApiResponse.success(null, "Question deleted successfully"));
  }

  @Operation(summary = "Bulk Delete Questions", description = "Deletes multiple questions by ID.")
  @DeleteMapping
  public ResponseEntity<ApiResponse<Void>> deleteQuestions(
      @PathVariable String projectId,
      @PathVariable String folderId,
      @RequestBody java.util.List<String> questionIds) {
    questionService.deleteQuestions(projectId, questionIds);
    return ResponseEntity.ok(ApiResponse.success(null, "Questions deleted successfully"));
  }
}
