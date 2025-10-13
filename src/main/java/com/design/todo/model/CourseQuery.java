package com.design.todo.model;

/**
 * 课程列表查询参数
 */
public class CourseQuery {
  private Integer limit;
  private Integer offset;
  private Integer status;
  private String q;
  private String sortBy;  // 排序字段：price（价格升序）、time（时间降序）

  public CourseQuery() {}

  public CourseQuery(Integer limit, Integer offset, Integer status, String q) {
    this.limit = limit;
    this.offset = offset;
    this.status = status;
    this.q = q;
  }

  public CourseQuery(Integer limit, Integer offset, Integer status, String q, String sortBy) {
    this.limit = limit;
    this.offset = offset;
    this.status = status;
    this.q = q;
    this.sortBy = sortBy;
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

  public Integer getStatus() {
    return status;
  }

  public void setStatus(Integer status) {
    this.status = status;
  }

  public String getQ() {
    return q;
  }

  public void setQ(String q) {
    this.q = q;
  }

  public String getSortBy() {
    return sortBy;
  }

  public void setSortBy(String sortBy) {
    this.sortBy = sortBy;
  }

  /**
   * 判断是否为空查询（所有参数都未设置）
   */
  public boolean isEmpty() {
    return limit == null && offset == null && status == null && q == null && sortBy == null;
  }
}
