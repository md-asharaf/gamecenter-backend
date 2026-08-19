package com.adnibog.gamecenter.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.adnibog.gamecenter.dto.request.CreateQuestionRequest;
import com.adnibog.gamecenter.dto.request.UpdateQuestionRequest;
import com.adnibog.gamecenter.dto.response.ProjectDto;
import com.adnibog.gamecenter.dto.response.QuestionDto;
import com.adnibog.gamecenter.entity.Project;
import com.adnibog.gamecenter.entity.Question;
import com.adnibog.gamecenter.exception.BadRequestException;
import com.adnibog.gamecenter.mapper.ProjectMapper;
import com.adnibog.gamecenter.mapper.QuestionMapper;
import com.adnibog.gamecenter.repository.QuestionRepository;

@ExtendWith(MockitoExtension.class)
class QuestionServiceTest {

  @Mock
  private QuestionRepository questionRepository;

  @Mock
  private QuestionMapper questionMapper;

  @Mock
  private ProjectService projectService;

  @Mock
  private ProjectMapper projectMapper;

  private QuestionService questionService;

  @BeforeEach
  void setUp() {
    questionService = new QuestionService(questionRepository, questionMapper, projectService, projectMapper);
  }

  @Test
  void createQuestionFromRequest_Success() {
    String projectId = "proj1";

    ProjectDto projectDto = new ProjectDto();
    projectDto.setField1Label("Word");
    projectDto.setField2Label("Meaning");

    when(projectService.getProjectById(projectId)).thenReturn(projectDto);

    CreateQuestionRequest req = new CreateQuestionRequest();
    req.setDynamicFields(new HashMap<>(Map.of("Word", "Apple", "Meaning", "A fruit")));

    QuestionDto expectedDto = QuestionDto.builder().build();
    expectedDto.setProjects(projectDto);
    expectedDto.setField1("Apple");
    expectedDto.setField2("A fruit");

    when(questionMapper.toDto(any(Question.class), eq(projectDto))).thenReturn(expectedDto);

    QuestionDto result = questionService.createQuestion(projectId, req);

    assertNotNull(result);
    verify(questionRepository).save(any(Question.class));
  }

  @Test
  void createQuestionFromRequest_MissingPrimaryField_ThrowsBadRequest() {
    String projectId = "proj1";

    ProjectDto projectDto = new ProjectDto();
    projectDto.setField1Label("Word");
    projectDto.setField2Label("Meaning");

    when(projectService.getProjectById(projectId)).thenReturn(projectDto);

    CreateQuestionRequest req = new CreateQuestionRequest();
    req.setDynamicFields(new HashMap<>(Map.of("Meaning", "A fruit")));

    assertThrows(BadRequestException.class, () -> questionService.createQuestion(projectId, req));
    verify(questionRepository, never()).save(any());
  }

  @Test
  void updateQuestion_Success() {
    String projectId = "proj1";
    String qId = "q1";

    ProjectDto projectDto = new ProjectDto();
    projectDto.setField1Label("Word");

    when(projectService.getProjectById(projectId)).thenReturn(projectDto);

    Question existing = new Question();
    existing.setId(qId);
    existing.setField1("OldWord");

    when(questionRepository.findById(projectId, qId)).thenReturn(Optional.of(existing));

    UpdateQuestionRequest req = new UpdateQuestionRequest();
    req.setDynamicFields(new HashMap<>(Map.of("Word", "NewWord")));

    QuestionDto expectedDto = QuestionDto.builder().build();
    expectedDto.setProjects(projectDto);
    expectedDto.setField1("NewWord");

    when(questionMapper.toDto(existing, projectDto)).thenReturn(expectedDto);

    QuestionDto result = questionService.updateQuestion(projectId, qId, req);

    assertNotNull(result);
    assertEquals("NewWord", existing.getField1());
    verify(questionRepository).save(existing);
  }

  @Test
  void generateQuiz_NotEnoughQuestions() {
    String projectId = "proj1";

    Project project = new Project();
    project.setNumberOfQuestionsInQuiz(10);

    when(projectService.getProjectEntityById(projectId)).thenReturn(project);
    when(projectMapper.toDto(project)).thenReturn(new ProjectDto());

    List<Question> questions = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      questions.add(new Question());
    }
    when(questionRepository.findAll(projectId)).thenReturn(questions);

    assertThrows(BadRequestException.class, () -> questionService.generateQuiz(projectId));
  }
}
