package com.adnibog.vocabkicker.dto.response;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"field1", "field2", "field3", "projects"})
public class QuizQuestion {
  private String answer;
  private List<String> options;

  @JsonIgnore
  private String field1;
  @JsonIgnore
  private String field2;
  @JsonIgnore
  private String field3;

  @JsonIgnore
  private ProjectDto projects;

  @JsonAnyGetter
  public Map<String, String> getDynamicProperties() {
    Map<String, String> map = new HashMap<>();
    if (projects != null) {
      if (field1 != null) map.put(projects.getField1Label(), field1);
      if (field2 != null) map.put(projects.getField2Label(), field2);
      if (field3 != null) map.put(projects.getField3Label(), field3);
    }
    return map;
  }
}
