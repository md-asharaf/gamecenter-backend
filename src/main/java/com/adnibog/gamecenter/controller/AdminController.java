package com.adnibog.gamecenter.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.adnibog.gamecenter.dto.request.RegisterAdminRequest;
import com.adnibog.gamecenter.dto.request.UpdateAdminRequest;
import com.adnibog.gamecenter.dto.response.ApiResponse;
import com.adnibog.gamecenter.dto.response.UserDto;
import com.adnibog.gamecenter.service.UserService;

import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admins")
public class AdminController {

  private final UserService userService;

  public AdminController(UserService userService) {
    this.userService = userService;
  }

  @PostMapping
  public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterAdminRequest req) {
    userService.createAdmin(req.getEmail(), req.getPassword(), req.getProjectIds());
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(null, "Admin created successfully"));
  }

  @GetMapping
  public ResponseEntity<ApiResponse<List<UserDto>>> listAdmins(@RequestAttribute("adminId") String currentAdminId) {
    List<UserDto> admins = userService.getAllAdmins().stream()
        .filter(admin -> !admin.getId().equals(currentAdminId))
        .collect(Collectors.toList());
    return ResponseEntity.ok(ApiResponse.success(admins));
  }

  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<UserDto>> updateAdmin(
      @PathVariable String id,
      @Valid @RequestBody UpdateAdminRequest req) {

    UserDto updatedUser = userService.updateAdmin(id, req.getEmail(), req.getPassword(), req.getProjectIds());
    return ResponseEntity.ok(ApiResponse.success(updatedUser, "Admin updated successfully"));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> deleteAdmin(@RequestAttribute("adminId") String currentAdminId,
      @PathVariable String id) {
    userService.deleteAdmin(currentAdminId, id);
    return ResponseEntity.ok(ApiResponse.success(null, "Admin deleted successfully"));
  }

}
