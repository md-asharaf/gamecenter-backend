package com.adnibog.gamecenter.service;

import com.adnibog.gamecenter.repository.AppStatsRepository;
import org.springframework.stereotype.Service;
import org.springframework.context.event.EventListener;
import com.adnibog.gamecenter.event.ProjectDeletedEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AppStatsService {
  private final AppStatsRepository repository;

  public AppStatsService(AppStatsRepository repository) {
    this.repository = repository;
  }

  public void incrementTotalAdmins() { repository.incrementTotalAdmins(); }
  public void decrementTotalAdmins() { repository.decrementTotalAdmins(); }
  public long getTotalAdmins() { return repository.getTotalAdmins(); }
  public void incrementTotalProjects() { repository.incrementTotalProjects(); }
  public void decrementTotalProjects() { repository.decrementTotalProjects(); }
  public long getTotalProjects() { return repository.getTotalProjects(); }

  @EventListener
  public void handleProjectDeletedEvent(ProjectDeletedEvent event) {
    log.info("Handling ProjectDeletedEvent in AppStatsService for project {}", event.getProjectId());
    decrementTotalProjects();
  }

  @EventListener
  public void handleProjectCreatedEvent(com.adnibog.gamecenter.event.ProjectCreatedEvent event) {
    log.info("Handling ProjectCreatedEvent in AppStatsService for project {}", event.getProjectId());
    incrementTotalProjects();
  }
}
