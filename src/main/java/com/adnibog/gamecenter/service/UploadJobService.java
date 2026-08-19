package com.adnibog.gamecenter.service;

import org.springframework.stereotype.Service;
import com.adnibog.gamecenter.entity.UploadJob;
import com.adnibog.gamecenter.repository.UploadJobRepository;
import lombok.extern.slf4j.Slf4j;
import java.util.Optional;

@Slf4j
@Service
public class UploadJobService {
  private final UploadJobRepository repository;

  public UploadJobService(UploadJobRepository repository) {
    this.repository = repository;
  }

  public void createJob(String id, String projectId) {
    UploadJob job = new UploadJob();
    job.setId(id);
    job.setProjectId(projectId);
    job.setStatus("PENDING");
    long now = System.currentTimeMillis();
    job.setCreatedAt(now);
    job.setUpdatedAt(now);
    repository.save(job);
    log.info("Created UploadJob {} for project {}", id, projectId);
  }

  public void updateJobStatus(String id, String status, String errorMessage) {
    Optional<UploadJob> opt = repository.findById(id);
    if (opt.isPresent()) {
      UploadJob job = opt.get();
      job.setStatus(status);
      job.setErrorMessage(errorMessage);
      job.setUpdatedAt(System.currentTimeMillis());
      repository.save(job);
      log.info("Updated UploadJob {} to status {}", id, status);
    } else {
      log.warn("UploadJob {} not found to update status to {}", id, status);
    }
  }

  public Optional<UploadJob> getJob(String id) {
    return repository.findById(id);
  }
}
