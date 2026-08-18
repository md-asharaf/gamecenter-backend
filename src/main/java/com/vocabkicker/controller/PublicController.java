package com.vocabkicker.controller;

import com.vocabkicker.dto.response.AuthResult;
import com.vocabkicker.dto.request.LoginRequest;
import com.vocabkicker.dto.request.RefreshRequest;
import com.vocabkicker.dto.response.QuizQuestion;
import com.vocabkicker.exception.BadRequestException;
import com.vocabkicker.exception.UnauthorizedException;
import com.vocabkicker.service.AuthService;
import com.vocabkicker.service.QuestionService;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
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
    ResponseCookie accessCookie = ResponseCookie.from("admin_token", java.util.Objects.requireNonNull(authResult.getAccessToken()))
        .httpOnly(true)
        .secure(true)
        .path("/")
        .maxAge(15 * 60)
        .sameSite("None")
        .build();

    ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", java.util.Objects.requireNonNull(authResult.getRefreshToken()))
        .httpOnly(true)
        .secure(true)
        .path("/")
        .maxAge(7 * 24 * 60 * 60)
        .sameSite("None")
        .build();

    HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.add(HttpHeaders.SET_COOKIE, accessCookie.toString());
    responseHeaders.add(HttpHeaders.SET_COOKIE, refreshCookie.toString());

    return ResponseEntity.ok()
        .headers(responseHeaders)
        .body(Map.of(
            "message", message,
            "refreshToken", authResult.getRefreshToken()));
  }
}
