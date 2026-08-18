package com.vocabkicker.service.parser;

import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class QuestionParserFactory {

    private final List<QuestionParser> parsers;

    public QuestionParserFactory(List<QuestionParser> parsers) {
        this.parsers = parsers;
    }

    public QuestionParser getParser(String filename) {
        return parsers.stream()
                .filter(parser -> parser.supports(filename))
                .findFirst()
                .orElse(null);
    }
}
