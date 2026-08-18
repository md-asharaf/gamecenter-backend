package com.adnibog.vocabkicker.scripts;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.util.HashMap;
import java.util.Map;

public class SeedData {

  public static void main(String[] args) {
    DynamoDbClient dynamoDb = DynamoDbClient.builder()
        .region(Region.AP_SOUTH_1)
        .build();
    BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    try {
      System.out.println("Seeding Admin User...");
      String adminEmail = "admin@vocabkicker.com";
      String adminPassword = "admin@123";
      String hashedPassword = passwordEncoder.encode(adminPassword);

      Map<String, AttributeValue> adminItem = new HashMap<>();
      adminItem.put("id", AttributeValue.builder().s(adminEmail).build());
      adminItem.put("passwordHash", AttributeValue.builder().s(hashedPassword).build());

      dynamoDb.putItem(PutItemRequest.builder()
          .tableName("Users")
          .item(adminItem)
          .build());
      System.out.println("✅ Admin user seeded! (" + adminEmail + ")");
    } catch (Exception e) {
      System.err.println("❌ Failed to seed:");
      e.printStackTrace();
    } finally {
      dynamoDb.close();
    }
  }
}
