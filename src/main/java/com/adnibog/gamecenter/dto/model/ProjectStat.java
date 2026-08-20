package com.adnibog.gamecenter.dto.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectStat {
  private String projectId;
  private String projectName;
  private long questionCount;
}
