package com.vocabkicker.handlers;

import com.vocabkicker.VocabKickerServerlessApplication;
import com.vocabkicker.service.S3BatchProcessor;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

public class S3BatchRequestHandler implements RequestHandler<S3Event, String> {

  private static ApplicationContext context;

  static {
    context = SpringApplication.run(VocabKickerServerlessApplication.class);
  }

  @Override
  public String handleRequest(S3Event s3Event, Context awsContext) {
    S3BatchProcessor processor = context.getBean(S3BatchProcessor.class);
    return processor.process(s3Event);
  }
}
