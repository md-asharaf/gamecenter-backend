package com.adnibog.gamecenter.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.adnibog.gamecenter.dto.response.ApiError;

import org.springframework.lang.NonNull;

import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<ApiError> handleUnauthorized(UnauthorizedException ex) {
    log.warn("Unauthorized request: {}", ex.getMessage());
    return buildErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
  }

  @ExceptionHandler(ForbiddenException.class)
  public ResponseEntity<ApiError> handleForbidden(ForbiddenException ex) {
    log.warn("Access forbidden: {}", ex.getMessage());
    return buildErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage());
  }

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<ApiError> handleNotFound(NotFoundException ex) {
    log.warn("Resource not found: {}", ex.getMessage());
    return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
  }

  @ExceptionHandler(ConflictException.class)
  public ResponseEntity<ApiError> handleConflict(ConflictException ex) {
    log.warn("Conflict error: {}", ex.getMessage());
    return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage());
  }

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<ApiError> handleBadRequest(BadRequestException ex) {
    log.warn("Bad request: {}", ex.getMessage());
    return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiError> handleValidationExceptions(MethodArgumentNotValidException ex) {
    List<String> errors = ex.getBindingResult().getFieldErrors().stream()
        .map(error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Invalid field")
        .collect(Collectors.toList());

    ex.getBindingResult().getGlobalErrors().forEach(error -> {
      if (error.getDefaultMessage() != null) {
        errors.add(error.getDefaultMessage());
      }
    });

    ApiError apiError = ApiError.failure("Validation failed", errors);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiError> handleGeneralException(Exception ex) {
    log.error("Unhandled internal server error occurred", ex);
    return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An internal server error occurred");
  }

  private ResponseEntity<ApiError> buildErrorResponse(@NonNull HttpStatus status, String message) {
    return ResponseEntity.status(status).body(ApiError.failure(message));
  }
}
