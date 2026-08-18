package com.adnibog.vocabkicker.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionDto {
  private String id;
  private String word;
  private String mnemonic;
  private String definition;
  private Long createdAt;
  private Long updatedAt;
}
