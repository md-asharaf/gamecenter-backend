package com.adnibog.vocabkicker.dto.request;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Data;
import java.util.HashMap;
import java.util.Map;

@Data
public class CreateQuestionRequest {
  private Map<String, String> dynamicFields = new HashMap<>();

  @JsonAnySetter
  public void setDynamicField(String key, String value) {
    dynamicFields.put(key, value);
  }
}
