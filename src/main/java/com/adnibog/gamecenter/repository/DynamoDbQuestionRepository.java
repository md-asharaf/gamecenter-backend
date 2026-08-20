package com.adnibog.gamecenter.repository;

import org.springframework.stereotype.Repository;

import com.adnibog.gamecenter.entity.Question;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.WriteBatch;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchWriteItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchWriteResult;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Repository
public class DynamoDbQuestionRepository implements QuestionRepository {

  private final DynamoDbTable<Question> questionTable;
  private final DynamoDbEnhancedClient enhancedClient;
  private final DynamoDbClient dynamoDbClient;

  public DynamoDbQuestionRepository(final DynamoDbEnhancedClient enhancedClient, final DynamoDbClient dynamoDbClient) {
    this.enhancedClient = enhancedClient;
    this.dynamoDbClient = dynamoDbClient;
    this.questionTable = enhancedClient.table("Questions", TableSchema.fromBean(Question.class));
  }

  @Override
  public Optional<Question> findById(String projectId, String id) {
    return Optional.ofNullable(questionTable.getItem(Key.builder().partitionValue(projectId).sortValue(id).build()));
  }

  @Override
  public void save(Question question) {
    questionTable.putItem(question);
  }

  @Override
  public void saveAll(List<Question> questions) {
    if (questions == null || questions.isEmpty()) {
      return;
    }

    int batchSize = 25;
    for (int i = 0; i < questions.size(); i += batchSize) {
      int end = Math.min(i + batchSize, questions.size());
      List<Question> chunk = questions.subList(i, end);

      WriteBatch.Builder<Question> batchBuilder = WriteBatch.builder(Question.class).mappedTableResource(questionTable);

      for (Question q : chunk) {
        batchBuilder.addPutItem(r -> r.item(q));
      }

      BatchWriteItemEnhancedRequest batchRequest = BatchWriteItemEnhancedRequest.builder()
          .addWriteBatch(batchBuilder.build())
          .build();

      BatchWriteResult result = enhancedClient.batchWriteItem(batchRequest);

      while (!result.unprocessedPutItemsForTable(questionTable).isEmpty()) {
        try {
          Thread.sleep(50);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }

        batchBuilder = WriteBatch.builder(Question.class).mappedTableResource(questionTable);
        for (Question req : result.unprocessedPutItemsForTable(questionTable)) {
          batchBuilder.addPutItem(r -> r.item(req));
        }

        batchRequest = BatchWriteItemEnhancedRequest.builder()
            .addWriteBatch(batchBuilder.build())
            .build();
        result = enhancedClient.batchWriteItem(batchRequest);
      }
    }
  }

  @Override
  public void deleteById(String projectId, String id) {
    questionTable.deleteItem(Key.builder().partitionValue(projectId).sortValue(id).build());
  }

  @Override
  public void deleteAllByProjectId(String projectId) {
    QueryConditional conditional = QueryConditional.keyEqualTo(Key.builder().partitionValue(projectId).build());
    questionTable.query(conditional).items().forEach(q -> deleteById(projectId, q.getId()));
  }

  @Override
  public List<Question> findAll(String projectId) {
    QueryConditional conditional = QueryConditional.keyEqualTo(Key.builder().partitionValue(projectId).build());
    return questionTable.query(conditional).items().stream().toList();
  }

  @Override
  public QuestionPage findQuestions(String projectId, int limit, String lastEvaluatedKeyId, String searchKeyword) {
    Map<String, AttributeValue> exclusiveStartKey = null;
    if (lastEvaluatedKeyId != null && !lastEvaluatedKeyId.equals("null")) {
      exclusiveStartKey = new HashMap<>();
      exclusiveStartKey.put("projectId", AttributeValue.builder().s(projectId).build());
      exclusiveStartKey.put("id", AttributeValue.builder().s(lastEvaluatedKeyId).build());
    }

    final Map<String, AttributeValue> finalExclusiveStartKey = exclusiveStartKey;
    List<Question> resultItems = new ArrayList<>();
    String nextKey = null;

    QueryConditional conditional = QueryConditional.keyEqualTo(Key.builder().partitionValue(projectId).build());

    if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
      Expression filterExpression = Expression.builder()
          .expression("contains(field1, :v) OR contains(field2, :v) OR contains(field3, :v)")
          .putExpressionValue(":v", AttributeValue.builder().s(searchKeyword).build())
          .build();

      Iterator<Page<Question>> iterator = questionTable.query(r -> {
        r.queryConditional(conditional);
        r.filterExpression(filterExpression);
        if (finalExclusiveStartKey != null) {
          r.exclusiveStartKey(finalExclusiveStartKey);
        }
      }).iterator();

      outerLoop: while (iterator.hasNext()) {
        Page<Question> page = iterator.next();
        for (Question q : page.items()) {
          resultItems.add(q);
          if (resultItems.size() == limit) {
            nextKey = q.getId();
            break outerLoop;
          }
        }
      }
    } else {
      Page<Question> firstPage = questionTable.query(r -> {
        r.queryConditional(conditional);
        r.limit(limit);
        if (finalExclusiveStartKey != null) {
          r.exclusiveStartKey(finalExclusiveStartKey);
        }
      }).stream().findFirst().orElse(null);

      if (firstPage != null) {
        resultItems.addAll(firstPage.items());
        if (firstPage.lastEvaluatedKey() != null && firstPage.lastEvaluatedKey().containsKey("id")) {
          nextKey = firstPage.lastEvaluatedKey().get("id").s();
        }
      }
    }

    return new QuestionPage(resultItems, nextKey);
  }

  @Override
  public List<Question> findRandomQuestions(String projectId, int amount) {
    String randomStart = UUID.randomUUID().toString();
    QueryConditional forward = QueryConditional.sortGreaterThanOrEqualTo(
        Key.builder().partitionValue(projectId).sortValue(randomStart).build());

    List<Question> results = new ArrayList<>();
    Iterator<Page<Question>> iterator = questionTable.query(r -> r.queryConditional(forward).limit(amount)).iterator();
    if (iterator.hasNext()) {
      results.addAll(iterator.next().items());
    }

    if (results.size() < amount) {
      QueryConditional wrap = QueryConditional.keyEqualTo(Key.builder().partitionValue(projectId).build());
      Iterator<Page<Question>> wrapIter = questionTable
          .query(r -> r.queryConditional(wrap).limit(amount - results.size())).iterator();
      if (wrapIter.hasNext()) {
        results.addAll(wrapIter.next().items());
      }
    }
    return results;
  }

  @Override
  public long countByProjectId(String projectId) {
    QueryRequest queryRequest = QueryRequest
        .builder()
        .tableName("Questions")
        .keyConditionExpression("projectId = :v_id")
        .expressionAttributeValues(Map.of(":v_id", AttributeValue.builder().s(projectId).build()))
        .select(software.amazon.awssdk.services.dynamodb.model.Select.COUNT)
        .build();
    return dynamoDbClient.query(queryRequest).count();
  }
}
