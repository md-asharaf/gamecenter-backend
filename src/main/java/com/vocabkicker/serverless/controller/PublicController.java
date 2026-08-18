package com.vocabkicker.serverless.controller;

import com.vocabkicker.serverless.dto.AuthResult;
import com.vocabkicker.serverless.dto.LoginRequest;
import com.vocabkicker.serverless.dto.RefreshRequest;
import com.vocabkicker.serverless.dto.QuizQuestion;
import com.vocabkicker.serverless.exception.BadRequestException;
import com.vocabkicker.serverless.exception.UnauthorizedException;
import com.vocabkicker.serverless.service.AuthService;
import com.vocabkicker.serverless.service.QuestionService;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/public")
public class PublicController {

  private final AuthService authService;
  private final QuestionService questionService;

  public PublicController(AuthService authService, QuestionService questionService) {
    this.authService = authService;
    this.questionService = questionService;
  }

  @PostMapping("/login")
  public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest req) {
    AuthResult authResult = authService.login(req.getEmail(), req.getPassword());
    return buildAuthResponse(authResult, "Login successful");
  }

  @GetMapping("/quiz")
  public ResponseEntity<List<QuizQuestion>> generateQuiz(
      @RequestParam(name = "count", required = false, defaultValue = "10") int count) {

    List<QuizQuestion> quiz = questionService.generateQuiz(count);
    return ResponseEntity.ok(quiz);
  }

  @PostMapping("/refresh")
  public ResponseEntity<Map<String, String>> refresh(
      @CookieValue(name = "refresh_token", required = false) String refreshTokenCookie,
      @RequestBody(required = false) RefreshRequest req) {

    if (req == null || req.getRefreshToken() == null) {
      if (refreshTokenCookie != null && !refreshTokenCookie.isEmpty()) {
        req = new RefreshRequest();
        req.setRefreshToken(refreshTokenCookie);
      } else {
        throw new BadRequestException("Missing refresh token in body or cookies");
      }
    }

    AuthResult authResult;
    try {
      authResult = authService.refresh(req.getRefreshToken());
    } catch (Exception e) {
      throw new UnauthorizedException("Invalid or expired refresh token: " + e.getMessage());
    }

    return buildAuthResponse(authResult, "Token refreshed");
  }

  private ResponseEntity<Map<String, String>> buildAuthResponse(AuthResult authResult, String message) {
    final String accessCookie = String.format(
        "admin_token=%s; Path=/; HttpOnly; Max-Age=%d; SameSite=None; Secure",
        authResult.getAccessToken(), 15 * 60);
    final String refreshCookie = String.format(
        "refresh_token=%s; Path=/; HttpOnly; Max-Age=%d; SameSite=None; Secure",
        authResult.getRefreshToken(), 7 * 24 * 60 * 60);

    HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.add(HttpHeaders.SET_COOKIE, accessCookie);
    responseHeaders.add(HttpHeaders.SET_COOKIE, refreshCookie);

    return ResponseEntity.ok()
        .headers(responseHeaders)
        .body(Map.of(
            "message", message,
            "refreshToken", authResult.getRefreshToken()));
  }
}
