package com.adnibog.vocabkicker.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.AssertTrue;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateQuestionRequest {
  private String word;
  private String mnemonic;
  private String definition;

  @AssertTrue(message = "At least one field (word, mnemonic, definition) must be provided to update")
  public boolean isAtLeastOneFieldProvided() {
    return (word != null && !word.isBlank()) ||
        (mnemonic != null && !mnemonic.isBlank()) ||
        (definition != null && !definition.isBlank());
  }
}
