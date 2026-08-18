package com.adnibog.vocabkicker.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.adnibog.vocabkicker.dto.response.ApiResponse;
import com.adnibog.vocabkicker.dto.response.UploadUrlResponse;
import com.adnibog.vocabkicker.service.StorageService;

@RestController
@RequestMapping("/projects/{projectId}/uploads")
public class UploadController {
  private final StorageService storageService;

  public UploadController(StorageService storageService) {
    this.storageService = storageService;
  }

  @PostMapping("/presigned-url")
  public ResponseEntity<ApiResponse<UploadUrlResponse>> generateUploadUrl(
      @PathVariable String projectId,
      @RequestParam(required = false) String ext) {

    UploadUrlResponse s3Info = storageService.generateUploadUrl(projectId, ext);
    return ResponseEntity.ok(ApiResponse.success(s3Info));
  }

}
