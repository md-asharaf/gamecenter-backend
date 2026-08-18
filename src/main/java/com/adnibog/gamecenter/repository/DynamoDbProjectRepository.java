package com.adnibog.gamecenter.repository;

import org.springframework.stereotype.Repository;

import com.adnibog.gamecenter.entity.Project;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class DynamoDbProjectRepository implements ProjectRepository {

  private final DynamoDbTable<Project> projectTable;

  public DynamoDbProjectRepository(final DynamoDbEnhancedClient enhancedClient) {
    this.projectTable = enhancedClient.table("Projects", TableSchema.fromBean(Project.class));
  }

  @Override
  public Optional<Project> findByProjectId(String projectId) {
    return Optional.ofNullable(projectTable.getItem(Key.builder().partitionValue(projectId).build()));
  }

  @Override
  public void save(Project project) {
    projectTable.putItem(project);
  }

  @Override
  public void deleteByProjectId(String projectId) {
    projectTable.deleteItem(Key.builder().partitionValue(projectId).build());
  }

  @Override
  public List<Project> findAll() {
    return projectTable.scan().items().stream().collect(Collectors.toList());
  }
}
