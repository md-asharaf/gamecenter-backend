package com.adnibog.gamecenter.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;

import java.util.Set;

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
  private Set<String> projectIds;
  private Role role;

  @DynamoDbPartitionKey
  public String getId() {
    return id;
  }

  @DynamoDbSecondaryPartitionKey(indexNames = "email-index")
  public String getEmail() {
    return email;
  }
}
