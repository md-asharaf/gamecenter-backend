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
import com.adnibog.gamecenter.dto.response.UploadJobDto;
import com.adnibog.gamecenter.service.StorageService;
import com.adnibog.gamecenter.entity.UploadJob;
import com.adnibog.gamecenter.service.UploadJobService;
import com.adnibog.gamecenter.exception.NotFoundException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Uploads", description = "Endpoints for generating presigned S3 URLs for bulk imports")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/projects/{projectId}/uploads")
public class UploadController {
  private final StorageService storageService;
  private final UploadJobService uploadJobService;

  public UploadController(StorageService storageService, UploadJobService uploadJobService) {
    this.storageService = storageService;
    this.uploadJobService = uploadJobService;
  }

  @Operation(summary = "Generate Presigned URL", description = "Generates an S3 presigned URL for uploading .csv or .docx files.")
  @PostMapping("/presigned-url")
  public ResponseEntity<ApiResponse<UploadUrlResponse>> generateUploadUrl(
      @PathVariable String projectId,
      @RequestParam(required = false) String ext) {

    UploadUrlResponse s3Info = storageService.generateUploadUrl(projectId, ext);
    return ResponseEntity.ok(ApiResponse.success(s3Info));
  }
  
  @Operation(summary = "Get Upload Status", description = "Gets the status of a bulk upload job.")
  @GetMapping("/{key}/status")
  public ResponseEntity<ApiResponse<UploadJobDto>> getUploadStatus(
      @PathVariable String projectId,
      @PathVariable String key) {
      
    String fullKey = projectId + "/" + key;
    UploadJob job = uploadJobService.getJob(fullKey)
        .orElseThrow(() -> new NotFoundException("Upload job not found for key: " + fullKey));
    UploadJobDto dto = new UploadJobDto(job.getId(), job.getProjectId(), job.getStatus(), job.getErrorMessage());
    return ResponseEntity.ok(ApiResponse.success(dto));
  }
}
