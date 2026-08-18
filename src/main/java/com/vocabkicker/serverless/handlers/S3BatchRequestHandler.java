package com.vocabkicker.serverless.handlers;

import com.vocabkicker.serverless.VocabKickerServerlessApplication;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.vocabkicker.serverless.service.S3BatchProcessor;
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
