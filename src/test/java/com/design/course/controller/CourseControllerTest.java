package com.design.course.controller;

import com.design.course.entity.Course;
import com.design.course.service.CourseService;
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

import java.sql.Timestamp;
import java.time.Instant;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CourseController.class)
@ContextConfiguration(classes = DesignTodoBackendApplication.class)
@TestPropertySource(
  properties = {
    "spring.mvc.throw-exception-if-no-handler-found=true",
    "spring.web.resources.add-mappings=false"
  }
)
class CourseControllerTest {
  @Autowired private MockMvc mvc;

  @MockBean private CourseService service;

  @Test
  @DisplayName("POST /api/v1/courses：status 为 null 时应返回 400001")
  void createCourse_statusNull_shouldFail() throws Exception {
    mvc
      .perform(
        post("/api/v1/courses")
          .contentType(MediaType.APPLICATION_JSON)
          .content("{\"title\":\"测试课程\",\"category\":\"后端开发\",\"level\":2,\"status\":null}")
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(400001))
      .andExpect(jsonPath("$.message").value("status 必须是整数"))
      .andExpect(jsonPath("$.data").value(nullValue()))
      .andExpect(jsonPath("$.timestamp").isNumber());

    verifyNoInteractions(service);
  }

  @Test
  @DisplayName("GET /api/v1/courses/{id}：不存在返回 404001")
  void getCourse_notFound_shouldFail() throws Exception {
    when(service.getCourse(1L)).thenReturn(null);

    mvc
      .perform(get("/api/v1/courses/1"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(404001))
      .andExpect(jsonPath("$.message").value("未找到该课程"))
      .andExpect(jsonPath("$.data").value(nullValue()))
      .andExpect(jsonPath("$.timestamp").isNumber());

    verify(service).getCourse(1L);
  }

  @Test
  @DisplayName("POST /api/v1/courses：正常创建返回 0 且结构正确")
  void createCourse_ok_shouldReturnCourse() throws Exception {
    Timestamp publishedAt = Timestamp.from(Instant.parse("2026-01-01T00:00:00Z"));

    Course course =
      new Course(
        1001L,
        "Spring Boot 实战",
        "后端开发",
        2,
        "https://example.com/cover.jpg",
        "从入门到精通",
        "详细讲解 Spring Boot 核心特性",
        30,
        1800,
        1,
        "2026-01-01T00:00:00.000Z",
        "2026-01-01T00:00:00.000Z",
        "2026-01-01T00:00:00.000Z"
      );

    when(
      service.createCourse(
        "Spring Boot 实战",
        "后端开发",
        2,
        "https://example.com/cover.jpg",
        "从入门到精通",
        "详细讲解 Spring Boot 核心特性",
        30,
        1800,
        1,
        publishedAt
      )
    )
      .thenReturn(course);

    mvc
      .perform(
        post("/api/v1/courses")
          .contentType(MediaType.APPLICATION_JSON)
          .content(
            "{\"title\":\" Spring Boot 实战 \",\"category\":\"后端开发\",\"level\":2,\"coverUrl\":\"https://example.com/cover.jpg\",\"summary\":\"从入门到精通\",\"description\":\"详细讲解 Spring Boot 核心特性\",\"totalLessons\":30,\"totalMinutes\":1800,\"status\":1,\"publishedAt\":\"2026-01-01T00:00:00Z\"}"
          )
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.message").value("OK"))
      .andExpect(jsonPath("$.data.id").value(1001))
      .andExpect(jsonPath("$.data.title").value("Spring Boot 实战"))
      .andExpect(jsonPath("$.data.category").value("后端开发"))
      .andExpect(jsonPath("$.data.level").value(2))
      .andExpect(jsonPath("$.data.status").value(1))
      .andExpect(jsonPath("$.timestamp").isNumber());

    verify(
      service
    )
      .createCourse(
        "Spring Boot 实战",
        "后端开发",
        2,
        "https://example.com/cover.jpg",
        "从入门到精通",
        "详细讲解 Spring Boot 核心特性",
        30,
        1800,
        1,
        publishedAt
      );
  }

  @Test
  @DisplayName("POST /api/v1/courses：坏 JSON 应返回 400001")
  void createCourse_invalidJson_shouldFail() throws Exception {
    mvc
      .perform(post("/api/v1/courses").contentType(MediaType.APPLICATION_JSON).content("{not-json}"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(400001))
      .andExpect(jsonPath("$.message").value("请求体必须是 JSON 对象"))
      .andExpect(jsonPath("$.data").value(nullValue()))
      .andExpect(jsonPath("$.timestamp").isNumber());

    verifyNoInteractions(service);
  }

  @Test
  @DisplayName("不存在的接口：应返回 HTTP 200 + 404001 + 接口不存在")
  void notFound_shouldReturnOkWithApiResponse() throws Exception {
    mvc
      .perform(get("/__not_found__"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(404001))
      .andExpect(jsonPath("$.message").value("接口不存在"))
      .andExpect(jsonPath("$.data").value(nullValue()))
      .andExpect(jsonPath("$.timestamp").isNumber());

    verifyNoInteractions(service);
  }
}