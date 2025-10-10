package com.design.todo.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(ApiException.class)
  public ResponseEntity<Result<Object>> handleApiException(ApiException ex) {
    int status = ex.getStatus();
    int code = 500001;
    if (status == 400) code = 400001;
    if (status == 404) code = 404001;
    if (status == 409) code = 409001;

    String message = ex.getMessage();
    if (message == null || message.isBlank()) {
      message = "服务端发生错误";
    }

    if (status >= 500) {
      log.error("服务端错误", ex);
    }

    return ResponseEntity.ok(Result.fail(code, message));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Result<Object>> handleValidationException(MethodArgumentNotValidException ex) {
    return ResponseEntity.ok(Result.fail(400001, resolveFieldErrorMessage(ex.getBindingResult().getFieldErrors())));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<Result<Object>> handleNotReadable(HttpMessageNotReadableException ex) {
    return ResponseEntity.ok(Result.fail(400001, "请求体必须是 JSON 对象"));
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<Result<Object>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
    return ResponseEntity.ok(Result.fail(404001, "接口不存在"));
  }

  @ExceptionHandler(NoHandlerFoundException.class)
  public ResponseEntity<Result<Object>> handleNoHandlerFound(NoHandlerFoundException ex) {
    return ResponseEntity.ok(Result.fail(404001, "接口不存在"));
  }

  @ExceptionHandler(BindException.class)
  public ResponseEntity<Result<Object>> handleBindException(BindException ex) {
    return ResponseEntity.ok(Result.fail(400001, resolveFieldErrorMessage(ex.getBindingResult().getFieldErrors())));
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<Result<Object>> handleConstraintViolationException(ConstraintViolationException ex) {
    String message = "参数校验失败";
    for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
      message = violation.getMessage();
      break;
    }
    return ResponseEntity.ok(Result.fail(400001, message));
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<Result<Object>> handleMissingRequestParameter(MissingServletRequestParameterException ex) {
    return ResponseEntity.ok(Result.fail(400001, ex.getParameterName() + " 必填"));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Result<Object>> handleOtherException(Exception ex) {
    String message = ex.getMessage();
    if (message == null || message.isBlank()) {
      message = "服务端发生错误";
    }

    log.error("服务端错误", ex);
    return ResponseEntity.ok(Result.fail(500001, message));
  }

  private String resolveFieldErrorMessage(java.util.List<FieldError> fieldErrors) {
    if (fieldErrors == null || fieldErrors.isEmpty()) {
      return "参数校验失败";
    }

    FieldError fieldError = fieldErrors.get(0);
    String message = fieldError.getDefaultMessage();
    log.warn("参数校验失败: field={}, message={}", fieldError.getField(), message);
    return message;
  }
}
