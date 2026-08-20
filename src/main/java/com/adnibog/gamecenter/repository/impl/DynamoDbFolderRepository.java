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
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import com.adnibog.gamecenter.dto.request.PaginationRequest;
import com.adnibog.gamecenter.repository.pagination.FolderPage;

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

    String expression = "#n = :n";
    if (excludeFolderId != null && !excludeFolderId.isEmpty()) {
      expressionNames.put("#id", "id");
      expressionValues.put(":id", AttributeValue.builder().s(excludeFolderId).build());
      expression += " AND #id <> :id";
    }

    Expression filterExpression = Expression.builder()
        .expression(expression)
        .expressionNames(expressionNames)
        .expressionValues(expressionValues)
        .build();

    return folderTable.query(r -> r.queryConditional(conditional).filterExpression(filterExpression).limit(1)).items()
        .iterator().hasNext();
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

    SdkIterable<Page<Folder>> pagedResults;
    if (startKey != null) {
      final Map<String, AttributeValue> finalStartKey = startKey;
      pagedResults = folderTable.query(r -> {
        r.queryConditional(conditional).limit(req.getLimit()).exclusiveStartKey(finalStartKey);
      });
    } else {
      pagedResults = folderTable.query(r -> {
        r.queryConditional(conditional).limit(req.getLimit());
      });
    }

    Iterator<Page<Folder>> iterator = pagedResults.iterator();
    if (iterator.hasNext()) {
      Page<Folder> page = iterator.next();
      String nextKey = null;
      if (page.lastEvaluatedKey() != null && page.lastEvaluatedKey().containsKey("id")) {
        nextKey = page.lastEvaluatedKey().get("id").s();
      }
      return new FolderPage(page.items(), nextKey);
    }

    return new FolderPage(List.of(), null);
  }
}
