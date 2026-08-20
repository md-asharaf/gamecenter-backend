package com.adnibog.gamecenter.dto.response;

import com.adnibog.gamecenter.dto.model.ProjectDto;
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
