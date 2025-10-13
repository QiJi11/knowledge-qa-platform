package com.design.todo.model;

/**
 * 课程统计数据
 */
public class CourseStats {
  private int total;
  private int draft;
  private int published;
  private int offline;

  public CourseStats() {}

  public CourseStats(int total, int draft, int published, int offline) {
    this.total = total;
    this.draft = draft;
    this.published = published;
    this.offline = offline;
  }

  public int getTotal() {
    return total;
  }

  public void setTotal(int total) {
    this.total = total;
  }

  public int getDraft() {
    return draft;
  }

  public void setDraft(int draft) {
    this.draft = draft;
  }

  public int getPublished() {
    return published;
  }

  public void setPublished(int published) {
    this.published = published;
  }

  public int getOffline() {
    return offline;
  }

  public void setOffline(int offline) {
    this.offline = offline;
  }
}
