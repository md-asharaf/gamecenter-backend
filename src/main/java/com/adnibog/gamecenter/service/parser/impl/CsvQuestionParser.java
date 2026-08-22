package com.adnibog.gamecenter.service.parser.impl;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import com.adnibog.gamecenter.entity.Project;
import com.adnibog.gamecenter.entity.Question;
import com.adnibog.gamecenter.service.parser.QuestionParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CsvQuestionParser implements QuestionParser {

  @Override
  public boolean supports(String filename) {
    return filename != null && filename.toLowerCase().endsWith(".csv");
  }

  @Override
  public List<Question> parse(InputStream inputStream, Project project) throws Exception {
    List<Question> questions = new ArrayList<>();
    try (Reader in = new InputStreamReader(inputStream)) {
      CSVParser parser = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build().parse(in);
      Map<String, Integer> headerMap = new HashMap<>();
      if (parser.getHeaderNames() != null) {
        for (String header : parser.getHeaderNames()) {
          if (header != null) {
            headerMap.put(header.trim().toLowerCase(), parser.getHeaderMap().get(header));
          }
        }
      }

      int field1Idx = headerMap.getOrDefault(
          project != null && project.getField1Label() != null ? project.getField1Label().trim().toLowerCase()
              : "field1",
          0);
      int field2Idx = headerMap.getOrDefault(
          project != null && project.getField2Label() != null ? project.getField2Label().trim().toLowerCase()
              : "field2",
          1);
      int field3Idx = headerMap.getOrDefault(
          project != null && project.getField3Label() != null ? project.getField3Label().trim().toLowerCase()
              : "field3",
          2);

      for (CSVRecord record : parser) {
        int maxIdx = Math.max(field1Idx, Math.max(field2Idx, field3Idx));
        if (record.size() <= maxIdx) {
          continue;
        }

        Question q = new Question();
        if (record.get(field1Idx) != null) {
          q.setField1(record.get(field1Idx).trim());
        }
        if (record.get(field2Idx) != null) {
          q.setField2(record.get(field2Idx).trim());
        }
        if (record.get(field3Idx) != null) {
          q.setField3(record.get(field3Idx).trim());
        }
        questions.add(q);
      }
    }
    return questions;
  }
}
