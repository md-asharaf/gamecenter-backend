package com.adnibog.gamecenter.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.adnibog.gamecenter.dto.request.RegisterAdminRequest;
import com.adnibog.gamecenter.dto.request.UpdateAdminRequest;
import com.adnibog.gamecenter.dto.response.ApiResponse;
import com.adnibog.gamecenter.dto.response.UserDto;
import com.adnibog.gamecenter.dto.response.UserPageResponse;

import com.adnibog.gamecenter.service.UserService;

import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;

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

  @GetMapping("/me")
  public ResponseEntity<ApiResponse<UserDto>> getMe(@RequestAttribute("adminId") String currentAdminId) {
    UserDto me = userService.getAdminById(currentAdminId);
    return ResponseEntity.ok(ApiResponse.success(me));
  }

  @GetMapping
  public ResponseEntity<ApiResponse<UserPageResponse>> listAdmins(
      @RequestAttribute("adminId") String currentAdminId,
      @RequestParam(defaultValue = "10") int limit,
      @RequestParam(required = false) String lastEvaluatedKey,
      @RequestParam(required = false) String search) {
    UserPageResponse admins = userService.getAllAdmins(currentAdminId, limit, lastEvaluatedKey, search);
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
