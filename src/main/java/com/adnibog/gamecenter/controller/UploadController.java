package com.adnibog.gamecenter.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.adnibog.gamecenter.dto.response.ApiResponse;
import com.adnibog.gamecenter.dto.response.UploadUrlResponse;
import com.adnibog.gamecenter.service.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Uploads", description = "Endpoints for generating presigned S3 URLs for bulk imports")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/projects/{projectId}/uploads")
public class UploadController {
  private final StorageService storageService;

  public UploadController(StorageService storageService) {
    this.storageService = storageService;
  }

  @Operation(summary = "Generate Presigned URL", description = "Generates an S3 presigned URL for uploading .csv or .docx files.")
  @PostMapping("/presigned-url")
  public ResponseEntity<ApiResponse<UploadUrlResponse>> generateUploadUrl(
      @PathVariable String projectId,
      @RequestParam(required = false) String ext) {

    UploadUrlResponse s3Info = storageService.generateUploadUrl(projectId, ext);
    return ResponseEntity.ok(ApiResponse.success(s3Info));
  }

}
