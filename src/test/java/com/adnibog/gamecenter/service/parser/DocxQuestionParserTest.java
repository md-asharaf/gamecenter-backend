package com.adnibog.gamecenter.service.parser;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.adnibog.gamecenter.entity.Question;
import com.adnibog.gamecenter.service.parser.impl.DocxQuestionParser;

class DocxQuestionParserTest {

  private DocxQuestionParser parser;

  @BeforeEach
  void setUp() {
    parser = new DocxQuestionParser();
  }

  @Test
  void parse_Success() throws Exception {
    XWPFDocument document = new XWPFDocument();
    XWPFTable table = document.createTable();

    XWPFTableRow row0 = table.getRow(0);
    row0.getCell(0).setText("Word");
    row0.addNewTableCell().setText("Usage");
    row0.addNewTableCell().setText("Meaning");

    XWPFTableRow row1 = table.createRow();
    row1.getCell(0).setText("Apple");
    row1.getCell(1).setText("A fruit");
    row1.getCell(2).setText("I eat apple");

    XWPFTableRow row2 = table.createRow();
    row2.getCell(0).setText("Dog");
    row2.getCell(1).setText("An animal");
    row2.getCell(2).setText("I pet dog");

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    document.write(out);
    out.close();
    document.close();

    ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());

    List<Question> questions = parser.parse(in);

    assertNotNull(questions);
    assertEquals(2, questions.size());

    Question q1 = questions.get(0);
    assertEquals("Apple", q1.getField1());
    assertEquals("A fruit", q1.getField3());
    assertEquals("I eat apple", q1.getField2());

    Question q2 = questions.get(1);
    assertEquals("Dog", q2.getField1());
    assertEquals("An animal", q2.getField3());
    assertEquals("I pet dog", q2.getField2());
  }

  @Test
  void parse_MissingColumns() throws Exception {
    XWPFDocument document = new XWPFDocument();
    XWPFTable table = document.createTable();

    XWPFTableRow row0 = table.getRow(0);
    row0.getCell(0).setText("Word");

    XWPFTableRow row1 = table.createRow();
    row1.getCell(0).setText("Apple");

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    document.write(out);
    out.close();
    document.close();

    ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());

    List<Question> questions = parser.parse(in);

    assertNotNull(questions);
    assertEquals(0, questions.size());
  }
}
