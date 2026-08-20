package com.adnibog.gamecenter.repository;

import java.util.List;
import java.util.Optional;
import com.adnibog.gamecenter.repository.pagination.QuestionPage;
import com.adnibog.gamecenter.dto.request.PaginationRequest;
import com.adnibog.gamecenter.entity.Question;

public interface QuestionRepository {
  Optional<Question> findById(String projectId, String id);

  void save(Question question);

  void saveAll(List<Question> questions);

  void deleteById(String projectId, String id);

  void deleteAllByProjectId(String projectId);

  void deleteAllByFolderId(String projectId, String folderId);

  void deleteMultiple(String projectId, List<String> questionIds);

  QuestionPage findQuestions(String projectId, PaginationRequest pageReq);
  
  QuestionPage findQuestionsByFolderId(String projectId, String folderId, PaginationRequest pageReq);

  List<Question> findRandomQuestionsByFolderId(String projectId, String folderId, int amount);

  long countByProjectId(String projectId);
}
