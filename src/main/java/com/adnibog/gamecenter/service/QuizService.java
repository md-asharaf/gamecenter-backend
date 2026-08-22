package com.adnibog.gamecenter.service;

import org.springframework.stereotype.Service;

import com.adnibog.gamecenter.dto.model.QuizQuestion;
import com.adnibog.gamecenter.dto.model.ProjectDto;
import com.adnibog.gamecenter.entity.Question;
import com.adnibog.gamecenter.exception.BadRequestException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

    String folderId = projectDto.getQuizFolderId();
    if (folderId == null || folderId.trim().isEmpty()) {
      throw new BadRequestException(
          "This project does not have an active quiz folder configured. Admins must set one first.");
    }

    int numberOfQuestions = projectDto.getNumberOfQuestionsInQuiz() != null ? projectDto.getNumberOfQuestionsInQuiz()
        : 10;
    String mainField = projectDto.getMainQuestionField() != null ? projectDto.getMainQuestionField() : "field1";
    boolean isField2Main = "field2".equalsIgnoreCase(mainField);

    int amountToFetch = Math.max(numberOfQuestions * 6, 100);
    List<Question> allQuestions = new ArrayList<>(
        questionService.getRandomQuestionsByFolder(projectId, folderId, amountToFetch));

    if (allQuestions.size() < numberOfQuestions) {
      throw new BadRequestException("Not enough questions in database to form a quiz");
    }

    Collections.shuffle(allQuestions);
    List<Question> selectedQuestions = allQuestions.stream()
        .limit(numberOfQuestions)
        .collect(Collectors.toList());

    List<QuizQuestion> quiz = new ArrayList<>();
    for (Question q : selectedQuestions) {
      String correctAnswer = isField2Main ? q.getField1() : q.getField2();
      if (correctAnswer == null)
        correctAnswer = "";

      List<String> options = new ArrayList<>();
      options.add(correctAnswer);

      Set<String> seenOptionsLower = new HashSet<>();
      seenOptionsLower.add(correctAnswer.trim().toLowerCase());

      List<Question> shuffledAll = new ArrayList<>(allQuestions);
      Collections.shuffle(shuffledAll);

      for (Question distQ : shuffledAll) {
        if (options.size() >= 4)
          break;
        String distractor = isField2Main ? distQ.getField1() : distQ.getField2();
        if (distractor != null && !distractor.trim().isEmpty()) {
          String lower = distractor.trim().toLowerCase();
          if (!seenOptionsLower.contains(lower)) {
            seenOptionsLower.add(lower);
            options.add(distractor);
          }
        }
      }

      Collections.shuffle(options);

      QuizQuestion qq = new QuizQuestion();
      qq.setAnswer(correctAnswer);
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
