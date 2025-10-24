package com.design.chat.service;

import com.design.chat.model.ChatMessage;
import com.design.chat.repository.ChatLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class SessionService {
  private static final Logger log = LoggerFactory.getLogger(SessionService.class);
  private static final Duration SESSION_TTL = Duration.ofMinutes(30);
  private static final int MAX_MESSAGES = 16;

  private final StringRedisTemplate redisTemplate;
  private final ObjectMapper objectMapper;
  private final ChatLogRepository chatLogRepository;
  private final AtomicBoolean redisDisabled = new AtomicBoolean(false);

  public SessionService(
    StringRedisTemplate redisTemplate,
    ObjectMapper objectMapper,
    ChatLogRepository chatLogRepository
  ) {
    this.redisTemplate = redisTemplate;
    this.objectMapper = objectMapper;
    this.chatLogRepository = chatLogRepository;
  }

  public List<ChatMessage> getRecentMessages(String sessionId) {
    if (redisDisabled.get()) {
      return Collections.emptyList();
    }

    String key = buildSessionKey(sessionId);
    try {
      List<String> values = redisTemplate.opsForList().range(key, 0, -1);
      redisTemplate.expire(key, SESSION_TTL);
      if (values == null || values.isEmpty()) {
        return Collections.emptyList();
      }

      List<ChatMessage> messages = new ArrayList<>();
      for (String value : values) {
        if (value == null || value.isBlank()) {
          continue;
        }
        messages.add(objectMapper.readValue(value, ChatMessage.class));
      }
      return messages;
    } catch (Exception e) {
      disableRedis(e);
      return Collections.emptyList();
    }
  }

  public void appendConversation(String sessionId, String userMessage, String assistantMessage) {
    List<ChatMessage> messages =
      Arrays.asList(ChatMessage.user(userMessage), ChatMessage.assistant(assistantMessage));

    saveChatLog(sessionId, messages);

    if (redisDisabled.get()) {
      return;
    }

    String key = buildSessionKey(sessionId);
    try {
      String userPayload = objectMapper.writeValueAsString(messages.get(0));
      String assistantPayload = objectMapper.writeValueAsString(messages.get(1));

      redisTemplate.opsForList().rightPushAll(key, userPayload, assistantPayload);
      redisTemplate.opsForList().trim(key, -MAX_MESSAGES, -1);
      redisTemplate.expire(key, SESSION_TTL);
    } catch (Exception e) {
      disableRedis(e);
    }
  }

  private void saveChatLog(String sessionId, List<ChatMessage> messages) {
    try {
      chatLogRepository.saveMessages(sessionId, messages);
    } catch (Exception e) {
      log.warn("写入 chat_log 失败，sessionId={}", sessionId, e);
    }
  }

  private String buildSessionKey(String sessionId) {
    return "chat:session:" + sessionId;
  }

  private void disableRedis(Exception e) {
    if (redisDisabled.compareAndSet(false, true)) {
      log.warn("Redis 会话能力不可用，已降级为仅写 chat_log：{}", String.valueOf(e.getMessage()));
    }
  }
}
