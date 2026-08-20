package com.adnibog.gamecenter.dto.response;

import com.adnibog.gamecenter.dto.model.QuestionDto;
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
