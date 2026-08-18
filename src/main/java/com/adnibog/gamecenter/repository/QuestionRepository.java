package com.adnibog.gamecenter.repository;

import java.util.List;
import java.util.Optional;

import com.adnibog.gamecenter.entity.Question;

public interface QuestionRepository {
  Optional<Question> findById(String projectId, String id);

  void save(Question question);

  void deleteById(String projectId, String id);

  QuestionPage findQuestions(String projectId, int limit, String lastEvaluatedKeyId, String searchKeyword);

  List<Question> findAll(String projectId);
}
