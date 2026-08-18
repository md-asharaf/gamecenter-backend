package com.vocabkicker.serverless.repository;

import java.util.List;

public class QuestionPage {
    private List<com.vocabkicker.serverless.entity.Question> items;
    private String lastEvaluatedKey;

    public QuestionPage(List<com.vocabkicker.serverless.entity.Question> items, String lastEvaluatedKey) {
        this.items = items;
        this.lastEvaluatedKey = lastEvaluatedKey;
    }

    public List<com.vocabkicker.serverless.entity.Question> getItems() { return items; }
    public void setItems(List<com.vocabkicker.serverless.entity.Question> items) { this.items = items; }
    public String getLastEvaluatedKey() { return lastEvaluatedKey; }
    public void setLastEvaluatedKey(String lastEvaluatedKey) { this.lastEvaluatedKey = lastEvaluatedKey; }
}
