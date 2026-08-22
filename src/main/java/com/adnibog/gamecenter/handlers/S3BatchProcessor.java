package com.adnibog.gamecenter.handlers;

import com.adnibog.gamecenter.entity.Project;
import com.adnibog.gamecenter.entity.Question;
import com.adnibog.gamecenter.service.ProjectService;
import com.adnibog.gamecenter.service.QuestionService;
import com.adnibog.gamecenter.service.storage.StorageService;
import com.adnibog.gamecenter.service.UploadJobService;
import com.adnibog.gamecenter.service.parser.QuestionParser;
import com.adnibog.gamecenter.service.parser.QuestionParserFactory;
import com.amazonaws.services.lambda.runtime.events.S3Event;

import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class S3BatchProcessor {

  private final QuestionService questionService;
  private final QuestionParserFactory parserFactory;
  private final StorageService storageService;
  private final UploadJobService uploadJobService;
  private final ProjectService projectService;

  public S3BatchProcessor(QuestionService questionService, QuestionParserFactory parserFactory,
      StorageService storageService, UploadJobService uploadJobService, ProjectService projectService) {
    this.uploadJobService = uploadJobService;
    this.questionService = questionService;
    this.parserFactory = parserFactory;
    this.storageService = storageService;
    this.projectService = projectService;
  }

  public String process(S3Event s3Event) {
    s3Event.getRecords().forEach(record -> {
      String bucket = record.getS3().getBucket().getName();
      String key = record.getS3().getObject().getKey();

      String projectId = "default";
      String folderId = "default";
      String[] parts = key.split("/");
      if (parts.length >= 3) {
        projectId = parts[0];
        folderId = parts[1];
      } else if (parts.length == 2) {
        projectId = parts[0];
      }

      log.info("Processing file from S3: bucket={}, key={}, projectId={}, folderId={}", bucket, key, projectId, folderId);
      uploadJobService.updateJobStatus(key, "PROCESSING", null);

      try (InputStream s3Stream = storageService.getFileStream(bucket, key)) {

        Project project = null;
        if (!"default".equals(projectId)) {
          try {
            project = projectService.getProjectEntityById(projectId);
          } catch (Exception e) {
            log.warn("Project not found for id: {}", projectId);
          }
        }

        List<Question> questions = new ArrayList<>();
        Optional<QuestionParser> parserOpt = parserFactory.getParser(key);

        if (parserOpt.isPresent()) {
          questions = parserOpt.get().parse(s3Stream, project);
        } else {
          throw new IllegalArgumentException("Unsupported file extension for key: " + key);
        }

        if (questions.isEmpty()) {
          throw new IllegalArgumentException("File contains 0 valid questions. Please check the file format and headers.");
        }

        log.info("Parsed {} questions. Saving to database...", questions.size());

        questionService.saveQuestionsBatch(projectId, folderId, questions);

        log.info("Successfully saved {} questions.", questions.size());
        uploadJobService.updateJobStatus(key, "COMPLETED", questions.size() + " questions imported successfully.");

      } catch (Exception e) {
        log.error("Error processing file {} from bucket {}", key, bucket, e);
        String errorMsg = e.getMessage();
        if (errorMsg != null && errorMsg.length() > 150) {
          errorMsg = errorMsg.substring(0, 150) + "...";
        }
        uploadJobService.updateJobStatus(key, "FAILED", errorMsg);
        throw new RuntimeException(e);
      }
    });

    return "Success";
  }
}
