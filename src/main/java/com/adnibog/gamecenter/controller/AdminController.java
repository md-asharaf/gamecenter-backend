package com.adnibog.gamecenter.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.adnibog.gamecenter.dto.request.RegisterAdminRequest;
import com.adnibog.gamecenter.dto.request.UpdateAdminRequest;
import com.adnibog.gamecenter.dto.request.ChangePasswordRequest;
import com.adnibog.gamecenter.dto.response.ApiResponse;
import com.adnibog.gamecenter.dto.model.UserDto;
import com.adnibog.gamecenter.dto.response.UserPageResponse;

import com.adnibog.gamecenter.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;

@Tag(name = "Admins", description = "Endpoints for managing admin users")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/admins")
public class AdminController {

  private final UserService userService;

  public AdminController(UserService userService) {
    this.userService = userService;
  }

  @Operation(summary = "Register Admin", description = "Creates a new sub-admin. Requires SUPER_ADMIN role.")
  @PostMapping
  public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterAdminRequest req) {
    userService.createAdmin(req.getEmail(), req.getPassword(), req.getProjectIds());
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(null, "Sub-admin created successfully."));
  }

  @Operation(summary = "Get Current Admin", description = "Returns the profile of the currently authenticated admin. Accessible to all admin roles.")
  @GetMapping("/me")
  public ResponseEntity<ApiResponse<UserDto>> getMe(@RequestAttribute("adminId") String currentAdminId) {
    UserDto me = userService.getAdminById(currentAdminId);
    return ResponseEntity.ok(ApiResponse.success(me));
  }

  @Operation(summary = "Change Password", description = "Updates the currently authenticated admin's password.")
  @PutMapping("/me/password")
  public ResponseEntity<ApiResponse<Void>> changePassword(
      @RequestAttribute("adminId") String currentAdminId,
      @Valid @RequestBody ChangePasswordRequest req) {
    userService.updatePassword(currentAdminId, req.getPassword());
    return ResponseEntity.ok(ApiResponse.success(null, "Password updated successfully."));
  }

  @Operation(summary = "List Admins", description = "Returns a paginated list of all admins. Requires SUPER_ADMIN role.")
  @GetMapping
  public ResponseEntity<ApiResponse<UserPageResponse>> listAdmins(
      @RequestAttribute("adminId") String currentAdminId,
      @RequestParam(defaultValue = "10") int limit,
      @RequestParam(required = false) String lastEvaluatedKey,
      @RequestParam(required = false) String search) {
    UserPageResponse admins = userService.getAllAdmins(currentAdminId, limit, lastEvaluatedKey, search);
    return ResponseEntity.ok(ApiResponse.success(admins));
  }

  @Operation(summary = "Update Admin", description = "Updates an admin's email, password, or project access. Requires SUPER_ADMIN role.")
  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<UserDto>> updateAdmin(
      @RequestAttribute("adminId") String currentAdminId,
      @PathVariable String id,
      @Valid @RequestBody UpdateAdminRequest req) {
    UserDto updatedUser = userService.updateAdmin(currentAdminId, id, req.getEmail(), req.getPassword(),
        req.getProjectIds());
    return ResponseEntity.ok(ApiResponse.success(updatedUser, "Sub-admin updated successfully."));
  }
        

  @Operation(summary = "Delete Admin", description = "Deletes an admin by ID. Cannot delete yourself. Requires SUPER_ADMIN role.")
  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> deleteAdmin(
      @RequestAttribute("adminId") String currentAdminId,
      @PathVariable String id) {
    userService.deleteAdmin(currentAdminId, id);
    return ResponseEntity.ok(ApiResponse.success(null, "Admin deleted successfully"));
  }
}
