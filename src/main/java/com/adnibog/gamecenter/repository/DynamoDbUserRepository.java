package com.adnibog.gamecenter.repository;

import org.springframework.stereotype.Repository;

import com.adnibog.gamecenter.entity.User;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;
import software.amazon.awssdk.services.dynamodb.model.Select;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class DynamoDbUserRepository implements UserRepository {

  private final DynamoDbTable<User> userTable;
  private final DynamoDbClient dynamoDbClient;

  public DynamoDbUserRepository(final DynamoDbEnhancedClient enhancedClient, final DynamoDbClient dynamoDbClient) {
    this.userTable = enhancedClient.table("Users", TableSchema.fromBean(User.class));
    this.dynamoDbClient = dynamoDbClient;
  }

  @Override
  public Optional<User> findByEmail(String email) {
    QueryConditional conditional = QueryConditional.keyEqualTo(Key.builder().partitionValue(email).build());
    return userTable.index("email-index").query(conditional).stream()
        .flatMap(page -> page.items().stream())
        .findFirst();
  }

  @Override
  public Optional<User> findById(String id) {
    return Optional.ofNullable(userTable.getItem(Key.builder().partitionValue(id).build()));
  }

  @Override
  public void save(User user) {
    userTable.putItem(user);
  }

  @Override
  public java.util.List<User> findAll() {
    return new java.util.ArrayList<>(userTable.scan().items().stream().toList());
  }

  @Override
  public long countAll() {
    long totalCount = 0;
    Map<String, AttributeValue> lastEvaluatedKey = null;
    do {
      ScanRequest scanRequest = ScanRequest.builder()
          .tableName("Users")
          .select(Select.COUNT)
          .exclusiveStartKey(lastEvaluatedKey)
          .build();
      ScanResponse response = dynamoDbClient.scan(scanRequest);
      totalCount += response.count();
      lastEvaluatedKey = response.lastEvaluatedKey();
    } while (lastEvaluatedKey != null && !lastEvaluatedKey.isEmpty());
    return totalCount;
  }

  @Override
  public void deleteById(String id) {
    userTable.deleteItem(Key.builder().partitionValue(id).build());
  }

  @Override
  public UserPage findUsers(int limit, String lastEvaluatedKeyId, String searchKeyword) {
    Map<String, AttributeValue> exclusiveStartKey = null;
    if (lastEvaluatedKeyId != null && !lastEvaluatedKeyId.equals("null") && !lastEvaluatedKeyId.trim().isEmpty()) {
      exclusiveStartKey = new HashMap<>();
      exclusiveStartKey.put("id", AttributeValue.builder().s(lastEvaluatedKeyId).build());
    }

    final Map<String, AttributeValue> finalExclusiveStartKey = exclusiveStartKey;
    List<User> resultItems = new ArrayList<>();
    String nextKey = null;

    Expression filterExpression = null;
    if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
      Map<String, AttributeValue> expressionValues = new HashMap<>();
      expressionValues.put(":searchKeyword", AttributeValue.builder().s(searchKeyword).build());
      Map<String, String> expressionNames = new HashMap<>();
      expressionNames.put("#email", "email");

      filterExpression = Expression.builder()
          .expression("contains(#email, :searchKeyword)")
          .expressionNames(expressionNames)
          .expressionValues(expressionValues)
          .build();
    }

    final Expression finalFilterExpression = filterExpression;

    Iterator<Page<User>> iterator = userTable.scan(r -> {
      if (finalExclusiveStartKey != null) {
        r.exclusiveStartKey(finalExclusiveStartKey);
      }
      if (finalFilterExpression != null) {
        r.filterExpression(finalFilterExpression);
      }
    }).iterator();

    outerLoop: while (iterator.hasNext()) {
      Page<User> page = iterator.next();
      for (User u : page.items()) {
        resultItems.add(u);
        if (resultItems.size() == limit) {
          nextKey = u.getId();
          break outerLoop;
        }
      }
    }

    return new UserPage(resultItems, nextKey);
  }
}