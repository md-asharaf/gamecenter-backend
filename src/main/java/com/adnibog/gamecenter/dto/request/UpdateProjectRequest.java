package com.adnibog.gamecenter.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class UpdateProjectRequest {

  private String name;

  @Min(value = 1, message = "Quiz must contain at least 1 question.")
  @Max(value = 100, message = "Quiz cannot exceed 100 questions.")
  private Integer numberOfQuestionsInQuiz;

  private String mainQuestionLabel;

  private String field1Label;
  private String field2Label;
  private String field3Label;

  private String quizFolderId;
}
