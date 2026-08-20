package com.adnibog.gamecenter.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamoDbBean
public class Folder {
  private String projectId;
  private String id;
  private String name;
  private Long createdAt;
  private Long updatedAt;

  @DynamoDbPartitionKey
  public String getProjectId() {
    return projectId;
  }

  @DynamoDbSortKey
  public String getId() {
    return id;
  }
}
