package com.design.todo.api;

@Deprecated
public class ApiResponse<T> extends Result<T> {
  public ApiResponse() {}

  public ApiResponse(int code, String message, T data, long timestamp) {
    super(code, message, data, timestamp);
  }

  public static <T> ApiResponse<T> ok(T data) {
    return ok(data, "OK");
  }

  public static <T> ApiResponse<T> ok(T data, String message) {
    return new ApiResponse<>(0, message, data, System.currentTimeMillis());
  }

  public static <T> ApiResponse<T> fail(int code, String message) {
    return new ApiResponse<>(code, message, null, System.currentTimeMillis());
  }

}
