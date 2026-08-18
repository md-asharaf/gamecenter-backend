package com.vocabkicker.serverless.controller;

import com.vocabkicker.serverless.dto.LoginRequest;
import com.vocabkicker.serverless.dto.UpdateAdminRequest;
import com.vocabkicker.serverless.dto.UserDto;
import com.vocabkicker.serverless.exception.BadRequestException;
import com.vocabkicker.serverless.service.AuthService;
import com.vocabkicker.serverless.service.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admins")
public class AdminController {

  private final AuthService authService;
  private final JwtService jwtService;

  public AdminController(AuthService authService, JwtService jwtService) {
    this.authService = authService;
    this.jwtService = jwtService;
  }

  @PostMapping
  public ResponseEntity<Map<String, String>> register(@RequestHeader Map<String, String> headers,
      @RequestBody LoginRequest req) {
    if (authService.getAdminCount() > 0) {
      jwtService.validateAdminToken(headers);
    }
    authService.createAdmin(req.getEmail(), req.getPassword());
    return ResponseEntity.ok(Map.of("message", "Admin created successfully"));
  }

  @GetMapping
  public ResponseEntity<List<UserDto>> listAdmins(HttpServletRequest request) {
    String currentAdminId = (String) request.getAttribute("adminId");
    List<UserDto> admins = authService.getAllAdmins().stream()
        .filter(admin -> !admin.getId().equals(currentAdminId))
        .toList();
    return ResponseEntity.ok(admins);
  }

  @PutMapping("/{id}")
  public ResponseEntity<Map<String, String>> updateAdmin(@PathVariable String id, @RequestBody UpdateAdminRequest req) {
    authService.updateAdmin(id, req.getEmail(), req.getPassword());

    return ResponseEntity.ok(Map.of("message", "Admin updated successfully"));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Map<String, String>> deleteAdmin(HttpServletRequest request, @PathVariable String id) {
    String currentAdminId = (String) request.getAttribute("adminId");

    if (currentAdminId.equals(id)) {
      throw new BadRequestException("Cannot delete yourself");
    }

    authService.deleteAdmin(id);

    return ResponseEntity.ok(Map.of("message", "Admin deleted successfully"));
  }

}
