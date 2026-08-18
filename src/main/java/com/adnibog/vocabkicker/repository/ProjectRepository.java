package com.adnibog.vocabkicker.repository;

import java.util.Optional;
import java.util.List;

import com.adnibog.vocabkicker.entity.Project;

public interface ProjectRepository {
  Optional<Project> findByProjectId(String projectId);

  void save(Project project);

  void deleteByProjectId(String projectId);

  List<Project> findAll();
}
