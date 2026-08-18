package com.adnibog.gamecenter.service.parser;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.adnibog.gamecenter.entity.Question;

class CsvQuestionParserTest {

  private CsvQuestionParser parser;

  @BeforeEach
  void setUp() {
    parser = new CsvQuestionParser();
  }

  @Test
  void parse_Success() throws Exception {
    String csvContent = "Word,Meaning,Usage\nApple,A fruit,I eat apple\nDog,An animal,I pet dog";
    InputStream is = new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8));

    List<Question> questions = parser.parse(is);

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
    String csvContent = "Word\nApple\nDog";
    InputStream is = new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8));

    List<Question> questions = parser.parse(is);

    assertNotNull(questions);
    assertEquals(0, questions.size());
  }

  @Test
  void parse_EmptyFile() throws Exception {
    String csvContent = "";
    InputStream is = new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8));

    List<Question> questions = parser.parse(is);

    assertNotNull(questions);
    assertTrue(questions.isEmpty());
  }
}
