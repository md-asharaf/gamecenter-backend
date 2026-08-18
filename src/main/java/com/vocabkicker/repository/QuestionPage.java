package com.vocabkicker.repository;

import java.util.List;

public class QuestionPage {
    private List<com.vocabkicker.entity.Question> items;
    private String lastEvaluatedKey;

    public QuestionPage(List<com.vocabkicker.entity.Question> items, String lastEvaluatedKey) {
        this.items = items;
        this.lastEvaluatedKey = lastEvaluatedKey;
    }

    public List<com.vocabkicker.entity.Question> getItems() { return items; }
    public void setItems(List<com.vocabkicker.entity.Question> items) { this.items = items; }
    public String getLastEvaluatedKey() { return lastEvaluatedKey; }
    public void setLastEvaluatedKey(String lastEvaluatedKey) { this.lastEvaluatedKey = lastEvaluatedKey; }
}
