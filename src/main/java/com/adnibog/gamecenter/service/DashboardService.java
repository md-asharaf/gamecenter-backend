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

    List<GrowthData> projectGrowth = new ArrayList<>();

    return new DashboardStatsResponse((int) totalProjects, totalAdmins, projectGrowth, projectStats);
  }
}
