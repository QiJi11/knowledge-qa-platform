package com.design.chat.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HexFormat;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class ChatCacheService {
  private static final Logger log = LoggerFactory.getLogger(ChatCacheService.class);
  private static final Duration CACHE_TTL = Duration.ofHours(1);

  private final StringRedisTemplate redisTemplate;
  private final AtomicBoolean redisDisabled = new AtomicBoolean(false);

  public ChatCacheService(StringRedisTemplate redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  public String getCachedAnswer(String question) {
    if (redisDisabled.get()) {
      return null;
    }

    try {
      return redisTemplate.opsForValue().get(buildCacheKey(question));
    } catch (Exception e) {
      disableRedis(e);
      return null;
    }
  }

  public void cacheAnswer(String question, String answer) {
    if (redisDisabled.get()) {
      return;
    }
    if (answer == null || answer.isBlank()) {
      return;
    }

    try {
      redisTemplate.opsForValue().set(buildCacheKey(question), answer, CACHE_TTL);
    } catch (Exception e) {
      disableRedis(e);
    }
  }

  private String buildCacheKey(String question) {
    return "chat:cache:" + md5(question == null ? "" : question.trim());
  }

  private String md5(String input) {
    try {
      MessageDigest digest = MessageDigest.getInstance("MD5");
      byte[] bytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(bytes);
    } catch (Exception e) {
      throw new RuntimeException("生成问题摘要失败", e);
    }
  }

  private void disableRedis(Exception e) {
    if (redisDisabled.compareAndSet(false, true)) {
      log.warn("Redis 问答缓存不可用，已降级为直连模型：{}", String.valueOf(e.getMessage()));
    }
  }
}
