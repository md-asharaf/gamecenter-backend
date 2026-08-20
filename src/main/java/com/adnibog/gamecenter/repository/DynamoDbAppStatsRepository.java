package com.adnibog.gamecenter.repository;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.adnibog.gamecenter.entity.AppStats;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;

@Repository
public class DynamoDbAppStatsRepository implements AppStatsRepository {

  private final DynamoDbTable<AppStats> statsTable;
  private final DynamoDbClient dynamoDbClient;

  public DynamoDbAppStatsRepository(final DynamoDbEnhancedClient enhancedClient, final DynamoDbClient dynamoDbClient) {
    this.statsTable = enhancedClient.table("AppStats", TableSchema.fromBean(AppStats.class));
    this.dynamoDbClient = dynamoDbClient;
  }

  @Override
  public void incrementTotalAdmins() {
    updateAdminCount(1);
  }

  @Override
  public void decrementTotalAdmins() {
    updateAdminCount(-1);
  }

  @Override
  public long getTotalAdmins() {
    AppStats stats = statsTable.getItem(Key.builder().partitionValue(AppStats.GLOBAL_STATS_ID).build());
    return stats != null && stats.getTotalAdmins() != null ? stats.getTotalAdmins() : 0L;
  }

  private void updateAdminCount(int incrementValue) {
    Map<String, AttributeValue> key = new HashMap<>();
    key.put("id", AttributeValue.builder().s(AppStats.GLOBAL_STATS_ID).build());

    Map<String, AttributeValue> expressionValues = new HashMap<>();
    expressionValues.put(":inc", AttributeValue.builder().n(String.valueOf(incrementValue)).build());

    UpdateItemRequest request = UpdateItemRequest.builder()
        .tableName("AppStats")
        .key(key)
        .updateExpression("ADD totalAdmins :inc")
        .expressionAttributeValues(expressionValues)
        .build();

    dynamoDbClient.updateItem(request);
  }
}
