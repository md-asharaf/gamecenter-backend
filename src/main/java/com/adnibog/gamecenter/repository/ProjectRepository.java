package com.adnibog.gamecenter.repository;

import java.util.Optional;
import com.adnibog.gamecenter.repository.pagination.ProjectPage;
import java.util.List;

import com.adnibog.gamecenter.entity.Project;

public interface ProjectRepository {
  Optional<Project> findById(String projectId);

  Optional<Project> findByName(String name);

  void save(Project project);

  void deleteById(String projectId);

  List<Project> findAll();

  ProjectPage findProjects(int limit, String lastEvaluatedKeyId, String searchKeyword);
}
