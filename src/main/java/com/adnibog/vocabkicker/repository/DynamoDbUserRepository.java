package com.adnibog.vocabkicker.repository;

import org.springframework.stereotype.Repository;

import com.adnibog.vocabkicker.entity.User;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import java.util.Optional;

@Repository
public class DynamoDbUserRepository implements UserRepository {

  private final DynamoDbTable<User> userTable;

  public DynamoDbUserRepository(final DynamoDbEnhancedClient enhancedClient) {
    this.userTable = enhancedClient.table("Users", TableSchema.fromBean(User.class));
  }

  @Override
  public Optional<User> findByEmail(String email) {
    return userTable.scan().items().stream()
        .filter(u -> u.getEmail().equalsIgnoreCase(email))
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
  public long count() {
    return userTable.scan().items().stream().count();
  }

  @Override
  public java.util.List<User> findAll() {
    return new java.util.ArrayList<>(userTable.scan().items().stream().toList());
  }

  @Override
  public void deleteById(String id) {
    userTable.deleteItem(Key.builder().partitionValue(id).build());
  }
}
