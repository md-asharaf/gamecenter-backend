package com.adnibog.gamecenter.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.adnibog.gamecenter.dto.response.DashboardStatsResponse;
import com.adnibog.gamecenter.dto.model.GrowthData;
import com.adnibog.gamecenter.dto.model.ProjectStat;
import com.adnibog.gamecenter.dto.model.ProjectDto;

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
    long totalProjects = projectService.getTotalProjectsForAdmin(adminId);
    Integer totalAdmins = userService.getTotalAdminCount();

    List<ProjectDto> recentProjects = projectService.getMostRecentProjectsForAdmin(adminId, 5);

    List<ProjectStat> projectStats = new ArrayList<>();
    for (ProjectDto p : recentProjects) {
      long qCount = questionService.countByProjectId(p.getId());
      projectStats.add(new ProjectStat(p.getId(), p.getName(), qCount));
    }

    List<ProjectDto> allRecentProjects = projectService.getMostRecentProjectsForAdmin(adminId, 100);
    java.util.Map<String, Integer> growthMap = new java.util.LinkedHashMap<>();
    java.time.LocalDate now = java.time.LocalDate.now();
    for (int i = 5; i >= 0; i--) {
        growthMap.put(now.minusMonths(i).format(java.time.format.DateTimeFormatter.ofPattern("MMM")), 0);
    }
    for (ProjectDto p : allRecentProjects) {
        if (p.getCreatedAt() != null) {
            String month = java.time.Instant.ofEpochMilli(p.getCreatedAt())
                    .atZone(java.time.ZoneId.systemDefault())
                    .format(java.time.format.DateTimeFormatter.ofPattern("MMM"));
            if (growthMap.containsKey(month)) {
                growthMap.put(month, growthMap.get(month) + 1);
            }
        }
    }
    List<GrowthData> projectGrowth = new ArrayList<>();
    for (java.util.Map.Entry<String, Integer> entry : growthMap.entrySet()) {
        projectGrowth.add(new GrowthData(entry.getKey(), entry.getValue()));
    }

    return new DashboardStatsResponse((int) totalProjects, totalAdmins, projectGrowth, projectStats);
  }
}
