package com.adnibog.gamecenter.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamoDbBean
public class Question {
  private String projectId;
  private String folderId;
  private String id;
  private String field1;
  private String field2;
  private String field3;
  private Long createdAt;
  private Long updatedAt;

  @DynamoDbPartitionKey
  public String getProjectId() {
    return projectId;
  }

  @DynamoDbSortKey
  @DynamoDbSecondarySortKey(indexNames = "folder-index")
  public String getId() {
    return id;
  }

  @DynamoDbSecondaryPartitionKey(indexNames = "folder-index")
  public String getFolderId() {
    return folderId;
  }
}
