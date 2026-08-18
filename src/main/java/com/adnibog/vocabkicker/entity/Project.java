package com.adnibog.vocabkicker.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamoDbBean
public class Project {
  private String id;
  private String name;
  private Integer numberOfQuestionsInQuiz;
  private String mainQuestionField;
  private String field1Label;
  private String field2Label;
  private String field3Label;
  private Long createdAt;
  private Long updatedAt;

  @DynamoDbPartitionKey
  public String getId() {
    return id;
  }
}
