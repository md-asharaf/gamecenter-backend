package com.adnibog.gamecenter.repository;

import java.util.List;

import com.adnibog.gamecenter.entity.Question;

public class QuestionPage {
  private List<Question> items;
  private String lastEvaluatedKey;

  public QuestionPage(List<Question> items, String lastEvaluatedKey) {
    this.items = items;
    this.lastEvaluatedKey = lastEvaluatedKey;
  }

  public List<Question> getItems() {
    return items;
  }

  public void setItems(List<Question> items) {
    this.items = items;
  }

  public String getLastEvaluatedKey() {
    return lastEvaluatedKey;
  }

  public void setLastEvaluatedKey(String lastEvaluatedKey) {
    this.lastEvaluatedKey = lastEvaluatedKey;
  }
}
