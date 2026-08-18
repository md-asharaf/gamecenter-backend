package com.adnibog.gamecenter.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectPageResponse {
  private List<ProjectDto> items;
  private String lastEvaluatedKey;
}
