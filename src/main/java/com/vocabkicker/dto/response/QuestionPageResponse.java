package com.vocabkicker.dto.response;

import com.vocabkicker.entity.Question;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionPageResponse {
    private List<Question> items;
    private String lastEvaluatedKey;
}
