package com.design.todo.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.PreDestroy;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class RedisCacheClient implements CacheClient {
  private static final Logger log = LoggerFactory.getLogger(RedisCacheClient.class);

  private final LettuceConnectionFactory factory;
  private final StringRedisTemplate redis;
  private final AtomicBoolean disabled = new AtomicBoolean(false);

  public RedisCacheClient(LettuceConnectionFactory factory) {
    this.factory = factory;
    this.redis = new StringRedisTemplate(factory);
    this.redis.afterPropertiesSet();
  }

  private void disable(Exception e) {
    if (disabled.compareAndSet(false, true)) {
      log.warn("Redis 连接失败（已自动降级为不使用缓存）：{}", String.valueOf(e));
    }
  }

  @Override
  public String get(String key) {
    if (disabled.get()) return null;
    try {
      return redis.opsForValue().get(key);
    } catch (Exception e) {
      disable(e);
      return null;
    }
  }

  @Override
  public void set(String key, String value, int ttlSeconds) {
    if (disabled.get()) return;
    try {
      if (ttlSeconds > 0) {
        redis.opsForValue().set(key, value, Duration.ofSeconds(ttlSeconds));
        return;
      }
      redis.opsForValue().set(key, value);
    } catch (Exception e) {
      disable(e);
    }
  }

  @Override
  public void del(List<String> keys) {
    if (disabled.get()) return;
    if (keys == null || keys.isEmpty()) return;
    try {
      redis.delete(keys);
    } catch (Exception e) {
      disable(e);
    }
  }

  @PreDestroy
  public void close() {
    try {
      factory.destroy();
    } catch (Exception ignored) {
      // ignore
    }
  }
}
