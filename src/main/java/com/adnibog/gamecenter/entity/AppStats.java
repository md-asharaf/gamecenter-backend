package com.adnibog.gamecenter.entity;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class AppStats {
  public static final String GLOBAL_STATS_ID = "GLOBAL_STATS";

  private String id;
  private Long count;

  @DynamoDbPartitionKey
  public String getId() {
    return id;
  }
}
