package com.vocabkicker.controller;

import com.vocabkicker.dto.response.QuestionPageResponse;
import com.vocabkicker.dto.request.UpdateQuestionRequest;
import com.vocabkicker.entity.Question;
import com.vocabkicker.repository.QuestionPage;
import com.vocabkicker.service.QuestionService;
import com.vocabkicker.service.StorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
  public ResponseEntity<QuestionPageResponse> getQuestions(
      @RequestParam(required = false, defaultValue = "10") int limit,
      @RequestParam(required = false) String lastEvaluatedKey,
      @RequestParam(required = false) String search) {

    QuestionPage page = questionService.getQuestions(limit, lastEvaluatedKey, search);

    return ResponseEntity.ok(new QuestionPageResponse(page.getItems(), page.getLastEvaluatedKey()));
  }

  @GetMapping("/{id}")
  public ResponseEntity<Question> getQuestionById(@PathVariable String id) {
    return ResponseEntity.ok(questionService.getQuestionById(id));
  }

  @PostMapping("/upload-requests")
  public ResponseEntity<Map<String, String>> generateUploadUrl(
      @RequestParam(required = false) String ext) {

    Map<String, String> s3Info = storageService.generateUploadUrl(ext);
    return ResponseEntity.ok(s3Info);
  }

  @PutMapping("/{id}")
  public ResponseEntity<Question> updateQuestion(
      @PathVariable String id,
      @RequestBody UpdateQuestionRequest req) {

    Question updatedFields = new Question();
    updatedFields.setWord(req.getWord());
    updatedFields.setDefinition(req.getDefinition());
    updatedFields.setMnemonic(req.getMnemonic());

    Question existingQuestion = questionService.updateQuestion(id, updatedFields);
    return ResponseEntity.ok(existingQuestion);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteQuestion(
      @PathVariable String id) {

    questionService.deleteQuestion(id);

    return ResponseEntity.noContent().build();
  }
}
