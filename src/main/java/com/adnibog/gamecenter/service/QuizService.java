package com.adnibog.gamecenter.service;

import org.springframework.stereotype.Service;

import com.adnibog.gamecenter.dto.model.QuizQuestion;
import com.adnibog.gamecenter.dto.model.ProjectDto;
import com.adnibog.gamecenter.entity.Question;
import com.adnibog.gamecenter.exception.BadRequestException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class QuizService {

  private final QuestionService questionService;
  private final ProjectService projectService;

  public QuizService(QuestionService questionService, ProjectService projectService) {
    this.questionService = questionService;
    this.projectService = projectService;
  }

  public List<QuizQuestion> generateQuiz(String projectId) {
    ProjectDto projectDto = projectService.getProjectById(projectId);

    int numberOfQuestions = projectDto.getNumberOfQuestionsInQuiz() != null ? projectDto.getNumberOfQuestionsInQuiz() : 10;
    String mainField = projectDto.getMainQuestionField() != null ? projectDto.getMainQuestionField() : "field1";

    int amountToFetch = Math.max(numberOfQuestions * 4, 50);
    List<Question> allQuestions = new ArrayList<>(questionService.getRandomQuestions(projectId, amountToFetch));

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
        options.add(q.getField1());
        for (int i = 0; i < 3 && i < distractors.size(); i++) {
          options.add(distractors.get(i).getField1());
        }
      } else {
        options.add(q.getField2());
        for (int i = 0; i < 3 && i < distractors.size(); i++) {
          options.add(distractors.get(i).getField2());
        }
      }

      Collections.shuffle(options);
      QuizQuestion qq = new QuizQuestion();
      qq.setAnswer("field2".equalsIgnoreCase(mainField) ? q.getField1() : q.getField2());
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
