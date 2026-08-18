package com.adnibog.gamecenter.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionPageResponse {
  private List<QuestionDto> items;
  private String lastEvaluatedKey;
}
