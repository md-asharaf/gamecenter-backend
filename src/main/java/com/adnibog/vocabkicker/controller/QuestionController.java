package com.adnibog.vocabkicker.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.adnibog.vocabkicker.dto.request.UpdateQuestionRequest;
import com.adnibog.vocabkicker.dto.response.ApiResponse;
import com.adnibog.vocabkicker.dto.response.QuestionDto;
import com.adnibog.vocabkicker.dto.response.QuestionPageResponse;
import com.adnibog.vocabkicker.dto.response.UploadUrlResponse;
import com.adnibog.vocabkicker.service.QuestionService;
import com.adnibog.vocabkicker.service.StorageService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/questions")
public class QuestionController {

  private final QuestionService questionService;
  private final StorageService storageService;

  public QuestionController(QuestionService questionService, StorageService storageService) {
    this.questionService = questionService;
    this.storageService = storageService;
  }

  @GetMapping
  public ResponseEntity<ApiResponse<QuestionPageResponse>> getQuestions(
      @RequestParam(required = false, defaultValue = "10") int limit,
      @RequestParam(required = false) String lastEvaluatedKey,
      @RequestParam(required = false) String search) {

    QuestionPageResponse response = questionService.getQuestions(limit, lastEvaluatedKey, search);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<QuestionDto>> getQuestionById(@PathVariable String id) {
    QuestionDto q = questionService.getQuestionById(id);
    return ResponseEntity.ok(ApiResponse.success(q));
  }

  @PostMapping("/uploads/presigned-url")
  public ResponseEntity<ApiResponse<UploadUrlResponse>> generateUploadUrl(
      @RequestParam(required = false) String ext) {

    UploadUrlResponse s3Info = storageService.generateUploadUrl(ext);
    return ResponseEntity.ok(ApiResponse.success(s3Info));
  }

  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<QuestionDto>> updateQuestion(
      @PathVariable String id,
      @Valid @RequestBody UpdateQuestionRequest req) {

    QuestionDto updatedQuestion = questionService.updateQuestion(id, req);
    return ResponseEntity.ok(ApiResponse.success(updatedQuestion, "Question updated successfully"));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> deleteQuestion(
      @PathVariable String id) {

    questionService.deleteQuestion(id);

    return ResponseEntity.ok(ApiResponse.success(null, "Question deleted successfully"));
  }

}
