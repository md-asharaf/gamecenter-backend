package com.adnibog.gamecenter.service.storage;

import java.io.InputStream;

import com.adnibog.gamecenter.dto.response.UploadUrlResponse;

public interface StorageService {
  UploadUrlResponse generateUploadUrl(String projectId, String ext);

  InputStream getFileStream(String bucket, String key);
}
