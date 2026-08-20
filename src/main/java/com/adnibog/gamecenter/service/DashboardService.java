package com.adnibog.gamecenter.service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.adnibog.gamecenter.dto.response.DashboardStatsResponse;
import com.adnibog.gamecenter.dto.response.GrowthData;
import com.adnibog.gamecenter.dto.response.ProjectDto;
import com.adnibog.gamecenter.dto.response.DashboardStatsResponse.ProjectStat;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DashboardService {

  private final ProjectService projectService;
  private final UserService userService;
  private final QuestionService questionService;

  public DashboardService(ProjectService projectService, UserService userService, QuestionService questionService) {
    this.projectService = projectService;
    this.userService = userService;
    this.questionService = questionService;
  }

  public DashboardStatsResponse getDashboardStats(String adminId) {
    List<ProjectDto> allProjects = projectService.getAllProjectsForAdmin(adminId);
    Integer totalAdmins = userService.getTotalAdminCount();

    Map<String, Integer> chartDataMap = new LinkedHashMap<>();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d").withZone(ZoneId.systemDefault());

    for (int i = 6; i >= 0; i--) {
      String dateLabel = formatter.format(Instant.now().minusSeconds(i * 86400L));
      chartDataMap.put(dateLabel, 0);
    }

    for (ProjectDto p : allProjects) {
      if (p.getCreatedAt() != null && p.getCreatedAt() > 0) {
        String label = formatter.format(Instant.ofEpochMilli(p.getCreatedAt()));
        if (chartDataMap.containsKey(label)) {
          chartDataMap.put(label, chartDataMap.get(label) + 1);
        }
      }
    }

    List<GrowthData> projectGrowth = new ArrayList<>();
    int cumulative = 0;
    for (Map.Entry<String, Integer> entry : chartDataMap.entrySet()) {
      cumulative += entry.getValue();
      projectGrowth.add(new GrowthData(entry.getKey(), cumulative));
    }

    List<ProjectStat> projectStats = new ArrayList<>();
    for (ProjectDto p : allProjects) {
      long qCount = questionService.countByProjectId(p.getId());
      projectStats.add(new ProjectStat(p.getId(), p.getName(), qCount));
    }

    return new DashboardStatsResponse(allProjects.size(), totalAdmins, projectGrowth, projectStats);
  }
}
