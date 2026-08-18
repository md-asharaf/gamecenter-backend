package com.vocabkicker.service;

import java.io.InputStream;
import java.util.Map;

public interface StorageService {
    Map<String, String> generateUploadUrl(String ext);
    InputStream getFileStream(String bucket, String key);
}
