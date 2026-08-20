package com.adnibog.gamecenter.repository.impl;

import com.adnibog.gamecenter.repository.FolderRepository;
import org.springframework.stereotype.Repository;

import com.adnibog.gamecenter.entity.Folder;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import com.adnibog.gamecenter.dto.request.PaginationRequest;
import com.adnibog.gamecenter.repository.pagination.FolderPage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class DynamoDbFolderRepository implements FolderRepository {

  private final DynamoDbTable<Folder> folderTable;

  public DynamoDbFolderRepository(final DynamoDbEnhancedClient enhancedClient) {
    this.folderTable = enhancedClient.table("Folders", TableSchema.fromBean(Folder.class));
  }

  @Override
  public Optional<Folder> findById(String projectId, String folderId) {
    return Optional
        .ofNullable(folderTable.getItem(Key.builder().partitionValue(projectId).sortValue(folderId).build()));
  }

  @Override
  public void save(Folder folder) {
    folderTable.putItem(folder);
  }

  @Override
  public void deleteById(String projectId, String folderId) {
    folderTable.deleteItem(Key.builder().partitionValue(projectId).sortValue(folderId).build());
  }

  @Override
  public void deleteAllByProjectId(String projectId) {
    QueryConditional conditional = QueryConditional.keyEqualTo(Key.builder().partitionValue(projectId).build());
    folderTable.query(r -> r.queryConditional(conditional)).items().forEach(folder -> {
      folderTable.deleteItem(folder);
    });
  }

  @Override
  public boolean hasAnyFolders(String projectId) {
    QueryConditional conditional = QueryConditional.keyEqualTo(Key.builder().partitionValue(projectId).build());
    return folderTable.query(r -> r.queryConditional(conditional).limit(1)).items().iterator().hasNext();
  }

  @Override
  public boolean existsByProjectIdAndName(String projectId, String name, String excludeFolderId) {
    QueryConditional conditional = QueryConditional.keyEqualTo(Key.builder().partitionValue(projectId).build());
    Map<String, String> expressionNames = new HashMap<>();
    expressionNames.put("#n", "name");
    Map<String, AttributeValue> expressionValues = new HashMap<>();
    expressionValues.put(":n", AttributeValue.builder().s(name).build());

    Expression filterExpression = Expression.builder()
        .expression("#n = :n")
        .expressionNames(expressionNames)
        .expressionValues(expressionValues)
        .build();

    for (Page<Folder> page : folderTable
        .query(r -> r.queryConditional(conditional).filterExpression(filterExpression))) {
      for (Folder f : page.items()) {
        if (excludeFolderId == null || !f.getId().equals(excludeFolderId)) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public FolderPage getFolders(String projectId, PaginationRequest req) {
    QueryConditional conditional = QueryConditional.keyEqualTo(Key.builder().partitionValue(projectId).build());

    Map<String, AttributeValue> startKey = null;
    if (req.getLastEvaluatedKey() != null && !req.getLastEvaluatedKey().isEmpty()) {
      startKey = new HashMap<>();
      startKey.put("projectId", AttributeValue.builder().s(projectId).build());
      startKey.put("id", AttributeValue.builder().s(req.getLastEvaluatedKey()).build());
    }

    Expression filterExpression = null;
    if (req.getSearch() != null && !req.getSearch().trim().isEmpty()) {
      Map<String, AttributeValue> expressionValues = new HashMap<>();
      expressionValues.put(":searchKeyword", AttributeValue.builder().s(req.getSearch()).build());
      Map<String, String> expressionNames = new HashMap<>();
      expressionNames.put("#name", "name");

      filterExpression = Expression.builder()
          .expression("contains(#name, :searchKeyword)")
          .expressionNames(expressionNames)
          .expressionValues(expressionValues)
          .build();
    }
    final Expression finalFilterExpression = filterExpression;
    final Map<String, AttributeValue> finalStartKey = startKey;

    Iterator<Page<Folder>> iterator = folderTable.query(r -> {
      r.queryConditional(conditional);
      if (finalStartKey != null) {
        r.exclusiveStartKey(finalStartKey);
      }
      if (finalFilterExpression != null) {
        r.filterExpression(finalFilterExpression);
      }
    }).iterator();

    List<Folder> resultItems = new ArrayList<>();
    String nextKey = null;

    while (iterator.hasNext() && resultItems.size() < req.getLimit()) {
      Page<Folder> page = iterator.next();
      for (Folder item : page.items()) {
        resultItems.add(item);
        if (resultItems.size() == req.getLimit()) {
          break;
        }
      }
      if (page.lastEvaluatedKey() != null && page.lastEvaluatedKey().containsKey("id")) {
        nextKey = page.lastEvaluatedKey().get("id").s();
      } else {
        nextKey = null;
      }
      if (resultItems.size() == req.getLimit()) {
        break;
      }
    }

    return new FolderPage(resultItems, nextKey);
  }
}
