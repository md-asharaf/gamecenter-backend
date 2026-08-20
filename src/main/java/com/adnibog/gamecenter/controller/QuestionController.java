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

  public QuestionController(QuestionService questionService) {
    this.questionService = questionService;
  }

  @Operation(summary = "Get Questions", description = "Retrieves a paginated list of questions for a project.")
  @GetMapping
  public ResponseEntity<ApiResponse<QuestionPageResponse>> getQuestions(
      @PathVariable String projectId,
      @PathVariable String folderId,
      @ModelAttribute PaginationRequest pageReq) {
    QuestionPageResponse response = questionService.getQuestions(projectId, folderId, pageReq);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @Operation(summary = "Create Question", description = "Creates a new question using dynamic fields based on project labels.")
  @PostMapping
  public ResponseEntity<ApiResponse<QuestionDto>> createQuestion(
      @PathVariable String projectId,
      @PathVariable String folderId,
      @Valid @RequestBody CreateQuestionRequest req) {
    QuestionDto created = questionService.createQuestion(projectId, folderId, req);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(created, "Question created successfully"));
  }

  @Operation(summary = "Get Question by ID", description = "Fetches a single question.")
  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<QuestionDto>> getQuestion(@PathVariable String projectId, @PathVariable String folderId, @PathVariable String id) {
    QuestionDto q = questionService.getQuestionById(projectId, id);
    return ResponseEntity.ok(ApiResponse.success(q));
  }

  @Operation(summary = "Update Question", description = "Updates a question using dynamic fields.")
  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<QuestionDto>> updateQuestion(
      @PathVariable String projectId,
      @PathVariable String folderId,
      @PathVariable String id,
      @Valid @RequestBody UpdateQuestionRequest req) {
    QuestionDto updated = questionService.updateQuestion(projectId, id, req);
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
