package com.design.order.entity;

import java.math.BigDecimal;

public class Order {
  private Long id;
  private String orderNo;
  private Long userId;
  private Long courseId;
  private BigDecimal amount;
  private String payType;
  private Integer status; // 0=待支付 1=已支付 2=已取消
  private String paidAt;
  private String createdAt;
  private String updatedAt;

  // 关联信息（非数据库字段）
  private String courseTitle;
  private String courseCoverUrl;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public String getOrderNo() { return orderNo; }
  public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
  public Long getUserId() { return userId; }
  public void setUserId(Long userId) { this.userId = userId; }
  public Long getCourseId() { return courseId; }
  public void setCourseId(Long courseId) { this.courseId = courseId; }
  public BigDecimal getAmount() { return amount; }
  public void setAmount(BigDecimal amount) { this.amount = amount; }
  public String getPayType() { return payType; }
  public void setPayType(String payType) { this.payType = payType; }
  public Integer getStatus() { return status; }
  public void setStatus(Integer status) { this.status = status; }
  public String getPaidAt() { return paidAt; }
  public void setPaidAt(String paidAt) { this.paidAt = paidAt; }
  public String getCreatedAt() { return createdAt; }
  public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
  public String getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
  public String getCourseTitle() { return courseTitle; }
  public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }
  public String getCourseCoverUrl() { return courseCoverUrl; }
  public void setCourseCoverUrl(String courseCoverUrl) { this.courseCoverUrl = courseCoverUrl; }
}
