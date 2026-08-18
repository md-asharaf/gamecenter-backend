package com.adnibog.gamecenter.repository;

import java.util.List;
import com.adnibog.gamecenter.entity.Project;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectPage {
  private List<Project> items;
  private String lastEvaluatedKey;
}
