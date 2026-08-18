package com.vocabkicker.serverless.service;

import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.vocabkicker.serverless.entity.Question;
import com.vocabkicker.serverless.service.parser.QuestionParser;
import com.vocabkicker.serverless.service.parser.QuestionParserFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class S3BatchProcessor {

  private static final Logger logger = LoggerFactory.getLogger(S3BatchProcessor.class);

  private final QuestionService questionService;
  private final QuestionParserFactory parserFactory;
  private final StorageService storageService;

  public S3BatchProcessor(QuestionService questionService, QuestionParserFactory parserFactory, StorageService storageService) {
    this.questionService = questionService;
    this.parserFactory = parserFactory;
    this.storageService = storageService;
  }

  public String process(S3Event s3Event) {
    s3Event.getRecords().forEach(record -> {
      String bucket = record.getS3().getBucket().getName();
      String key = record.getS3().getObject().getKey();

      logger.info("Processing file from S3: bucket={}, key={}", bucket, key);

      try (InputStream s3Stream = storageService.getFileStream(bucket, key)) {

        List<Question> questions = new ArrayList<>();
        QuestionParser parser = parserFactory.getParser(key);

        if (parser != null) {
          questions = parser.parse(s3Stream);
        } else {
          logger.warn("Unsupported file extension for key: {}", key);
        }

        logger.info("Parsed {} questions. Saving to database...", questions.size());

        for (Question q : questions) {
          questionService.createQuestion(q);
        }

        logger.info("Successfully saved {} questions.", questions.size());

      } catch (Exception e) {
        logger.error("Error processing file {} from bucket {}", key, bucket, e);
        throw new RuntimeException(e);
      }
    });

    return "Success";
  }
}
