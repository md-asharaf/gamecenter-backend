package com.adnibog.vocabkicker.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadUrlResponse {
  private String url;
  private String key;
}
