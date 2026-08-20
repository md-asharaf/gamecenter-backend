package com.adnibog.gamecenter.repository;

public interface AppStatsRepository {
  void incrementTotalAdmins();
  void decrementTotalAdmins();
  long getTotalAdmins();
  void incrementTotalProjects();
  void decrementTotalProjects();
  long getTotalProjects();
}
