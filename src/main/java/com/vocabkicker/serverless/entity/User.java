package com.vocabkicker.serverless.entity;

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
public class User {
  private String id;
  private String email;
  private String passwordHash;
  private String refreshTokenHash;
  private Long refreshTokenExpiry;
  private Long createdAt;
  private Long updatedAt;

  @DynamoDbPartitionKey
  public String getId() {
    return id;
  }

}
