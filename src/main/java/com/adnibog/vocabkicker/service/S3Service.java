package com.adnibog.vocabkicker.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.adnibog.vocabkicker.dto.response.UploadUrlResponse;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.InputStream;
import java.time.Duration;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class S3Service implements StorageService {

  private final S3Presigner presigner;
  private final S3Client s3Client;
  private final String bucketName;

  public S3Service(@Value("${aws.s3.import-bucket-name}") String bucketName) {
    this.presigner = S3Presigner.create();
    this.s3Client = S3Client.builder()
        .region(software.amazon.awssdk.regions.Region.AP_SOUTH_1)
        .httpClientBuilder(software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient.builder())
        .build();
    this.bucketName = bucketName;
  }

  @Override
  public UploadUrlResponse generateUploadUrl(String projectId, String ext) {
    String extension = ".csv";
    if ("docx".equalsIgnoreCase(ext)) {
      extension = ".docx";
    }

    String key = projectId + "/" + UUID.randomUUID().toString() + extension;

    PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
        .signatureDuration(Duration.ofMinutes(10))
        .putObjectRequest(b -> b.bucket(bucketName).key(key))
        .build();

    PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(presignRequest);

    log.info("Generated presigned upload URL for project {} (key: {})", projectId, key);
    return new UploadUrlResponse(presignedRequest.url().toString(), key);
  }

  @Override
  public InputStream getFileStream(String bucket, String key) {
    return s3Client.getObject(GetObjectRequest.builder()
        .bucket(bucket)
        .key(key)
        .build());
  }
}
