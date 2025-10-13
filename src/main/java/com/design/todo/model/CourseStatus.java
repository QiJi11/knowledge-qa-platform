package com.design.todo.model;

public enum CourseStatus {
  DRAFT(0),
  PUBLISHED(1),
  ARCHIVED(2);

  private final int code;

  CourseStatus(int code) {
    this.code = code;
  }

  public int getCode() {
    return code;
  }

  public static CourseStatus fromCode(Integer code) {
    if (code == null) return null;
    for (CourseStatus status : values()) {
      if (status.code == code) return status;
    }
    return null;
  }
}
