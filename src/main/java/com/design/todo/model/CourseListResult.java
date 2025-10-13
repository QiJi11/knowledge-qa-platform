package com.design.todo.model;

import java.util.List;

/**
 * 课程列表查询结果（包含分页信息）
 */
public class CourseListResult {
  private List<Course> items;
  private int total;
  private Integer limit;
  private Integer offset;

  public CourseListResult() {}

  public CourseListResult(List<Course> items, int total, Integer limit, Integer offset) {
    this.items = items;
    this.total = total;
    this.limit = limit;
    this.offset = offset;
  }

  public List<Course> getItems() {
    return items;
  }

  public void setItems(List<Course> items) {
    this.items = items;
  }

  public int getTotal() {
    return total;
  }

  public void setTotal(int total) {
    this.total = total;
  }

  public Integer getLimit() {
    return limit;
  }

  public void setLimit(Integer limit) {
    this.limit = limit;
  }

  public Integer getOffset() {
    return offset;
  }

  public void setOffset(Integer offset) {
    this.offset = offset;
  }
}
