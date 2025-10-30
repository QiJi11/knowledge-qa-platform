package com.design.todo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 集成测试：测试完整的端到端流程（HTTP -> Controller -> Service -> Repository -> MySQL）
 *
 * 运行前提：
 * 1. 需要手动创建测试数据库：CREATE DATABASE IF NOT EXISTS design_test;
 * 2. 设置环境变量 MYSQL_PASSWORD（或在 IDE 运行配置中设置）
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class CourseIntegrationTest {

  @Autowired private TestRestTemplate restTemplate;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private JdbcTemplate jdbcTemplate;

  @BeforeEach
  void setUp() {
    // 由于存在外键约束，必须先删任务再删课程
    jdbcTemplate.update("DELETE FROM learning_tasks");
    jdbcTemplate.update("DELETE FROM courses");
  }

  @AfterEach
  void tearDown() {
    jdbcTemplate.update("DELETE FROM learning_tasks");
    jdbcTemplate.update("DELETE FROM courses");
  }

  @Test
  @DisplayName("健康检查：GET /health 应返回 code=0")
  void healthCheck_shouldReturnOk() throws Exception {
    ResponseEntity<String> response = restTemplate.getForEntity("/health", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    JsonNode json = objectMapper.readTree(response.getBody());
    assertThat(json.get("code").asInt()).isEqualTo(0);
    assertThat(json.get("message").asText()).isEqualTo("OK");
    assertThat(json.get("data").get("ok").asBoolean()).isTrue();
  }

  @Test
  @DisplayName("课程 CRUD：创建 -> 列表 -> 详情 -> 更新 -> 删除")
  void courseCrud_shouldWork() throws Exception {
    // 1) 初始列表为空
    ResponseEntity<String> listResp1 = restTemplate.getForEntity("/api/v1/courses", String.class);
    JsonNode listJson1 = objectMapper.readTree(listResp1.getBody());
    assertThat(listJson1.get("code").asInt()).isEqualTo(0);
    assertThat(listJson1.get("data").get("items").size()).isEqualTo(0);
    assertThat(listJson1.get("data").get("total").asInt()).isEqualTo(0);

    // 2) 创建课程
    Map<String, Object> createReq = new HashMap<>();
    createReq.put("title", " Spring Boot 实战 ");
    createReq.put("category", "后端开发");
    createReq.put("level", 2);
    createReq.put("coverUrl", "https://example.com/cover.jpg");
    createReq.put("summary", "从零构建企业级应用");
    createReq.put("description", "详细讲解 Spring Boot 核心特性");
    createReq.put("totalLessons", 30);
    createReq.put("totalMinutes", 1800);
    createReq.put("status", 1);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<Map<String, Object>> createEntity = new HttpEntity<>(createReq, headers);

    ResponseEntity<String> createResp = restTemplate.postForEntity("/api/v1/courses", createEntity, String.class);
    JsonNode createJson = objectMapper.readTree(createResp.getBody());
    assertThat(createJson.get("code").asInt()).isEqualTo(0);
    assertThat(createJson.get("message").asText()).isEqualTo("OK");

    long courseId = createJson.get("data").get("id").asLong();
    assertThat(courseId).isGreaterThan(0);
    assertThat(createJson.get("data").get("title").asText()).isEqualTo("Spring Boot 实战");
    assertThat(createJson.get("data").get("category").asText()).isEqualTo("后端开发");
    assertThat(createJson.get("data").get("level").asInt()).isEqualTo(2);
    assertThat(createJson.get("data").get("status").asInt()).isEqualTo(1);

    // 3) 课程详情
    ResponseEntity<String> detailResp = restTemplate.getForEntity("/api/v1/courses/" + courseId, String.class);
    JsonNode detailJson = objectMapper.readTree(detailResp.getBody());
    assertThat(detailJson.get("code").asInt()).isEqualTo(0);
    assertThat(detailJson.get("data").get("id").asLong()).isEqualTo(courseId);

    // 4) 列表应该有 1 条
    ResponseEntity<String> listResp2 = restTemplate.getForEntity("/api/v1/courses?page=1&pageSize=20", String.class);
    JsonNode listJson2 = objectMapper.readTree(listResp2.getBody());
    assertThat(listJson2.get("code").asInt()).isEqualTo(0);
    assertThat(listJson2.get("data").get("total").asInt()).isEqualTo(1);
    assertThat(listJson2.get("data").get("items").size()).isEqualTo(1);

    // 5) 更新课程
    Map<String, Object> updateReq = new HashMap<>();
    updateReq.put("title", "Spring Boot 实战（更新）");
    updateReq.put("category", "后端开发");
    updateReq.put("level", 2);
    updateReq.put("summary", "更新后的简介");
    updateReq.put("totalLessons", 31);
    updateReq.put("totalMinutes", 1860);
    updateReq.put("status", 1);

    HttpEntity<Map<String, Object>> updateEntity = new HttpEntity<>(updateReq, headers);
    ResponseEntity<String> updateResp =
      restTemplate.exchange("/api/v1/courses/" + courseId, HttpMethod.PUT, updateEntity, String.class);
    JsonNode updateJson = objectMapper.readTree(updateResp.getBody());
    assertThat(updateJson.get("code").asInt()).isEqualTo(0);
    assertThat(updateJson.get("data").get("title").asText()).isEqualTo("Spring Boot 实战（更新）");
    assertThat(updateJson.get("data").get("totalLessons").asInt()).isEqualTo(31);

    // 6) 删除课程
    HttpEntity<Void> deleteEntity = new HttpEntity<>(null, headers);
    ResponseEntity<String> deleteResp =
      restTemplate.exchange("/api/v1/courses/" + courseId, HttpMethod.DELETE, deleteEntity, String.class);
    JsonNode deleteJson = objectMapper.readTree(deleteResp.getBody());
    assertThat(deleteJson.get("code").asInt()).isEqualTo(0);
    assertThat(deleteJson.get("data").get("deleted").asBoolean()).isTrue();

    // 7) 再查详情应 404001
    ResponseEntity<String> detailResp2 = restTemplate.getForEntity("/api/v1/courses/" + courseId, String.class);
    JsonNode detailJson2 = objectMapper.readTree(detailResp2.getBody());
    assertThat(detailJson2.get("code").asInt()).isEqualTo(404001);
  }

  @Test
  @DisplayName("删除课程：存在学习任务时应返回 409001")
  void deleteCourse_withTasks_shouldFail() throws Exception {
    // 1) 先创建课程
    Map<String, Object> createReq = new HashMap<>();
    createReq.put("title", "MySQL 性能优化");
    createReq.put("category", "数据库");
    createReq.put("level", 3);
    createReq.put("status", 1);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<Map<String, Object>> createEntity = new HttpEntity<>(createReq, headers);

    ResponseEntity<String> createResp = restTemplate.postForEntity("/api/v1/courses", createEntity, String.class);
    JsonNode createJson = objectMapper.readTree(createResp.getBody());
    assertThat(createJson.get("code").asInt()).isEqualTo(0);

    long courseId = createJson.get("data").get("id").asLong();
    assertThat(courseId).isGreaterThan(0);

    // 2) 插入一条关联学习任务
    jdbcTemplate.update(
      "INSERT INTO learning_tasks (learner_id, course_id, title, status, priority) VALUES (1, ?, '任务1', 0, 2)",
      courseId
    );

    // 3) 删除应失败（409001）
    HttpEntity<Void> deleteEntity = new HttpEntity<>(null, headers);
    ResponseEntity<String> deleteResp =
      restTemplate.exchange("/api/v1/courses/" + courseId, HttpMethod.DELETE, deleteEntity, String.class);
    JsonNode deleteJson = objectMapper.readTree(deleteResp.getBody());

    assertThat(deleteJson.get("code").asInt()).isEqualTo(409001);
    assertThat(deleteJson.get("message").asText()).isEqualTo("该课程下还有学习任务，无法删除");
  }
}