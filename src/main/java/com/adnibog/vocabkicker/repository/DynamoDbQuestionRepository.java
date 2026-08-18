package com.adnibog.vocabkicker.repository;

import org.springframework.stereotype.Repository;

import com.adnibog.vocabkicker.entity.Question;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Repository
public class DynamoDbQuestionRepository implements QuestionRepository {

  private final DynamoDbTable<Question> questionTable;

  public DynamoDbQuestionRepository(final DynamoDbEnhancedClient enhancedClient) {
    this.questionTable = enhancedClient.table("Questions", TableSchema.fromBean(Question.class));
  }

  @Override
  public Optional<Question> findById(String id) {
    return Optional.ofNullable(questionTable.getItem(Key.builder().partitionValue(id).build()));
  }

  @Override
  public void save(Question question) {
    questionTable.putItem(question);
  }

  @Override
  public void deleteById(String id) {
    questionTable.deleteItem(Key.builder().partitionValue(id).build());
  }

  @Override
  public List<Question> findAll() {
    return questionTable.scan().items().stream().toList();
  }

  @Override
  public QuestionPage findQuestions(int limit, String lastEvaluatedKeyId, String searchKeyword) {
    Map<String, AttributeValue> exclusiveStartKey = null;
    if (lastEvaluatedKeyId != null && !lastEvaluatedKeyId.equals("null")) {
      exclusiveStartKey = new HashMap<>();
      exclusiveStartKey.put("id", AttributeValue.builder().s(lastEvaluatedKeyId).build());
    }

    final Map<String, AttributeValue> finalExclusiveStartKey = exclusiveStartKey;
    List<Question> resultItems = new ArrayList<>();
    String nextKey = null;

    if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
      Pattern pattern;
      try {
        pattern = Pattern.compile(searchKeyword, Pattern.CASE_INSENSITIVE);
      } catch (PatternSyntaxException e) {
        pattern = Pattern.compile(Pattern.quote(searchKeyword), Pattern.CASE_INSENSITIVE);
      }

      Iterator<Page<Question>> iterator = questionTable.scan(r -> {
        if (finalExclusiveStartKey != null) {
          r.exclusiveStartKey(finalExclusiveStartKey);
        }
      }).iterator();

      outerLoop: while (iterator.hasNext()) {
        Page<Question> page = iterator.next();
        for (Question q : page.items()) {
          boolean match = false;
          if (q.getWord() != null && pattern.matcher(q.getWord()).find())
            match = true;
          if (!match && q.getDefinition() != null && pattern.matcher(q.getDefinition()).find())
            match = true;

          if (match) {
            resultItems.add(q);
            if (resultItems.size() == limit) {
              nextKey = q.getId();
              break outerLoop;
            }
          }
        }
      }
    } else {
      Page<Question> firstPage = questionTable.scan(r -> {
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
}
