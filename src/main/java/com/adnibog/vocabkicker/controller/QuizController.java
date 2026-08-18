package com.adnibog.vocabkicker.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.adnibog.vocabkicker.dto.response.ApiResponse;
import com.adnibog.vocabkicker.dto.response.QuizQuestion;
import com.adnibog.vocabkicker.service.QuestionService;

import java.util.List;

@RestController
@RequestMapping("/projects/{projectId}/quiz")
public class QuizController {

  private final QuestionService questionService;

  public QuizController(QuestionService questionService) {
    this.questionService = questionService;
  }

  @GetMapping
  public ResponseEntity<ApiResponse<List<QuizQuestion>>> generateQuiz(
      @PathVariable String projectId) {
    List<QuizQuestion> quiz = questionService.generateQuiz(projectId);
    return ResponseEntity.ok(ApiResponse.success(quiz));
  }
}
