package com.adnibog.gamecenter.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;

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
  @Builder.Default
  private String type = "PROJECT";
  private Long createdAt;
  private Long updatedAt;

  @DynamoDbPartitionKey
  public String getId() {
    return id;
  }

  @DynamoDbSecondaryPartitionKey(indexNames = "name-index")
  public String getName() {
    return name;
  }

  @DynamoDbSecondaryPartitionKey(indexNames = "type-createdAt-index")
  public String getType() {
    return type;
  }

  @DynamoDbSecondarySortKey(indexNames = "type-createdAt-index")
  public Long getCreatedAt() {
    return createdAt;
  }
}
