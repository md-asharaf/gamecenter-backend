package com.adnibog.gamecenter.repository.impl;

import com.adnibog.gamecenter.repository.ProjectRepository;
import com.adnibog.gamecenter.repository.pagination.ProjectPage;
import org.springframework.stereotype.Repository;

import com.adnibog.gamecenter.entity.Project;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class DynamoDbProjectRepository implements ProjectRepository {

  private final DynamoDbTable<Project> projectTable;

  public DynamoDbProjectRepository(final DynamoDbEnhancedClient enhancedClient) {
    this.projectTable = enhancedClient.table("Projects", TableSchema.fromBean(Project.class));
  }

  @Override
  public Optional<Project> findById(String projectId) {
    return Optional.ofNullable(projectTable.getItem(Key.builder().partitionValue(projectId).build()));
  }

  @Override
  public Optional<Project> findByName(String name) {
    QueryConditional conditional = QueryConditional.keyEqualTo(Key.builder().partitionValue(name).build());
    return projectTable.index("name-index").query(conditional).stream()
        .flatMap(page -> page.items().stream())
        .findFirst();
  }

  @Override
  public void save(Project project) {
    projectTable.putItem(project);
  }

  @Override
  public void deleteById(String projectId) {
    projectTable.deleteItem(Key.builder().partitionValue(projectId).build());
  }

  @Override
  public List<Project> findAll() {
    return projectTable.scan().items().stream().collect(Collectors.toList());
  }

  @Override
  public ProjectPage findProjects(int limit, String lastEvaluatedKeyId, String searchKeyword) {
    Map<String, AttributeValue> exclusiveStartKey = null;
    if (lastEvaluatedKeyId != null && !lastEvaluatedKeyId.equals("null") && !lastEvaluatedKeyId.trim().isEmpty()) {
      exclusiveStartKey = new HashMap<>();
      exclusiveStartKey.put("id", AttributeValue.builder().s(lastEvaluatedKeyId).build());
    }

    final Map<String, AttributeValue> finalExclusiveStartKey = exclusiveStartKey;
    List<Project> resultItems = new ArrayList<>();
    String nextKey = null;

    Expression filterExpression = null;
    if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
      Map<String, AttributeValue> expressionValues = new HashMap<>();
      expressionValues.put(":searchKeyword", AttributeValue.builder().s(searchKeyword).build());
      Map<String, String> expressionNames = new HashMap<>();
      expressionNames.put("#name", "name");

      filterExpression = Expression.builder()
          .expression("contains(#name, :searchKeyword)")
          .expressionNames(expressionNames)
          .expressionValues(expressionValues)
          .build();
    }

    final Expression finalFilterExpression = filterExpression;

    Iterator<Page<Project>> iterator = projectTable.scan(r -> {
      if (finalExclusiveStartKey != null) {
        r.exclusiveStartKey(finalExclusiveStartKey);
      }
      if (finalFilterExpression != null) {
        r.filterExpression(finalFilterExpression);
      }
    }).iterator();

    outerLoop: while (iterator.hasNext()) {
      Page<Project> page = iterator.next();
      for (Project p : page.items()) {
        resultItems.add(p);
        if (resultItems.size() == limit) {
          nextKey = p.getId();
          break outerLoop;
        }
      }
    }

    return new ProjectPage(resultItems, nextKey);
  }

  @Override
  public List<Project> getMostRecentProjects(int limit) {
    DynamoDbIndex<Project> index = projectTable.index("type-createdAt-index");
    QueryConditional conditional = QueryConditional.keyEqualTo(Key.builder().partitionValue("PROJECT").build());

    SdkIterable<Page<Project>> pagedResults = index.query(r -> r
        .queryConditional(conditional)
        .scanIndexForward(false)
        .limit(limit));

    List<Project> projects = new ArrayList<>();
    for (Page<Project> page : pagedResults) {
      projects.addAll(page.items());
      if (projects.size() >= limit) {
        return projects.subList(0, limit);
      }
    }
    return projects;
  }
}
