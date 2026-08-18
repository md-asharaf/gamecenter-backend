package com.adnibog.vocabkicker.mapper;

import org.springframework.stereotype.Component;
import com.adnibog.vocabkicker.dto.response.QuestionDto;
import com.adnibog.vocabkicker.dto.response.ProjectDto;
import com.adnibog.vocabkicker.entity.Question;

@Component
public class QuestionMapper {

  public QuestionDto toDto(Question q, ProjectDto projects) {
    if (q == null) {
      return null;
    }
    return QuestionDto.builder()
        .id(q.getId())
        .field1(q.getField1())
        .field2(q.getField2())
        .field3(q.getField3())
        .createdAt(q.getCreatedAt())
        .updatedAt(q.getUpdatedAt())
        .projects(projects)
        .build();
  }
}
