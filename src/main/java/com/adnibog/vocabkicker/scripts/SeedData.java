package com.adnibog.vocabkicker.scripts;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.io.File;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SeedData {

  public static void main(String[] args) {
    DynamoDbClient dynamoDb = DynamoDbClient.builder()
        .region(Region.AP_SOUTH_1)
        .build();
    BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    ObjectMapper objectMapper = new ObjectMapper();

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

      System.out.println("Seeding Questions from data.json...");
      File dataFile = new File("../data.json");
      if (dataFile.exists()) {
        List<Map<String, Object>> questions = objectMapper.readValue(dataFile, new TypeReference<>() {
        });

        int count = 0;
        for (Map<String, Object> q : questions) {
          String word = (String) q.get("field1");
          String mnemonic = (String) q.get("field3");
          String definition = (String) q.get("field2");

          if (word == null || definition == null) {
            System.err.println("Skipping invalid question in default-questions.json: " + q);
            continue;
          }

          String id = Base64.getEncoder().encodeToString(word.getBytes());

          Map<String, AttributeValue> questionItem = new HashMap<>();
          questionItem.put("projectId", AttributeValue.builder().s("default").build());
          questionItem.put("id", AttributeValue.builder().s(id).build());
          questionItem.put("field1", AttributeValue.builder().s(word).build());
          if (mnemonic != null)
            questionItem.put("field3", AttributeValue.builder().s(mnemonic).build());
          if (definition != null)
            questionItem.put("field2", AttributeValue.builder().s(definition).build());

          dynamoDb.putItem(PutItemRequest.builder()
              .tableName("Questions")
              .item(questionItem)
              .build());
          count++;
        }
        System.out.println("✅ Seeded " + count + " questions into DynamoDB!");

        if (dataFile.delete()) {
          System.out.println("🗑️ Deleted data.json from ../data.json.");
        }
      } else {
        System.out.println("⚠️ data.json not found, assuming already seeded and deleted.");
      }

    } catch (Exception e) {
      System.err.println("❌ Failed to seed:");
      e.printStackTrace();
    } finally {
      dynamoDb.close();
    }
  }
}
