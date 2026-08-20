package com.adnibog.gamecenter.repository.impl;

import com.adnibog.gamecenter.repository.AppStatsRepository;
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
    updateCount("TOTAL_ADMINS", 1);
  }

  @Override
  public void decrementTotalAdmins() {
    updateCount("TOTAL_ADMINS", -1);
  }

  @Override
  public long getTotalAdmins() {
    AppStats stats = getStats("TOTAL_ADMINS");
    return stats != null ? stats.getCount() : 0;
  }

  @Override
  public void incrementTotalProjects() {
    updateCount("TOTAL_PROJECTS", 1);
  }

  @Override
  public void decrementTotalProjects() {
    updateCount("TOTAL_PROJECTS", -1);
  }

  @Override
  public long getTotalProjects() {
    AppStats stats = getStats("TOTAL_PROJECTS");
    return stats != null ? stats.getCount() : 0;
  }

  private AppStats getStats(String id) {
    return statsTable.getItem(Key.builder().partitionValue(id).build());
  }

  private void updateCount(String id, int incrementValue) {
    Map<String, AttributeValue> key = new HashMap<>();
    key.put("id", AttributeValue.builder().s(id).build());

    Map<String, AttributeValue> expressionValues = new HashMap<>();
    expressionValues.put(":inc", AttributeValue.builder().n(String.valueOf(incrementValue)).build());

    Map<String, String> expressionNames = new HashMap<>();
    expressionNames.put("#cnt", "count");

    UpdateItemRequest request = UpdateItemRequest.builder()
        .tableName("AppStats")
        .key(key)
        .updateExpression("ADD #cnt :inc")
        .expressionAttributeNames(expressionNames)
        .expressionAttributeValues(expressionValues)
        .build();

    dynamoDbClient.updateItem(request);
  }
}
