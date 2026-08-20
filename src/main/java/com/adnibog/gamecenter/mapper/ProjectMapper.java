package com.adnibog.gamecenter.mapper;

import org.springframework.stereotype.Component;

import com.adnibog.gamecenter.dto.model.ProjectDto;
import com.adnibog.gamecenter.entity.Project;

@Component
public class ProjectMapper {
  public ProjectDto toDto(Project project) {
    if (project == null) {
      return null;
    }
    return ProjectDto.builder()
        .id(project.getId())
        .name(project.getName())
        .numberOfQuestionsInQuiz(project.getNumberOfQuestionsInQuiz())
        .mainQuestionField(project.getMainQuestionField())
        .field1Label(project.getField1Label())
        .field2Label(project.getField2Label())
        .field3Label(project.getField3Label())
        .quizFolderId(project.getQuizFolderId())
        .createdAt(project.getCreatedAt())
        .updatedAt(project.getUpdatedAt())
        .build();
  }
}
