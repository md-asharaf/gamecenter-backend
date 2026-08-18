package com.adnibog.vocabkicker.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.adnibog.vocabkicker.dto.request.LoginRequest;
import com.adnibog.vocabkicker.dto.request.RefreshRequest;
import com.adnibog.vocabkicker.dto.response.ApiResponse;
import com.adnibog.vocabkicker.dto.response.AuthResult;
import com.adnibog.vocabkicker.dto.response.LoginResponse;
import com.adnibog.vocabkicker.service.AuthService;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Authentication", description = "Endpoints for admin login and token management")
@RestController
@RequestMapping("/auth")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @Operation(summary = "Admin Login", description = "Authenticates an admin and returns access and refresh tokens via cookies.")
  @PostMapping("/login")
  public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest req) {
    AuthResult authResult = authService.login(req.getEmail(), req.getPassword());
    return buildAuthResponse(authResult, "Login successful");
  }

  @Operation(summary = "Refresh Token", description = "Refreshes the admin access token using a valid refresh token.")
  @PostMapping("/refresh")
  public ResponseEntity<ApiResponse<LoginResponse>> refresh(
      @CookieValue(name = "refresh_token", required = false) String refreshTokenCookie,
      @RequestBody(required = false) RefreshRequest req) {

    String token = (req != null && req.getRefreshToken() != null) ? req.getRefreshToken() : refreshTokenCookie;

    AuthResult authResult = authService.refresh(token);
    return buildAuthResponse(authResult, "Token refreshed");
  }

  private ResponseEntity<ApiResponse<LoginResponse>> buildAuthResponse(AuthResult authResult, String message) {
    ResponseCookie accessCookie = ResponseCookie
        .from("admin_token", java.util.Objects.requireNonNull(authResult.getAccessToken()))
        .httpOnly(true)
        .secure(true)
        .path("/")
        .maxAge(15 * 60)
        .sameSite("None")
        .build();

    ResponseCookie refreshCookie = ResponseCookie
        .from("refresh_token", java.util.Objects.requireNonNull(authResult.getRefreshToken()))
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
        .body(ApiResponse.success(new LoginResponse(message, authResult.getRefreshToken()), message));
  }
}
