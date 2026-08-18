package com.vocabkicker.serverless.repository;

import com.vocabkicker.serverless.entity.Question;
import java.util.List;
import java.util.Optional;

public interface QuestionRepository {
    Optional<Question> findById(String id);
    void save(Question question);
    void deleteById(String id);
    QuestionPage findQuestions(int limit, String lastEvaluatedKeyId, String searchKeyword);
    List<Question> findAll(); 
}
