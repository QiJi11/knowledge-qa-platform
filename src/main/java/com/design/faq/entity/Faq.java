package com.design.faq.entity;

public class Faq {
  private Long id;
  private String question;
  private String answer;
  private String keywords;
  private String category;
  private Integer hitCount;
  private String createdAt;
  private String updatedAt;

  public Faq() {}

  public Faq(
    Long id,
    String question,
    String answer,
    String keywords,
    String category,
    Integer hitCount,
    String createdAt,
    String updatedAt
  ) {
    this.id = id;
    this.question = question;
    this.answer = answer;
    this.keywords = keywords;
    this.category = category;
    this.hitCount = hitCount;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

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

  public String getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(String createdAt) {
    this.createdAt = createdAt;
  }

  public String getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(String updatedAt) {
    this.updatedAt = updatedAt;
  }
}
