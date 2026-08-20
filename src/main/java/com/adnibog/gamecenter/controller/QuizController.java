package com.adnibog.gamecenter.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.adnibog.gamecenter.dto.response.ApiResponse;
import com.adnibog.gamecenter.dto.model.QuizQuestion;
import com.adnibog.gamecenter.service.QuizService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "Quiz", description = "Endpoint for generating a randomized quiz from project questions")
@RestController
@RequestMapping("/projects/{projectId}/quiz")
public class QuizController {

  private final QuizService quizService;

  public QuizController(QuizService quizService) {
    this.quizService = quizService;
  }

  @Operation(summary = "Generate Quiz", description = "Generates a randomized set of questions for a given project. Requires admin authentication and project access.")
  @GetMapping
  public ResponseEntity<ApiResponse<List<QuizQuestion>>> generateQuiz(
      @PathVariable String projectId) {
    List<QuizQuestion> quiz = quizService.generateQuiz(projectId);
    return ResponseEntity.ok(ApiResponse.success(quiz));
  }
}
