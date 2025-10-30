package com.design.todo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.impl.client.HttpClientBuilder;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.test.context.ActiveProfiles;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class LearningTaskIntegrationTest {

  @Autowired private TestRestTemplate restTemplate;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private JdbcTemplate jdbcTemplate;

  @BeforeEach
  void setUp() {
    // TestRestTemplate 默认不支持 PATCH，这里换成 Apache HttpClient
    restTemplate.getRestTemplate().setRequestFactory(
      new HttpComponentsClientHttpRequestFactory(HttpClientBuilder.create().build())
    );

    jdbcTemplate.update("DELETE FROM learning_tasks");
    jdbcTemplate.update("DELETE FROM courses");
  }

  @AfterEach
  void tearDown() {
    jdbcTemplate.update("DELETE FROM learning_tasks");
    jdbcTemplate.update("DELETE FROM courses");
  }

  private long insertCourseAndReturnId() {
    KeyHolder keyHolder = new GeneratedKeyHolder();
    jdbcTemplate.update(
      connection -> {
        PreparedStatement ps =
          connection.prepareStatement(
            "INSERT INTO courses (title, category, level) VALUES (?, ?, ?)",
            Statement.RETURN_GENERATED_KEYS
          );
        ps.setString(1, "测试课程");
        ps.setString(2, "后端开发");
        ps.setInt(3, 1);
        return ps;
      },
      keyHolder
    );

    Number key = keyHolder.getKey();
    assertThat(key).isNotNull();
    return key.longValue();
  }

  @Test
  @DisplayName("状态机：创建 status=2 自动设置 completedAt；2->1 清空；1->2 再设置")
  void completedAtStateMachine_shouldWork() throws Exception {
    long courseId = insertCourseAndReturnId();

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    // 1) 创建时 status=2，应自动设置 completedAt
    Map<String, Object> createReq = new HashMap<>();
    createReq.put("courseId", courseId);
    createReq.put("title", "完成第1章学习");
    createReq.put("status", 2);

    HttpEntity<Map<String, Object>> createEntity = new HttpEntity<>(createReq, headers);
    ResponseEntity<String> createResp =
      restTemplate.postForEntity("/api/v1/learners/1/learning-tasks", createEntity, String.class);

    JsonNode createJson = objectMapper.readTree(createResp.getBody());
    assertThat(createJson.get("code").asInt()).isEqualTo(0);
    assertThat(createJson.get("message").asText()).isEqualTo("OK");

    long taskId = createJson.get("data").get("id").asLong();
    assertThat(taskId).isGreaterThan(0);
    assertThat(createJson.get("data").get("completedAt").isNull()).isFalse();

    // 2) PATCH：2 -> 1，应清空 completedAt
    Map<String, Object> patchToInProgress = new HashMap<>();
    patchToInProgress.put("status", 1);

    HttpEntity<Map<String, Object>> patchEntity1 = new HttpEntity<>(patchToInProgress, headers);
    ResponseEntity<String> patchResp1 =
      restTemplate.exchange(
        "/api/v1/learners/1/learning-tasks/" + taskId,
        HttpMethod.PATCH,
        patchEntity1,
        String.class
      );

    JsonNode patchJson1 = objectMapper.readTree(patchResp1.getBody());
    assertThat(patchJson1.get("code").asInt()).isEqualTo(0);
    assertThat(patchJson1.get("data").get("status").asInt()).isEqualTo(1);
    assertThat(patchJson1.get("data").get("completedAt").isNull()).isTrue();

    // 3) PATCH：1 -> 2，应重新设置 completedAt
    Map<String, Object> patchToCompleted = new HashMap<>();
    patchToCompleted.put("status", 2);

    HttpEntity<Map<String, Object>> patchEntity2 = new HttpEntity<>(patchToCompleted, headers);
    ResponseEntity<String> patchResp2 =
      restTemplate.exchange(
        "/api/v1/learners/1/learning-tasks/" + taskId,
        HttpMethod.PATCH,
        patchEntity2,
        String.class
      );

    JsonNode patchJson2 = objectMapper.readTree(patchResp2.getBody());
    assertThat(patchJson2.get("code").asInt()).isEqualTo(0);
    assertThat(patchJson2.get("data").get("status").asInt()).isEqualTo(2);
    assertThat(patchJson2.get("data").get("completedAt").isNull()).isFalse();
  }

  @Test
  @DisplayName("状态机：0 不能直接变 2（必须先 0->1->2）")
  void statusTransition_0To2_shouldFail() throws Exception {
    long courseId = insertCourseAndReturnId();

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    // 创建默认 status=0
    Map<String, Object> createReq = new HashMap<>();
    createReq.put("courseId", courseId);
    createReq.put("title", "任务A");

    HttpEntity<Map<String, Object>> createEntity = new HttpEntity<>(createReq, headers);
    ResponseEntity<String> createResp =
      restTemplate.postForEntity("/api/v1/learners/1/learning-tasks", createEntity, String.class);

    JsonNode createJson = objectMapper.readTree(createResp.getBody());
    long taskId = createJson.get("data").get("id").asLong();

    // 直接 0 -> 2
    Map<String, Object> patchReq = new HashMap<>();
    patchReq.put("status", 2);

    HttpEntity<Map<String, Object>> patchEntity = new HttpEntity<>(patchReq, headers);
    ResponseEntity<String> patchResp =
      restTemplate.exchange(
        "/api/v1/learners/1/learning-tasks/" + taskId,
        HttpMethod.PATCH,
        patchEntity,
        String.class
      );

    JsonNode patchJson = objectMapper.readTree(patchResp.getBody());
    assertThat(patchJson.get("code").asInt()).isEqualTo(400001);
    assertThat(patchJson.get("message").asText()).contains("状态流转不允许");
  }

  @Test
  @DisplayName("越权保护：用其他 learnerId 查同一个 taskId 应返回 404001")
  void learnerIsolation_shouldReturn404() throws Exception {
    long courseId = insertCourseAndReturnId();

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    Map<String, Object> createReq = new HashMap<>();
    createReq.put("courseId", courseId);
    createReq.put("title", "任务B");

    HttpEntity<Map<String, Object>> createEntity = new HttpEntity<>(createReq, headers);
    ResponseEntity<String> createResp =
      restTemplate.postForEntity("/api/v1/learners/1/learning-tasks", createEntity, String.class);

    JsonNode createJson = objectMapper.readTree(createResp.getBody());
    long taskId = createJson.get("data").get("id").asLong();

    ResponseEntity<String> getOtherLearnerResp =
      restTemplate.getForEntity("/api/v1/learners/2/learning-tasks/" + taskId, String.class);
    JsonNode getOtherLearnerJson = objectMapper.readTree(getOtherLearnerResp.getBody());

    assertThat(getOtherLearnerJson.get("code").asInt()).isEqualTo(404001);
  }
}