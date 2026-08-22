package com.adnibog.gamecenter.service.parser.impl;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.stereotype.Component;

import com.adnibog.gamecenter.entity.Project;
import com.adnibog.gamecenter.entity.Question;
import com.adnibog.gamecenter.service.parser.QuestionParser;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DocxQuestionParser implements QuestionParser {

  @Override
  public boolean supports(String filename) {
    return filename != null && filename.toLowerCase().endsWith(".docx");
  }

  @Override
  public List<Question> parse(InputStream inputStream, Project project) throws Exception {
    List<Question> questions = new ArrayList<>();
    try (XWPFDocument document = new XWPFDocument(inputStream)) {
      for (XWPFTable table : document.getTables()) {
        boolean isFirstRow = true;
        Map<String, Integer> headerMap = new HashMap<>();
        for (XWPFTableRow row : table.getRows()) {
          if (isFirstRow) {
            isFirstRow = false;
            for (int i = 0; i < row.getTableCells().size(); i++) {
              String text = row.getCell(i).getText();
              if (text != null) {
                headerMap.put(text.trim().toLowerCase(), i);
              }
            }
            continue;
          }

          int field1Idx = headerMap.getOrDefault(project != null && project.getField1Label() != null ? project.getField1Label().trim().toLowerCase() : "field1", 0);
          int field2Idx = headerMap.getOrDefault(project != null && project.getField2Label() != null ? project.getField2Label().trim().toLowerCase() : "field2", 1);
          int field3Idx = headerMap.getOrDefault(project != null && project.getField3Label() != null ? project.getField3Label().trim().toLowerCase() : "field3", 2);

          int maxIdx = Math.max(field1Idx, Math.max(field2Idx, field3Idx));
          if (row.getTableCells().size() <= maxIdx) {
            continue;
          }

          Question q = new Question();
          if (row.getCell(field1Idx) != null) {
            q.setField1(row.getCell(field1Idx).getText().trim());
          }
          if (row.getCell(field2Idx) != null) {
            q.setField2(row.getCell(field2Idx).getText().trim());
          }
          if (row.getCell(field3Idx) != null) {
            q.setField3(row.getCell(field3Idx).getText().trim());
          }
          questions.add(q);
        }
      }
    }
    return questions;
  }
}
