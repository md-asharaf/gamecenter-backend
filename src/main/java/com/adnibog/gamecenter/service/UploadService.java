package com.adnibog.gamecenter.service;

import org.springframework.stereotype.Service;
import com.adnibog.gamecenter.dto.response.UploadUrlResponse;
import com.adnibog.gamecenter.dto.model.UploadJobDto;
import com.adnibog.gamecenter.entity.UploadJob;
import com.adnibog.gamecenter.service.storage.StorageService;
import com.adnibog.gamecenter.exception.NotFoundException;

@Service
public class UploadService {
  private final StorageService storageService;
  private final UploadJobService uploadJobService;

  public UploadService(StorageService storageService, UploadJobService uploadJobService) {
    this.storageService = storageService;
    this.uploadJobService = uploadJobService;
  }

  public UploadUrlResponse generateUploadUrl(String projectId, String folderId, String ext) {
    return storageService.generateUploadUrl(projectId, folderId, ext);
  }

  public UploadJobDto getJob(String fullKey) {
    UploadJob job = uploadJobService.getJob(fullKey)
        .orElseThrow(() -> new NotFoundException("Upload job not found for key: " + fullKey));
    return new UploadJobDto(job.getId(), job.getProjectId(), job.getStatus(), job.getErrorMessage());
  }
}
