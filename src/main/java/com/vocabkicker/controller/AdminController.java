package com.vocabkicker.controller;

import com.vocabkicker.dto.request.RegisterAdminRequest;
import com.vocabkicker.dto.request.UpdateAdminRequest;
import com.vocabkicker.dto.response.UserDto;
import com.vocabkicker.exception.BadRequestException;
import com.vocabkicker.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
@RequestMapping("/admins")
public class AdminController {

  private final AuthService authService;

  public AdminController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping
  public ResponseEntity<Void> register(@RequestBody RegisterAdminRequest req) {
    authService.createAdmin(req.getEmail(), req.getPassword());
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @GetMapping
  public ResponseEntity<List<UserDto>> listAdmins(@RequestAttribute("adminId") String currentAdminId) {
    List<UserDto> admins = authService.getAllAdmins().stream()
        .filter(admin -> !admin.getId().equals(currentAdminId))
        .toList();
    return ResponseEntity.ok(admins);
  }

  @PutMapping("/{id}")
  public ResponseEntity<Void> updateAdmin(@PathVariable String id, @RequestBody UpdateAdminRequest req) {
    authService.updateAdmin(id, req.getEmail(), req.getPassword());
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteAdmin(@RequestAttribute("adminId") String currentAdminId, @PathVariable String id) {
    if (currentAdminId.equals(id)) {
      throw new BadRequestException("Cannot delete yourself");
    }
    authService.deleteAdmin(id);
    return ResponseEntity.noContent().build();
  }

}
