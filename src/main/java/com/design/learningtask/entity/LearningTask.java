package com.design.learningtask.entity;

public class LearningTask {
  private Long id;
  private Long learnerId;
  private Long courseId;
  private String title;
  private String note;
  private Integer status;
  private Integer priority;
  private String dueDate;
  private String completedAt;
  private String createdAt;
  private String updatedAt;

  public LearningTask() {}

  public LearningTask(
    Long id,
    Long learnerId,
    Long courseId,
    String title,
    String note,
    Integer status,
    Integer priority,
    String dueDate,
    String completedAt,
    String createdAt,
    String updatedAt
  ) {
    this.id = id;
    this.learnerId = learnerId;
    this.courseId = courseId;
    this.title = title;
    this.note = note;
    this.status = status;
    this.priority = priority;
    this.dueDate = dueDate;
    this.completedAt = completedAt;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getLearnerId() {
    return learnerId;
  }

  public void setLearnerId(Long learnerId) {
    this.learnerId = learnerId;
  }

  public Long getCourseId() {
    return courseId;
  }

  public void setCourseId(Long courseId) {
    this.courseId = courseId;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getNote() {
    return note;
  }

  public void setNote(String note) {
    this.note = note;
  }

  public Integer getStatus() {
    return status;
  }

  public void setStatus(Integer status) {
    this.status = status;
  }

  public Integer getPriority() {
    return priority;
  }

  public void setPriority(Integer priority) {
    this.priority = priority;
  }

  public String getDueDate() {
    return dueDate;
  }

  public void setDueDate(String dueDate) {
    this.dueDate = dueDate;
  }

  public String getCompletedAt() {
    return completedAt;
  }

  public void setCompletedAt(String completedAt) {
    this.completedAt = completedAt;
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