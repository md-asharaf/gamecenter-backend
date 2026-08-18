package com.vocabkicker.service.parser;

import com.vocabkicker.entity.Question;
import java.io.InputStream;
import java.util.List;

public interface QuestionParser {
    boolean supports(String filename);
    List<Question> parse(InputStream inputStream) throws Exception;
}
