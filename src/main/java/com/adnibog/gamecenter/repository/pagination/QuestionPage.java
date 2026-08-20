package com.adnibog.gamecenter.repository.pagination;

import java.util.List;

import com.adnibog.gamecenter.entity.Question;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionPage {
  private List<Question> items;
  private String lastEvaluatedKey;
}
