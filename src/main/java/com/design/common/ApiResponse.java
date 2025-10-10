package com.design.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;

/**
 * 统一返回格式
 * @param <T> 数据类型
 */
public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant timestamp;

    public ApiResponse() {
        this.timestamp = Instant.now();
    }

    public ApiResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = Instant.now();
    }

    /**
     * 成功响应
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(0, "OK", data);
    }

    /**
     * 成功响应（无数据）
     */
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(0, "OK", null);
    }

    /**
     * 错误响应
     */
    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }

    /**
     * 参数错误
     */
    public static <T> ApiResponse<T> badRequest(String message) {
        return new ApiResponse<>(400001, message, null);
    }

    /**
     * 资源不存在
     */
    public static <T> ApiResponse<T> notFound(String message) {
        return new ApiResponse<>(404001, message, null);
    }

    /**
     * 业务冲突
     */
    public static <T> ApiResponse<T> conflict(String message) {
        return new ApiResponse<>(409001, message, null);
    }

    /**
     * 服务器错误
     */
    public static <T> ApiResponse<T> serverError(String message) {
        return new ApiResponse<>(500001, message, null);
    }

    // Getters and Setters
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

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
