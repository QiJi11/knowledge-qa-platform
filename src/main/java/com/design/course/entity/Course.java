package com.design.course.entity;

public class Course {
  private Long id;
  private String title;
  private String category;
  private Integer level;
  private String coverUrl;
  private String summary;
  private String description;
  private Integer totalLessons;
  private Integer totalMinutes;
  private Integer status;
  private String publishedAt;
  private String createdAt;
  private String updatedAt;

  public Course() {}

  public Course(
    Long id,
    String title,
    String category,
    Integer level,
    String coverUrl,
    String summary,
    String description,
    Integer totalLessons,
    Integer totalMinutes,
    Integer status,
    String publishedAt,
    String createdAt,
    String updatedAt
  ) {
    this.id = id;
    this.title = title;
    this.category = category;
    this.level = level;
    this.coverUrl = coverUrl;
    this.summary = summary;
    this.description = description;
    this.totalLessons = totalLessons;
    this.totalMinutes = totalMinutes;
    this.status = status;
    this.publishedAt = publishedAt;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public Integer getLevel() {
    return level;
  }

  public void setLevel(Integer level) {
    this.level = level;
  }

  public String getCoverUrl() {
    return coverUrl;
  }

  public void setCoverUrl(String coverUrl) {
    this.coverUrl = coverUrl;
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

  public Integer getTotalLessons() {
    return totalLessons;
  }

  public void setTotalLessons(Integer totalLessons) {
    this.totalLessons = totalLessons;
  }

  public Integer getTotalMinutes() {
    return totalMinutes;
  }

  public void setTotalMinutes(Integer totalMinutes) {
    this.totalMinutes = totalMinutes;
  }

  public Integer getStatus() {
    return status;
  }

  public void setStatus(Integer status) {
    this.status = status;
  }

  public String getPublishedAt() {
    return publishedAt;
  }

  public void setPublishedAt(String publishedAt) {
    this.publishedAt = publishedAt;
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