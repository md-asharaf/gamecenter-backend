package com.vocabkicker.serverless.service;

import com.vocabkicker.serverless.dto.QuizQuestion;
import com.vocabkicker.serverless.entity.Question;
import com.vocabkicker.serverless.exception.NotFoundException;
import com.vocabkicker.serverless.repository.QuestionPage;
import com.vocabkicker.serverless.repository.QuestionRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class QuestionService {

  private final QuestionRepository questionRepository;

  public QuestionService(QuestionRepository questionRepository) {
    this.questionRepository = questionRepository;
  }

  public QuestionPage getQuestions(int limit, String lastEvaluatedKeyId, String searchKeyword) {
    return questionRepository.findQuestions(limit, lastEvaluatedKeyId, searchKeyword);
  }

  public Question getQuestionById(String id) {
    return questionRepository.findById(id).orElseThrow(() -> new NotFoundException("Question not found"));
  }

  public Question createQuestion(Question q) {
    long now = System.currentTimeMillis();
    if (q.getId() == null || q.getId().trim().isEmpty()) {
      q.setId(UUID.randomUUID().toString());
      q.setCreatedAt(now);
    } else if (q.getCreatedAt() == null) {
      q.setCreatedAt(now);
    }
    q.setUpdatedAt(now);
    questionRepository.save(q);
    return q;
  }

  public Question updateQuestion(String id, Question updatedFields) {
    Question existing = getQuestionById(id);
    if (updatedFields.getWord() != null)
      existing.setWord(updatedFields.getWord());
    if (updatedFields.getMnemonic() != null)
      existing.setMnemonic(updatedFields.getMnemonic());
    if (updatedFields.getDefinition() != null)
      existing.setDefinition(updatedFields.getDefinition());

    questionRepository.save(existing);
    return existing;
  }

  public void deleteQuestion(String id) {
    questionRepository.deleteById(id);
  }

  public int processCsvQuestions(java.io.Reader csvReader) throws Exception {
    org.apache.commons.csv.CSVParser parser = org.apache.commons.csv.CSVFormat.DEFAULT
        .builder()
        .setHeader()
        .setSkipHeaderRecord(true)
        .setIgnoreHeaderCase(true)
        .setTrim(true)
        .build()
        .parse(csvReader);

    int count = 0;
    for (org.apache.commons.csv.CSVRecord record : parser) {
      String word = record.get("Word");
      String definition = record.get("Definition");
      String mnemonic = record.isMapped("Mnemonic") ? record.get("Mnemonic") : "";

      if (word != null && !word.isEmpty() && definition != null && !definition.isEmpty()) {
        Question q = new Question();
        q.setWord(word);
        q.setDefinition(definition);
        q.setMnemonic(mnemonic);
        createQuestion(q);
        count++;
      }
    }
    return count;
  }

  public List<QuizQuestion> generateQuiz(int numberOfQuestions) {
    List<Question> allQuestions = new ArrayList<>(questionRepository.findAll());

    if (allQuestions.size() < numberOfQuestions) {
      throw new RuntimeException("Not enough questions in database to form a quiz.");
    }

    Collections.shuffle(allQuestions);
    List<Question> selectedQuestions = allQuestions.stream()
        .limit(numberOfQuestions)
        .collect(Collectors.toList());

    List<QuizQuestion> quiz = new ArrayList<>();
    for (Question q : selectedQuestions) {
      List<Question> distractors = new ArrayList<>(allQuestions);
      distractors.remove(q);
      Collections.shuffle(distractors);

      List<String> options = new ArrayList<>();
      options.add(q.getWord());

      for (int i = 0; i < 3 && i < distractors.size(); i++) {
        options.add(distractors.get(i).getWord());
      }

      Collections.shuffle(options);
      QuizQuestion qq = new QuizQuestion();
      qq.setAnswer(q.getWord());
      qq.setDefinition(q.getDefinition());
      qq.setMnemonic(q.getMnemonic());
      qq.setOptions(options);

      quiz.add(qq);
    }

    return quiz;
  }
}
