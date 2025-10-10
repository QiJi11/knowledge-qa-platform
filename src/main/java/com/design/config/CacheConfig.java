package com.design.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis 缓存配置：
 * - 使用 JSON 序列化，避免 JDK 序列化要求 DTO 实现 Serializable
 * - Redis 不可用时吞掉缓存异常，保证主流程可用（降级）
 */
@Configuration
public class CacheConfig extends CachingConfigurerSupport {
  private static final Logger log = LoggerFactory.getLogger(CacheConfig.class);

  @Bean
  public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
    RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
      .serializeValuesWith(
        RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer())
      )
      .disableCachingNullValues()
      // 默认 TTL（秒）：演示/验收用，避免数据长期不一致
      .entryTtl(Duration.ofSeconds(60));

    Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
    cacheConfigs.put("stats:overview", defaultConfig.entryTtl(Duration.ofSeconds(30)));
    cacheConfigs.put("stats:by-course", defaultConfig.entryTtl(Duration.ofSeconds(60)));
    cacheConfigs.put("stats:trend", defaultConfig.entryTtl(Duration.ofSeconds(60)));

    return RedisCacheManager.builder(connectionFactory)
      .cacheDefaults(defaultConfig)
      .withInitialCacheConfigurations(cacheConfigs)
      .build();
  }

  /**
   * 降级策略：Redis 停止/连接失败时，缓存读写异常不影响接口正常返回。
   */
  @Bean
  @Override
  public CacheErrorHandler errorHandler() {
    return new CacheErrorHandler() {
      @Override
      public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
        log.debug("Cache GET error (ignored). cache={}, key={}, err={}", cache.getName(), key, exception.getMessage());
      }

      @Override
      public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
        log.debug("Cache PUT error (ignored). cache={}, key={}, err={}", cache.getName(), key, exception.getMessage());
      }

      @Override
      public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
        log.debug("Cache EVICT error (ignored). cache={}, key={}, err={}", cache.getName(), key, exception.getMessage());
      }

      @Override
      public void handleCacheClearError(RuntimeException exception, Cache cache) {
        log.debug("Cache CLEAR error (ignored). cache={}, err={}", cache.getName(), exception.getMessage());
      }
    };
  }
}
