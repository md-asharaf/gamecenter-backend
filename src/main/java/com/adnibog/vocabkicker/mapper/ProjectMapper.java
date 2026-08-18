package com.adnibog.vocabkicker.mapper;

import org.springframework.stereotype.Component;

import com.adnibog.vocabkicker.dto.response.ProjectDto;
import com.adnibog.vocabkicker.entity.Project;

@Component
public class ProjectMapper {
  public ProjectDto toDto(Project project) {
    if (project == null) {
      return null;
    }
    return ProjectDto.builder()
        .projectId(project.getProjectId())
        .numberOfQuestionsInQuiz(project.getNumberOfQuestionsInQuiz())
        .mainQuestionField(project.getMainQuestionField())
        .field1Label(project.getField1Label())
        .field2Label(project.getField2Label())
        .field3Label(project.getField3Label())
        .createdAt(project.getCreatedAt())
        .updatedAt(project.getUpdatedAt())
        .build();
  }
}
