package com.adnibog.vocabkicker.service.parser;

import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

@Component
public class QuestionParserFactory {

  private final List<QuestionParser> parsers;

  public QuestionParserFactory(List<QuestionParser> parsers) {
    this.parsers = parsers;
  }

  public Optional<QuestionParser> getParser(String filename) {
    return parsers.stream()
        .filter(parser -> parser.supports(filename))
        .findFirst();
  }
}
