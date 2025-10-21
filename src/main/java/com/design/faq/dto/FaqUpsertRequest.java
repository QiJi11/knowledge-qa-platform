package com.design.faq.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class FaqUpsertRequest {
  @NotBlank(message = "question 必须是非空字符串")
  @Size(max = 500, message = "question 长度不能超过 500")
  private String question;

  @NotBlank(message = "answer 必须是非空字符串")
  @Size(max = 20000, message = "answer 长度不能超过 20000")
  private String answer;

  @Size(max = 500, message = "keywords 长度不能超过 500")
  private String keywords;

  @Size(max = 100, message = "category 长度不能超过 100")
  private String category;

  @Min(value = 0, message = "hitCount 必须是非负整数")
  private Integer hitCount;

  public String getQuestion() {
    return question;
  }

  public void setQuestion(String question) {
    this.question = question;
  }

  public String getAnswer() {
    return answer;
  }

  public void setAnswer(String answer) {
    this.answer = answer;
  }

  public String getKeywords() {
    return keywords;
  }

  public void setKeywords(String keywords) {
    this.keywords = keywords;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public Integer getHitCount() {
    return hitCount;
  }

  public void setHitCount(Integer hitCount) {
    this.hitCount = hitCount;
  }
}
