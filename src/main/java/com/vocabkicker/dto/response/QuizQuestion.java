package com.vocabkicker.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizQuestion {
  private String definition;
  private String answer;
  private String mnemonic;
  private List<String> options;
}
