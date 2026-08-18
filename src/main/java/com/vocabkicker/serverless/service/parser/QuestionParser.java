package com.vocabkicker.serverless.service.parser;

import com.vocabkicker.serverless.entity.Question;
import java.io.InputStream;
import java.util.List;

public interface QuestionParser {
    List<Question> parse(InputStream inputStream) throws Exception;
}
