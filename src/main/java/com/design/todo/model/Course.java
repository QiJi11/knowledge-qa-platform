package com.design.todo.model;

public class Course {
  private String id;
  private String title;
  private String summary;
  private String description;
  private Integer status;
  private Integer priceCents;
  private String teacherName;
  private String createdAt;
  private String updatedAt;

  public Course() {}

  public Course(
    String id,
    String title,
    String summary,
    String description,
    Integer status,
    Integer priceCents,
    String teacherName,
    String createdAt,
    String updatedAt
  ) {
    this.id = id;
    this.title = title;
    this.summary = summary;
    this.description = description;
    this.status = status;
    this.priceCents = priceCents;
    this.teacherName = teacherName;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getSummary() {
    return summary;
  }

  public void setSummary(String summary) {
    this.summary = summary;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Integer getStatus() {
    return status;
  }

  public void setStatus(Integer status) {
    this.status = status;
  }

  public Integer getPriceCents() {
    return priceCents;
  }

  public void setPriceCents(Integer priceCents) {
    this.priceCents = priceCents;
  }

  public String getTeacherName() {
    return teacherName;
  }

  public void setTeacherName(String teacherName) {
    this.teacherName = teacherName;
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
