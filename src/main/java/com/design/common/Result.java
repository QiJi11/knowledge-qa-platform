package com.design.common;

/**
 * 统一返回结构
 * @param <T> 数据类型
 */
public class Result<T> {
  private int code;
  private String message;
  private T data;
  private long timestamp;

  public Result() {}

  public Result(int code, String message, T data, long timestamp) {
    this.code = code;
    this.message = message;
    this.data = data;
    this.timestamp = timestamp;
  }

  public static <T> Result<T> ok(T data) {
    return ok(data, "OK");
  }

  public static <T> Result<T> ok(T data, String message) {
    return new Result<>(0, message, data, System.currentTimeMillis());
  }

  public static <T> Result<T> fail(int code, String message) {
    return new Result<>(code, message, null, System.currentTimeMillis());
  }

  public int getCode() {
    return code;
  }

  public void setCode(int code) {
    this.code = code;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public T getData() {
    return data;
  }

  public void setData(T data) {
    this.data = data;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }
}
