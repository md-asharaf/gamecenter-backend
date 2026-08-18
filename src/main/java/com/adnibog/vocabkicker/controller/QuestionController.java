package com.adnibog.vocabkicker.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.adnibog.vocabkicker.dto.request.CreateQuestionRequest;
import com.adnibog.vocabkicker.dto.request.UpdateQuestionRequest;
import com.adnibog.vocabkicker.dto.response.ApiResponse;
import com.adnibog.vocabkicker.dto.response.QuestionDto;
import com.adnibog.vocabkicker.dto.response.QuestionPageResponse;
import com.adnibog.vocabkicker.service.QuestionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/projects/{projectId}/questions")
public class QuestionController {

  private final QuestionService questionService;

  public QuestionController(QuestionService questionService) {
    this.questionService = questionService;
  }

  @GetMapping
  public ResponseEntity<ApiResponse<QuestionPageResponse>> getQuestions(
      @PathVariable String projectId,
      @RequestParam(defaultValue = "10") int limit,
      @RequestParam(required = false) String lastEvaluatedKey,
      @RequestParam(required = false) String search) {
    QuestionPageResponse response = questionService.getQuestions(projectId, limit, lastEvaluatedKey, search);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @PostMapping
  public ResponseEntity<ApiResponse<QuestionDto>> createQuestion(
      @PathVariable String projectId,
      @Valid @RequestBody CreateQuestionRequest req) {
    QuestionDto created = questionService.createQuestionFromRequest(projectId, req);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(created, "Question created successfully"));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<QuestionDto>> getQuestion(@PathVariable String projectId, @PathVariable String id) {
    QuestionDto q = questionService.getQuestionById(projectId, id);
    return ResponseEntity.ok(ApiResponse.success(q));
  }

  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<QuestionDto>> updateQuestion(
      @PathVariable String projectId,
      @PathVariable String id,
      @Valid @RequestBody UpdateQuestionRequest req) {
    QuestionDto updated = questionService.updateQuestion(projectId, id, req);
    return ResponseEntity.ok(ApiResponse.success(updated, "Question updated successfully"));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> deleteQuestion(@PathVariable String projectId, @PathVariable String id) {
    questionService.deleteQuestion(projectId, id);
    return ResponseEntity.ok(ApiResponse.success(null, "Question deleted successfully"));
  }

}
