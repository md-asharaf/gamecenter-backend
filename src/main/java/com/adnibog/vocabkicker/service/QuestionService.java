package com.adnibog.vocabkicker.service;

import org.springframework.stereotype.Service;

import com.adnibog.vocabkicker.dto.request.UpdateQuestionRequest;
import com.adnibog.vocabkicker.dto.response.QuestionDto;
import com.adnibog.vocabkicker.dto.response.QuestionPageResponse;
import com.adnibog.vocabkicker.dto.response.QuizQuestion;
import com.adnibog.vocabkicker.entity.Question;
import com.adnibog.vocabkicker.exception.NotFoundException;
import com.adnibog.vocabkicker.mapper.QuestionMapper;
import com.adnibog.vocabkicker.repository.QuestionPage;
import com.adnibog.vocabkicker.repository.QuestionRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;

@Service
public class QuestionService {

  private final QuestionRepository questionRepository;
  private final QuestionMapper questionMapper;

  public QuestionService(QuestionRepository questionRepository, QuestionMapper questionMapper) {
    this.questionRepository = questionRepository;
    this.questionMapper = questionMapper;
  }

  public QuestionPageResponse getQuestions(int limit, String lastEvaluatedKeyId, String searchKeyword) {
    QuestionPage page = questionRepository.findQuestions(limit, lastEvaluatedKeyId, searchKeyword);
    List<QuestionDto> dtos = page.getItems().stream().map(questionMapper::toDto).collect(Collectors.toList());
    return new QuestionPageResponse(dtos, page.getLastEvaluatedKey());
  }

  public QuestionDto getQuestionById(String id) {
    Question q = questionRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("Question not found"));
    return questionMapper.toDto(q);
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

  public QuestionDto updateQuestion(String id, UpdateQuestionRequest req) {
    Question existing = questionRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("Question not found"));

    if (req.getWord() != null)
      existing.setWord(req.getWord());
    if (req.getDefinition() != null)
      existing.setDefinition(req.getDefinition());
    if (req.getMnemonic() != null)
      existing.setMnemonic(req.getMnemonic());

    existing.setUpdatedAt(System.currentTimeMillis());
    questionRepository.save(existing);

    return questionMapper.toDto(existing);
  }

  public void deleteQuestion(String id) {
    questionRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("Question not found"));
    questionRepository.deleteById(id);
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
