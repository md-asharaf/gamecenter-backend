package com.adnibog.gamecenter.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.net.URI;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.adnibog.gamecenter.dto.response.UploadUrlResponse;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

  @Mock
  private S3Presigner s3Presigner;

  @Mock
  private S3Client s3Client;

  @Mock
  private UploadJobService uploadJobService;

  private S3Service s3Service;

  @BeforeEach
  void setUp() {
    s3Service = new S3Service(s3Presigner, s3Client, uploadJobService, "test-bucket");
  }

  @Test
  void generateUploadUrl_Csv() throws Exception {
    String projectId = "proj1";
    String ext = "csv";

    PresignedPutObjectRequest presignedReq = mock(PresignedPutObjectRequest.class);
    when(presignedReq.url()).thenReturn(URI.create("https://s3.amazonaws.com/test").toURL());
    when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(presignedReq);

    UploadUrlResponse result = s3Service.generateUploadUrl(projectId, ext);

    assertNotNull(result);
    assertEquals("https://s3.amazonaws.com/test", result.getUrl());
    assertTrue(result.getKey().startsWith(projectId + "/"));
    assertTrue(result.getKey().endsWith(".csv"));
  }

  @Test
  void generateUploadUrl_Docx() throws Exception {
    String projectId = "proj1";
    String ext = "docx";

    PresignedPutObjectRequest presignedReq = mock(PresignedPutObjectRequest.class);
    when(presignedReq.url()).thenReturn(URI.create("https://s3.amazonaws.com/test").toURL());
    when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(presignedReq);

    UploadUrlResponse result = s3Service.generateUploadUrl(projectId, ext);

    assertNotNull(result);
    assertEquals("https://s3.amazonaws.com/test", result.getUrl());
    assertTrue(result.getKey().startsWith(projectId + "/"));
    assertTrue(result.getKey().endsWith(".docx"));
  }
}
