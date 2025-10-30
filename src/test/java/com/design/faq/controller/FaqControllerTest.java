package com.design.faq.controller;

import com.design.faq.entity.Faq;
import com.design.faq.service.FaqService;
import com.design.todo.DesignTodoBackendApplication;
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

@WebMvcTest(FaqController.class)
@ContextConfiguration(classes = DesignTodoBackendApplication.class)
@TestPropertySource(
  properties = {
    "spring.mvc.throw-exception-if-no-handler-found=true",
    "spring.web.resources.add-mappings=false"
  }
)
class FaqControllerTest {
  @Autowired private MockMvc mvc;

  @MockBean private FaqService service;

  @Test
  @DisplayName("POST /api/v1/faq：question 为空时应返回 400001")
  void createFaq_questionBlank_shouldFail() throws Exception {
    mvc
      .perform(
        post("/api/v1/faq")
          .contentType(MediaType.APPLICATION_JSON)
          .content("{\"question\":\" \",\"answer\":\"测试答案\"}")
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(400001))
      .andExpect(jsonPath("$.message").value("question 必须是非空字符串"))
      .andExpect(jsonPath("$.data").value(nullValue()))
      .andExpect(jsonPath("$.timestamp").isNumber());

    verifyNoInteractions(service);
  }

  @Test
  @DisplayName("GET /api/v1/faq/{id}：不存在返回 404001")
  void getFaq_notFound_shouldFail() throws Exception {
    when(service.getFaq(1L)).thenReturn(null);

    mvc
      .perform(get("/api/v1/faq/1"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(404001))
      .andExpect(jsonPath("$.message").value("未找到该 FAQ"))
      .andExpect(jsonPath("$.data").value(nullValue()))
      .andExpect(jsonPath("$.timestamp").isNumber());

    verify(service).getFaq(1L);
  }

  @Test
  @DisplayName("POST /api/v1/faq：正常创建返回 0 且结构正确")
  void createFaq_ok_shouldReturnFaq() throws Exception {
    Faq faq =
      new Faq(
        1L,
        "如何重置密码？",
        "进入个人中心后点击重置密码。",
        "密码,重置,账号",
        "账号",
        3,
        "2026-03-12T00:00:00.000Z",
        "2026-03-12T00:00:00.000Z"
      );

    when(
      service.createFaq(
        "如何重置密码？",
        "进入个人中心后点击重置密码。",
        "密码,重置,账号",
        "账号",
        3
      )
    )
      .thenReturn(faq);

    mvc
      .perform(
        post("/api/v1/faq")
          .contentType(MediaType.APPLICATION_JSON)
          .content(
            "{\"question\":\" 如何重置密码？ \",\"answer\":\"进入个人中心后点击重置密码。\",\"keywords\":\"密码,重置,账号\",\"category\":\"账号\",\"hitCount\":3}"
          )
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.message").value("OK"))
      .andExpect(jsonPath("$.data.id").value(1))
      .andExpect(jsonPath("$.data.question").value("如何重置密码？"))
      .andExpect(jsonPath("$.data.category").value("账号"))
      .andExpect(jsonPath("$.data.hitCount").value(3))
      .andExpect(jsonPath("$.timestamp").isNumber());

    verify(service).createFaq("如何重置密码？", "进入个人中心后点击重置密码。", "密码,重置,账号", "账号", 3);
  }
}
