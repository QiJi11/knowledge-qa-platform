package com.design.chat.controller;

import com.design.chat.service.ChatService;
import com.design.KnowledgeQaPlatformApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatController.class)
@ContextConfiguration(classes = KnowledgeQaPlatformApplication.class)
@TestPropertySource(
  properties = {
    "spring.mvc.throw-exception-if-no-handler-found=true",
    "spring.web.resources.add-mappings=false"
  }
)
class ChatControllerTest {
  @Autowired private MockMvc mvc;

  @MockBean private ChatService chatService;

  @Test
  @DisplayName("POST /api/v1/chat/ask：message 为空时应返回 400001")
  void ask_messageBlank_shouldFail() throws Exception {
    mvc
      .perform(
        post("/api/v1/chat/ask")
          .contentType(MediaType.APPLICATION_JSON)
          .content("{\"message\":\" \",\"sessionId\":\"session-1\"}")
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(400001))
      .andExpect(jsonPath("$.message").value("message 必须是非空字符串"))
      .andExpect(jsonPath("$.data").value(nullValue()))
      .andExpect(jsonPath("$.timestamp").isNumber());

    verifyNoInteractions(chatService);
  }

  @Test
  @DisplayName("POST /api/v1/chat/ask：正常返回 AI 回答")
  void ask_ok_shouldReturnAnswer() throws Exception {
    when(chatService.ask("如何重置密码？", "session-1")).thenReturn("进入个人中心后点击重置密码。");

    mvc
      .perform(
        post("/api/v1/chat/ask")
          .contentType(MediaType.APPLICATION_JSON)
          .content("{\"message\":\"如何重置密码？\",\"sessionId\":\"session-1\"}")
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.message").value("OK"))
      .andExpect(jsonPath("$.data.answer").value("进入个人中心后点击重置密码。"))
      .andExpect(jsonPath("$.timestamp").isNumber());

    verify(chatService).ask("如何重置密码？", "session-1");
  }

  @Test
  @DisplayName("GET /api/v1/chat/stream：sessionId 为空时应返回 400001")
  void stream_sessionIdBlank_shouldFail() throws Exception {
    mvc
      .perform(get("/api/v1/chat/stream").param("message", "你好").param("sessionId", " "))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(400001))
      .andExpect(jsonPath("$.message").value("sessionId 必须是非空字符串"))
      .andExpect(jsonPath("$.data").value(nullValue()))
      .andExpect(jsonPath("$.timestamp").isNumber());

    verifyNoInteractions(chatService);
  }
}
