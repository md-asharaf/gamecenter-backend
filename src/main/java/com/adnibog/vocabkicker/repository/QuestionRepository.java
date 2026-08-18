package com.adnibog.vocabkicker.repository;

import java.util.List;
import java.util.Optional;

import com.adnibog.vocabkicker.entity.Question;

public interface QuestionRepository {
  Optional<Question> findById(String id);

  void save(Question question);

  void deleteById(String id);

  QuestionPage findQuestions(int limit, String lastEvaluatedKeyId, String searchKeyword);

  List<Question> findAll();
}
