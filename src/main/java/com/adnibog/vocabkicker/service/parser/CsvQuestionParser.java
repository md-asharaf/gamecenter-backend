package com.adnibog.vocabkicker.service.parser;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import com.adnibog.vocabkicker.entity.Question;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

@Component
public class CsvQuestionParser implements QuestionParser {

  @Override
  public boolean supports(String filename) {
    return filename != null && filename.toLowerCase().endsWith(".csv");
  }

  @Override
  public List<Question> parse(InputStream inputStream) throws Exception {
    List<Question> questions = new ArrayList<>();
    try (Reader in = new InputStreamReader(inputStream)) {
      Iterable<CSVRecord> records = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build().parse(in);
      for (CSVRecord record : records) {
        if (record.size() >= 3) {
          Question q = new Question();
          q.setWord(record.get(0).trim());
          q.setMnemonic(record.get(1).trim());
          q.setDefinition(record.get(2).trim());
          questions.add(q);
        }
      }
    }
    return questions;
  }
}
