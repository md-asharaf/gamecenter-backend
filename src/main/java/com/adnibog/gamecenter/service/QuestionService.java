package com.adnibog.gamecenter.service;

import org.springframework.stereotype.Service;

import com.adnibog.gamecenter.dto.request.CreateQuestionRequest;
import com.adnibog.gamecenter.dto.request.UpdateQuestionRequest;
import com.adnibog.gamecenter.dto.response.QuestionDto;
import com.adnibog.gamecenter.dto.response.QuestionPageResponse;
import com.adnibog.gamecenter.dto.response.QuizQuestion;
import com.adnibog.gamecenter.dto.response.ProjectDto;
import com.adnibog.gamecenter.entity.Question;
import com.adnibog.gamecenter.entity.Project;
import com.adnibog.gamecenter.exception.BadRequestException;
import com.adnibog.gamecenter.exception.NotFoundException;
import com.adnibog.gamecenter.mapper.QuestionMapper;
import com.adnibog.gamecenter.repository.QuestionPage;
import com.adnibog.gamecenter.repository.QuestionRepository;

import java.util.ArrayList;
import java.util.Collections;
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
      ProjectService projectService) {
    this.questionRepository = questionRepository;
    this.questionMapper = questionMapper;
    this.projectService = projectService;
  }

  public QuestionPageResponse getQuestions(String projectId, int limit, String lastEvaluatedKeyId,
      String searchKeyword) {
    ProjectDto project = projectService.getProjectById(projectId);
    QuestionPage page = questionRepository.findQuestions(projectId, limit, lastEvaluatedKeyId, searchKeyword);
    List<QuestionDto> dtos = page.getItems().stream()
        .map(q -> questionMapper.toDto(q, project))
        .collect(Collectors.toList());
    return new QuestionPageResponse(dtos, page.getLastEvaluatedKey());
  }

  public QuestionDto getQuestionById(String projectId, String id) {
    ProjectDto project = projectService.getProjectById(projectId);
    Question q = questionRepository.findById(projectId, id)
        .orElseThrow(() -> new NotFoundException("Question not found"));
    return questionMapper.toDto(q, project);
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
    log.info("Saved question {} for project {} via batch import", q.getId(), projectId);
  }

  public QuestionDto updateQuestion(String projectId, String id, UpdateQuestionRequest req) {
    ProjectDto project = projectService.getProjectById(projectId);
    Question existing = questionRepository.findById(projectId, id)
        .orElseThrow(() -> new NotFoundException("Question not found"));

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
        .orElseThrow(() -> new NotFoundException("Question not found"));
    questionRepository.deleteById(projectId, id);
    log.info("Deleted question {} in project {}", id, projectId);
  }

  public List<QuizQuestion> generateQuiz(String projectId) {
    Project project = projectService.getProjectEntityById(projectId);
    ProjectDto projectDto = projectService.getProjectById(projectId);

    int numberOfQuestions = project.getNumberOfQuestionsInQuiz() != null ? project.getNumberOfQuestionsInQuiz() : 10;
    String mainField = project.getMainQuestionField() != null ? project.getMainQuestionField() : "field1";

    List<Question> allQuestions = new ArrayList<>(questionRepository.findAll(projectId));

    if (allQuestions.size() < numberOfQuestions) {
      throw new BadRequestException("Not enough questions in database to form a quiz");
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

      if ("field2".equalsIgnoreCase(mainField)) {
        options.add(q.getField2());
        for (int i = 0; i < 3 && i < distractors.size(); i++) {
          options.add(distractors.get(i).getField2());
        }
      } else {
        options.add(q.getField1());
        for (int i = 0; i < 3 && i < distractors.size(); i++) {
          options.add(distractors.get(i).getField1());
        }
      }

      Collections.shuffle(options);
      QuizQuestion qq = new QuizQuestion();
      qq.setAnswer("field2".equalsIgnoreCase(mainField) ? q.getField2() : q.getField1());
      qq.setField1(q.getField1());
      qq.setField2(q.getField2());
      qq.setField3(q.getField3());
      qq.setOptions(options);
      qq.setProjects(projectDto);

      quiz.add(qq);
    }

    log.info("Generated quiz of size {} for project {}", quiz.size(), projectId);
    return quiz;
  }
}
