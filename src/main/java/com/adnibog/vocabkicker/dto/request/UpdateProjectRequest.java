package com.adnibog.vocabkicker.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class UpdateProjectRequest {

  private String name;

  @Min(value = 1, message = "The number of questions in a quiz must be at least 1")
  @Max(value = 100, message = "The number of questions in a quiz must be at most 100")
  private Integer numberOfQuestionsInQuiz;

  private String mainQuestionLabel;

  private String field1Label;
  private String field2Label;
  private String field3Label;
}
