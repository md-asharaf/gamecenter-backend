package com.adnibog.vocabkicker.service;

import org.springframework.stereotype.Service;

import com.adnibog.vocabkicker.dto.request.CreateQuestionRequest;
import com.adnibog.vocabkicker.dto.request.UpdateQuestionRequest;
import com.adnibog.vocabkicker.dto.response.QuestionDto;
import com.adnibog.vocabkicker.dto.response.QuestionPageResponse;
import com.adnibog.vocabkicker.dto.response.QuizQuestion;
import com.adnibog.vocabkicker.dto.response.ProjectDto;
import com.adnibog.vocabkicker.entity.Question;
import com.adnibog.vocabkicker.entity.Project;
import com.adnibog.vocabkicker.exception.NotFoundException;
import com.adnibog.vocabkicker.mapper.QuestionMapper;
import com.adnibog.vocabkicker.mapper.ProjectMapper;
import com.adnibog.vocabkicker.repository.QuestionPage;
import com.adnibog.vocabkicker.repository.QuestionRepository;
import com.adnibog.vocabkicker.repository.ProjectRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class QuestionService {

  private final QuestionRepository questionRepository;
  private final QuestionMapper questionMapper;
  private final ProjectRepository projectRepository;
  private final ProjectMapper projectMapper;

  public QuestionService(QuestionRepository questionRepository, QuestionMapper questionMapper,
      ProjectRepository projectRepository, ProjectMapper projectMapper) {
    this.questionRepository = questionRepository;
    this.questionMapper = questionMapper;
    this.projectRepository = projectRepository;
    this.projectMapper = projectMapper;
  }

  private ProjectDto getProjects(String projectId) {
    Project project = projectRepository.findByProjectId(projectId)
        .orElseThrow(() -> new NotFoundException("Project projects not found"));
    return projectMapper.toDto(project);
  }

  public QuestionPageResponse getQuestions(String projectId, int limit, String lastEvaluatedKeyId,
      String searchKeyword) {
    ProjectDto projects = getProjects(projectId);
    QuestionPage page = questionRepository.findQuestions(projectId, limit, lastEvaluatedKeyId, searchKeyword);
    List<QuestionDto> dtos = page.getItems().stream()
        .map(q -> questionMapper.toDto(q, projects))
        .collect(Collectors.toList());
    return new QuestionPageResponse(dtos, page.getLastEvaluatedKey());
  }

  public QuestionDto getQuestionById(String projectId, String id) {
    ProjectDto projects = getProjects(projectId);
    Question q = questionRepository.findById(projectId, id)
        .orElseThrow(() -> new NotFoundException("Question not found"));
    return questionMapper.toDto(q, projects);
  }

  public QuestionDto createQuestion(String projectId, Question q) {
    ProjectDto projects = getProjects(projectId);
    long now = System.currentTimeMillis();
    q.setProjectId(projectId);
    if (q.getId() == null || q.getId().trim().isEmpty()) {
      q.setId(UUID.randomUUID().toString());
      q.setCreatedAt(now);
    } else if (q.getCreatedAt() == null) {
      q.setCreatedAt(now);
    }
    q.setUpdatedAt(now);
    questionRepository.save(q);
    return questionMapper.toDto(q, projects);
  }

  public QuestionDto createQuestionFromRequest(String projectId, CreateQuestionRequest req) {
    ProjectDto projects = getProjects(projectId);
    Map<String, String> dynamicFields = req.getDynamicFields();

    String field1 = dynamicFields.get(projects.getField1Label());
    String field2 = dynamicFields.get(projects.getField2Label());
    String field3 = dynamicFields.get(projects.getField3Label());

    if (field1 == null || field1.trim().isEmpty()) {
      throw new IllegalArgumentException(projects.getField1Label() + " cannot be blank");
    }
    if (field2 == null || field2.trim().isEmpty()) {
      throw new IllegalArgumentException(projects.getField2Label() + " cannot be blank");
    }

    Question q = new Question();
    q.setField1(field1);
    q.setField2(field2);
    q.setField3(field3);

    long now = System.currentTimeMillis();
    q.setProjectId(projectId);
    q.setId(UUID.randomUUID().toString());
    q.setCreatedAt(now);
    q.setUpdatedAt(now);

    questionRepository.save(q);
    return questionMapper.toDto(q, projects);
  }

  public QuestionDto updateQuestion(String projectId, String id, UpdateQuestionRequest req) {
    ProjectDto projects = getProjects(projectId);
    Question existing = questionRepository.findById(projectId, id)
        .orElseThrow(() -> new NotFoundException("Question not found"));

    Map<String, String> dynamicFields = req.getDynamicFields();

    boolean updated = false;
    if (dynamicFields.containsKey(projects.getField1Label())) {
      existing.setField1(dynamicFields.get(projects.getField1Label()));
      updated = true;
    }
    if (dynamicFields.containsKey(projects.getField2Label())) {
      existing.setField2(dynamicFields.get(projects.getField2Label()));
      updated = true;
    }
    if (dynamicFields.containsKey(projects.getField3Label())) {
      existing.setField3(dynamicFields.get(projects.getField3Label()));
      updated = true;
    }

    if (!updated) {
      throw new IllegalArgumentException("At least one valid field must be provided to update");
    }

    existing.setUpdatedAt(System.currentTimeMillis());
    questionRepository.save(existing);

    return questionMapper.toDto(existing, projects);
  }

  public void deleteQuestion(String projectId, String id) {
    questionRepository.findById(projectId, id)
        .orElseThrow(() -> new NotFoundException("Question not found"));
    questionRepository.deleteById(projectId, id);
  }

  public List<QuizQuestion> generateQuiz(String projectId) {
    Project project = projectRepository.findByProjectId(projectId)
        .orElseThrow(() -> new NotFoundException("Project projects not found"));
    ProjectDto projectDto = projectMapper.toDto(project);

    int numberOfQuestions = project.getNumberOfQuestionsInQuiz() != null ? project.getNumberOfQuestionsInQuiz() : 10;
    String mainField = project.getMainQuestionField() != null ? project.getMainQuestionField() : "field1";

    List<Question> allQuestions = new ArrayList<>(questionRepository.findAll(projectId));

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
      qq.setField2(q.getField2());
      qq.setField3(q.getField3());
      qq.setOptions(options);
      qq.setProjects(projectDto);

      quiz.add(qq);
    }

    return quiz;
  }
}
