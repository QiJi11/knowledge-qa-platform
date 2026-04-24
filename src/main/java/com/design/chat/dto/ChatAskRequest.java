package com.design.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ChatAskRequest {
  @NotBlank(message = "message 必须是非空字符串")
  @Size(max = 4000, message = "message 长度不能超过 4000")
  private String message;

  @NotBlank(message = "sessionId 必须是非空字符串")
  @Size(max = 64, message = "sessionId 长度不能超过 64")
  private String sessionId;

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getSessionId() {
    return sessionId;
  }

  public void setSessionId(String sessionId) {
    this.sessionId = sessionId;
  }
}
