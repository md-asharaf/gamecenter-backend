package com.adnibog.gamecenter.service;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.adnibog.gamecenter.dto.request.CreateQuestionRequest;
import com.adnibog.gamecenter.dto.request.PaginationRequest;
import com.adnibog.gamecenter.dto.request.UpdateQuestionRequest;
import com.adnibog.gamecenter.dto.model.QuestionDto;
import com.adnibog.gamecenter.dto.response.QuestionPageResponse;
import com.adnibog.gamecenter.dto.model.ProjectDto;
import com.adnibog.gamecenter.entity.Question;
import com.adnibog.gamecenter.event.ProjectDeletedEvent;
import com.adnibog.gamecenter.exception.BadRequestException;
import com.adnibog.gamecenter.exception.NotFoundException;
import com.adnibog.gamecenter.mapper.QuestionMapper;
import com.adnibog.gamecenter.mapper.ProjectMapper;
import com.adnibog.gamecenter.repository.pagination.QuestionPage;
import com.adnibog.gamecenter.repository.QuestionRepository;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class QuestionService {

  private final QuestionRepository questionRepository;
  private final QuestionMapper questionMapper;
  private final ProjectService projectService;

  public QuestionService(QuestionRepository questionRepository, QuestionMapper questionMapper,
      ProjectService projectService, ProjectMapper projectMapper) {
    this.questionRepository = questionRepository;
    this.questionMapper = questionMapper;
    this.projectService = projectService;
  }

  public QuestionPageResponse getQuestions(String projectId, PaginationRequest pageReq) {
    ProjectDto project = projectService.getProjectById(projectId);
    QuestionPage page = questionRepository.findQuestions(projectId, pageReq);
    List<QuestionDto> dtos = page.getItems().stream()
        .map(q -> questionMapper.toDto(q, project))
        .collect(Collectors.toList());
    return new QuestionPageResponse(dtos, page.getLastEvaluatedKey());
  }

  public QuestionDto getQuestionById(String projectId, String id) {
    ProjectDto project = projectService.getProjectById(projectId);
    Question q = questionRepository.findById(projectId, id)
        .orElseThrow(() -> new NotFoundException("Question not found."));
    return questionMapper.toDto(q, project);
  }

  public long countByProjectId(String projectId) {
    return questionRepository.countByProjectId(projectId);
  }

  public QuestionDto createQuestion(String projectId, CreateQuestionRequest req) {
    ProjectDto project = projectService.getProjectById(projectId);
    Map<String, String> dynamicFields = req.getDynamicFields();

    String field1 = dynamicFields.get(project.getField1Label());
    String field2 = dynamicFields.get(project.getField2Label());
    String field3 = dynamicFields.get(project.getField3Label());

    if (field1 == null || field1.trim().isEmpty()) {
      throw new BadRequestException("The primary question field cannot be empty");
    }

    if (project.getField2Label() != null && !project.getField2Label().isEmpty() &&
        (field2 == null || field2.trim().isEmpty())) {
      throw new BadRequestException("The secondary question field cannot be empty");
    }

    long now = System.currentTimeMillis();
    Question q = new Question();
    q.setProjectId(projectId);
    q.setId(UUID.randomUUID().toString());
    q.setField1(field1);
    q.setField2(field2);
    q.setField3(field3);
    q.setCreatedAt(now);
    q.setUpdatedAt(now);
    questionRepository.save(q);

    log.info("Created question {} from request for project {}", q.getId(), projectId);
    return questionMapper.toDto(q, project);
  }

  public void saveQuestion(String projectId, Question q) {
    projectService.getProjectById(projectId);
    long now = System.currentTimeMillis();
    q.setProjectId(projectId);
    if (q.getId() == null || q.getId().isBlank()) {
      q.setId(java.util.UUID.randomUUID().toString());
      q.setCreatedAt(now);
    } else if (q.getCreatedAt() == null) {
      q.setCreatedAt(now);
    }
    q.setUpdatedAt(now);
    questionRepository.save(q);
    log.info("Saved question {} for project {}", q.getId(), projectId);
  }

  public void saveQuestionsBatch(String projectId, List<Question> questions) {
    projectService.getProjectById(projectId);
    long now = System.currentTimeMillis();
    for (Question q : questions) {
      q.setProjectId(projectId);
      if (q.getId() == null || q.getId().isBlank()) {
        q.setId(java.util.UUID.randomUUID().toString());
        q.setCreatedAt(now);
      } else if (q.getCreatedAt() == null) {
        q.setCreatedAt(now);
      }
      q.setUpdatedAt(now);
    }
    questionRepository.saveAll(questions);
    log.info("Saved {} questions in batch for project {}", questions.size(), projectId);
  }

  public QuestionDto updateQuestion(String projectId, String id, UpdateQuestionRequest req) {
    ProjectDto project = projectService.getProjectById(projectId);
    Question existing = questionRepository.findById(projectId, id)
        .orElseThrow(() -> new NotFoundException("Question not found."));

    Map<String, String> dynamicFields = req.getDynamicFields();

    boolean updated = false;
    if (dynamicFields.containsKey(project.getField1Label())) {
      existing.setField1(dynamicFields.get(project.getField1Label()));
      updated = true;
    }
    if (dynamicFields.containsKey(project.getField2Label())) {
      existing.setField2(dynamicFields.get(project.getField2Label()));
      updated = true;
    }
    if (dynamicFields.containsKey(project.getField3Label())) {
      existing.setField3(dynamicFields.get(project.getField3Label()));
      updated = true;
    }

    if (!updated) {
      throw new BadRequestException("Please provide at least one valid field to update");
    }

    existing.setUpdatedAt(System.currentTimeMillis());
    questionRepository.save(existing);

    log.info("Updated question {} in project {}", id, projectId);
    return questionMapper.toDto(existing, project);
  }

  public void deleteQuestion(String projectId, String id) {
    questionRepository.findById(projectId, id)
        .orElseThrow(() -> new NotFoundException("Question not found."));
    questionRepository.deleteById(projectId, id);
    log.info("Deleted question {} in project {}", id, projectId);
  }

  @EventListener
  public void handleProjectDeletedEvent(ProjectDeletedEvent event) {
    String projectId = event.getProjectId();
    log.info("Handling ProjectDeletedEvent for project {}", projectId);
    questionRepository.deleteAllByProjectId(projectId);
  }

  public List<Question> getRandomQuestions(String projectId, int amountToFetch) {
    return questionRepository.findRandomQuestions(projectId, amountToFetch);
  }
}
