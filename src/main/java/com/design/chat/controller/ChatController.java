package com.design.chat.controller;

import com.design.chat.dto.ChatAskRequest;
import com.design.chat.dto.ChatResponse;
import com.design.chat.service.ChatService;
import com.design.common.Result;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Validated
@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {
  private final ChatService chatService;

  public ChatController(ChatService chatService) {
    this.chatService = chatService;
  }

  @PostMapping("/ask")
  public Result<ChatResponse> ask(@Valid @RequestBody ChatAskRequest request) {
    String message = request.getMessage().trim();
    String sessionId = request.getSessionId().trim();

    String answer = chatService.ask(message, sessionId);
    return Result.ok(new ChatResponse(answer));
  }

  @GetMapping("/stream")
  public SseEmitter stream(
    @RequestParam("message") @NotBlank(message = "message 必须是非空字符串") @Size(max = 4000, message = "message 长度不能超过 4000") String message,
    @RequestParam("sessionId") @NotBlank(message = "sessionId 必须是非空字符串") @Size(max = 64, message = "sessionId 长度不能超过 64") String sessionId
  ) {
    return chatService.askStream(message.trim(), sessionId.trim());
  }
}
