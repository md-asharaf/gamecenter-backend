package com.adnibog.vocabkicker.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.adnibog.vocabkicker.dto.response.ApiResponse;
import com.adnibog.vocabkicker.dto.response.QuizQuestion;
import com.adnibog.vocabkicker.service.QuestionService;

import java.util.List;

@RestController
@RequestMapping("/quiz")
public class QuizController {

  private final QuestionService questionService;

  public QuizController(QuestionService questionService) {
    this.questionService = questionService;
  }

  @GetMapping
  public ResponseEntity<ApiResponse<List<QuizQuestion>>> generateQuiz(
      @RequestParam(name = "count", required = false, defaultValue = "10") int count) {

    List<QuizQuestion> quiz = questionService.generateQuiz(count);
    return ResponseEntity.ok(ApiResponse.success(quiz));
  }
}
