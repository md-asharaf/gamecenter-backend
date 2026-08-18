package com.adnibog.vocabkicker.mapper;

import org.springframework.stereotype.Component;

import com.adnibog.vocabkicker.dto.response.QuestionDto;
import com.adnibog.vocabkicker.entity.Question;

@Component
public class QuestionMapper {

  public QuestionDto toDto(Question q) {
    if (q == null) {
      return null;
    }
    return QuestionDto.builder()
        .id(q.getId())
        .word(q.getWord())
        .mnemonic(q.getMnemonic())
        .definition(q.getDefinition())
        .createdAt(q.getCreatedAt())
        .updatedAt(q.getUpdatedAt())
        .build();
  }
}
