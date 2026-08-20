package com.adnibog.gamecenter.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

import com.adnibog.gamecenter.dto.response.ApiResponse;
import com.adnibog.gamecenter.dto.response.UploadUrlResponse;
import com.adnibog.gamecenter.dto.model.UploadJobDto;
import com.adnibog.gamecenter.service.UploadService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Uploads", description = "Endpoints for generating presigned S3 URLs for bulk imports")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/projects/{projectId}/folders/{folderId}/uploads")
public class UploadController {
  private final UploadService uploadService;

  public UploadController(UploadService uploadService) {
    this.uploadService = uploadService;
  }

  @Operation(summary = "Generate Presigned URL", description = "Generates an S3 presigned URL for uploading .csv or .docx files.")
  @PostMapping("/presigned-url")
  public ResponseEntity<ApiResponse<UploadUrlResponse>> generateUploadUrl(
      @PathVariable String projectId,
      @PathVariable String folderId,
      @RequestParam(required = false) String ext) {

    UploadUrlResponse s3Info = uploadService.generateUploadUrl(projectId, folderId, ext);
    return ResponseEntity.ok(ApiResponse.success(s3Info));
  }

  @Operation(summary = "Get Upload Status", description = "Gets the status of a bulk upload job.")
  @GetMapping("/{key}/status")
  public ResponseEntity<ApiResponse<UploadJobDto>> getUploadStatus(
      @PathVariable String projectId,
      @PathVariable String folderId,
      @PathVariable String key) {

    String fullKey = projectId + "/" + folderId + "/" + key;
    UploadJobDto dto = uploadService.getJob(fullKey);
    return ResponseEntity.ok(ApiResponse.success(dto));
  }
}
