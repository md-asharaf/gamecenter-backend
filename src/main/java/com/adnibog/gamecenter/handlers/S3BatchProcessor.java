package com.adnibog.gamecenter.handlers;

import com.adnibog.gamecenter.entity.Question;
import com.adnibog.gamecenter.service.QuestionService;
import com.adnibog.gamecenter.service.StorageService;
import com.adnibog.gamecenter.service.UploadJobService;
import com.adnibog.gamecenter.service.parser.QuestionParser;
import com.adnibog.gamecenter.service.parser.QuestionParserFactory;
import com.amazonaws.services.lambda.runtime.events.S3Event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class S3BatchProcessor {

  private static final Logger logger = LoggerFactory.getLogger(S3BatchProcessor.class);

  private final QuestionService questionService;
  private final QuestionParserFactory parserFactory;
  private final StorageService storageService;
  private final UploadJobService uploadJobService;

  public S3BatchProcessor(QuestionService questionService, QuestionParserFactory parserFactory,
      StorageService storageService, UploadJobService uploadJobService) {
    this.uploadJobService = uploadJobService;
    this.questionService = questionService;
    this.parserFactory = parserFactory;
    this.storageService = storageService;
  }

  public String process(S3Event s3Event) {
    s3Event.getRecords().forEach(record -> {
      String bucket = record.getS3().getBucket().getName();
      String key = record.getS3().getObject().getKey();

      String projectId = "default";
      if (key.contains("/")) {
        projectId = key.substring(0, key.indexOf("/"));
      }

      logger.info("Processing file from S3: bucket={}, key={}, projectId={}", bucket, key, projectId);
      uploadJobService.updateJobStatus(key, "PROCESSING", null);

      try (InputStream s3Stream = storageService.getFileStream(bucket, key)) {

        List<Question> questions = new ArrayList<>();
        Optional<QuestionParser> parserOpt = parserFactory.getParser(key);

        if (parserOpt.isPresent()) {
          questions = parserOpt.get().parse(s3Stream);
        } else {
          throw new IllegalArgumentException("Unsupported file extension for key: " + key);
        }

        logger.info("Parsed {} questions. Saving to database...", questions.size());

        for (Question q : questions) {
          q.setProjectId(projectId);
          questionService.saveQuestion(projectId, q);
        }

        logger.info("Successfully saved {} questions.", questions.size());
        uploadJobService.updateJobStatus(key, "COMPLETED", null);

      } catch (Exception e) {
        logger.error("Error processing file {} from bucket {}", key, bucket, e);
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
