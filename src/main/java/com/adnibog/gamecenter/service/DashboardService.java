package com.adnibog.gamecenter.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
  private final AppStatsService appStatsService;
  private final QuestionService questionService;
  private final UserService userService;

  public DashboardService(ProjectService projectService, AppStatsService appStatsService,
      QuestionService questionService, UserService userService) {
    this.projectService = projectService;
    this.appStatsService = appStatsService;
    this.questionService = questionService;
    this.userService = userService;
  }

  public DashboardStatsResponse getDashboardStats(String adminId) {
    com.adnibog.gamecenter.entity.User admin = userService.getUserEntityById(adminId);
    boolean isSuperAdmin = admin.getRole() == com.adnibog.gamecenter.entity.Role.SUPER_ADMIN;
    java.util.Set<String> projectIds = admin.getProjectIds();

    long totalProjects = isSuperAdmin ? appStatsService.getTotalProjects() : projectService.getTotalProjects(projectIds);
    Integer totalAdmins = (int) appStatsService.getTotalAdmins();

    List<ProjectDto> recentProjects = projectService.getMostRecentProjects(isSuperAdmin, projectIds, 5);

    List<ProjectStat> projectStats = new ArrayList<>();
    for (ProjectDto p : recentProjects) {
      long qCount = questionService.countByProjectId(p.getId());
      projectStats.add(new ProjectStat(p.getId(), p.getName(), qCount));
    }

    List<ProjectDto> allRecentProjects = projectService.getMostRecentProjects(isSuperAdmin, projectIds, 100);
    Map<String, Integer> growthMap = new LinkedHashMap<>();
    LocalDate now = java.time.LocalDate.now();
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
    for (Map.Entry<String, Integer> entry : growthMap.entrySet()) {
      projectGrowth.add(new GrowthData(entry.getKey(), entry.getValue()));
    }

    return new DashboardStatsResponse((int) totalProjects, totalAdmins, projectGrowth, projectStats);
  }
}
