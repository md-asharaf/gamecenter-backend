package com.vocabkicker.serverless.controller;

import com.vocabkicker.serverless.dto.QuizQuestion;
import com.vocabkicker.serverless.entity.Question;
import com.vocabkicker.serverless.exception.NotFoundException;
import com.vocabkicker.serverless.repository.QuestionPage;
import com.vocabkicker.serverless.service.QuestionService;
import com.vocabkicker.serverless.service.StorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
  public ResponseEntity<Map<String, Object>> getQuestions(
      @RequestParam(required = false, defaultValue = "10") int limit,
      @RequestParam(required = false) String lastEvaluatedKey,
      @RequestParam(required = false) String search) {

    QuestionPage page = questionService.getQuestions(limit, lastEvaluatedKey, search);

    return ResponseEntity.ok(Map.of(
        "items", page.getItems(),
        "lastEvaluatedKey", page.getLastEvaluatedKey()));
  }

  @GetMapping("/{id}")
  public ResponseEntity<Question> getQuestionById(@PathVariable String id) {
    return ResponseEntity.ok(questionService.getQuestionById(id));
  }

  @GetMapping("/upload-url")
  public ResponseEntity<Map<String, String>> generateUploadUrl(
      @RequestParam(required = false) String ext) {

    Map<String, String> s3Info = storageService.generateUploadUrl(ext);
    return ResponseEntity.ok(s3Info);
  }

  @PutMapping("/{id}")
  public ResponseEntity<Question> updateQuestion(
      @PathVariable String id,
      @RequestBody Question updatedFields) {

    Question existingQuestion = questionService.updateQuestion(id, updatedFields);
    return ResponseEntity.ok(existingQuestion);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Map<String, String>> deleteQuestion(
      @PathVariable String id) {

    questionService.deleteQuestion(id);

    return ResponseEntity.ok(Map.of("message", "Deleted"));
  }
}
