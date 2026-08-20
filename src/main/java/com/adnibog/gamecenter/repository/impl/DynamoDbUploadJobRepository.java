package com.adnibog.gamecenter.repository.impl;

import com.adnibog.gamecenter.repository.UploadJobRepository;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import java.util.Optional;
import com.adnibog.gamecenter.entity.UploadJob;

@Repository
public class DynamoDbUploadJobRepository implements UploadJobRepository {

  private final DynamoDbTable<UploadJob> table;

  public DynamoDbUploadJobRepository(DynamoDbEnhancedClient enhancedClient) {
    this.table = enhancedClient.table("UploadJobs", TableSchema.fromBean(UploadJob.class));
  }

  @Override
  public Optional<UploadJob> findById(String id) {
    Key key = Key.builder().partitionValue(id).build();
    return Optional.ofNullable(table.getItem(key));
  }

  @Override
  public void save(UploadJob job) {
    table.putItem(job);
  }
}
