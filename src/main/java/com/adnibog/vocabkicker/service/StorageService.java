package com.adnibog.vocabkicker.service;

import java.io.InputStream;

import com.adnibog.vocabkicker.dto.response.UploadUrlResponse;

public interface StorageService {
  UploadUrlResponse generateUploadUrl(String projectId, String ext);

  InputStream getFileStream(String bucket, String key);
}
