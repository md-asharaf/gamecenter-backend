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
import com.adnibog.gamecenter.entity.Project;
import com.adnibog.gamecenter.entity.Role;
import com.adnibog.gamecenter.entity.User;
import com.adnibog.gamecenter.repository.ProjectRepository;
import com.adnibog.gamecenter.repository.QuestionRepository;
import com.adnibog.gamecenter.repository.UserRepository;
import com.adnibog.gamecenter.dto.response.DashboardStatsResponse.ProjectStat;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DashboardService {

  private final ProjectRepository projectRepository;
  private final UserRepository userRepository;
  private final UserService userService;
  private final QuestionRepository questionRepository;

  public DashboardService(ProjectRepository projectRepository, UserRepository userRepository, UserService userService,
      QuestionRepository questionRepository) {
    this.projectRepository = projectRepository;
    this.userRepository = userRepository;
    this.userService = userService;
    this.questionRepository = questionRepository;
  }

  public DashboardStatsResponse getDashboardStats(String adminId) {
    User admin = userService.getUserEntityById(adminId);
    List<Project> allProjects;
    Integer totalAdmins = userRepository.findAll().size();

    if (admin.getRole() == Role.SUPER_ADMIN) {
      allProjects = projectRepository.findAll();
    } else {
      allProjects = new ArrayList<>();
      if (admin.getProjectIds() != null) {
        for (String pId : admin.getProjectIds()) {
          projectRepository.findById(pId).ifPresent(allProjects::add);
        }
      }
    }

    Map<String, Integer> chartDataMap = new LinkedHashMap<>();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d").withZone(ZoneId.systemDefault());

    for (int i = 6; i >= 0; i--) {
      String dateLabel = formatter.format(Instant.now().minusSeconds(i * 86400L));
      chartDataMap.put(dateLabel, 0);
    }

    for (Project p : allProjects) {
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
    for (Project p : allProjects) {
      long qCount = questionRepository.countByProjectId(p.getId());
      projectStats.add(new ProjectStat(p.getId(), p.getName(), qCount));
    }

    return new DashboardStatsResponse(allProjects.size(), totalAdmins, projectGrowth, projectStats);
  }
}
