package com.adnibog.gamecenter.service.parser;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.stereotype.Component;

import com.adnibog.gamecenter.entity.Question;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
public class DocxQuestionParser implements QuestionParser {

  @Override
  public boolean supports(String filename) {
    return filename != null && filename.toLowerCase().endsWith(".docx");
  }

  @Override
  public List<Question> parse(InputStream inputStream) throws Exception {
    List<Question> questions = new ArrayList<>();
    try (XWPFDocument document = new XWPFDocument(inputStream)) {
      for (XWPFTable table : document.getTables()) {
        boolean isFirstRow = true;
        for (XWPFTableRow row : table.getRows()) {
          if (isFirstRow) {
            isFirstRow = false;
            continue;
          }
          if (row.getTableCells().size() >= 3) {
            Question q = new Question();
            q.setField1(row.getCell(0).getText().trim());
            q.setField3(row.getCell(1).getText().trim());
            q.setField2(row.getCell(2).getText().trim());
            questions.add(q);
          }
        }
      }
    }
    return questions;
  }
}
