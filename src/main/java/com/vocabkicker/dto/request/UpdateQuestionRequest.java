package com.vocabkicker.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateQuestionRequest {
    private String word;
    private String mnemonic;
    private String definition;
}
