package com.adnibog.gamecenter.dto.response;

import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectDto {
  private String id;
  private String name;
  private Integer numberOfQuestionsInQuiz;
  @JsonIgnore
  private String mainQuestionField;

  private String field1Label;
  private String field2Label;
  private String field3Label;
  private Long createdAt;
  private Long updatedAt;

  public String getMainQuestionLabel() {
    if ("field1".equals(mainQuestionField))
      return field1Label;
    if ("field2".equals(mainQuestionField))
      return field2Label;
    return field1Label;
  }
}
