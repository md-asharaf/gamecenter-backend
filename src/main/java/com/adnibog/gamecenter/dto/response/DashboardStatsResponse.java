package com.adnibog.gamecenter.dto.response;

import com.adnibog.gamecenter.dto.model.GrowthData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
  private int totalProjects;
  private Integer totalAdmins;
  private List<GrowthData> projectGrowth;
  private List<ProjectStat> projectStats;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ProjectStat {
    private String projectId;
    private String projectName;
    private long questionCount;
  }
}
