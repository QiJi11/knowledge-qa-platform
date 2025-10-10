package com.design.todo.config;

import com.design.todo.cache.CacheClient;
import com.design.todo.cache.NoopCacheClient;
import com.design.todo.cache.RedisCacheClient;
import com.design.todo.repo.CachedCourseRepository;
import com.design.todo.repo.JdbcCourseRepository;
import com.design.todo.repo.CourseRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import java.net.URI;
import java.time.Duration;

@Configuration
public class AppBeansConfig {
  private static final Logger log = LoggerFactory.getLogger(AppBeansConfig.class);

  /**
   * 创建缓存客户端 Bean
   * 实现 Redis 自动降级策略：
   * - 如果 Redis 配置不存在或连接失败，自动降级为 NoopCacheClient
   * - NoopCacheClient 所有操作都是空实现，相当于直连 MySQL
   */
  @Bean
  public CacheClient cacheClient(Environment env) {
    RedisConnectOptions options = readRedisConnectOptions(env);
    if (options == null) return new NoopCacheClient();

    try {
      LettuceClientConfiguration clientConfig =
        LettuceClientConfiguration.builder().commandTimeout(Duration.ofSeconds(1)).build();

      RedisStandaloneConfiguration standalone =
        new RedisStandaloneConfiguration(options.host, options.port);
      if (options.password != null && !options.password.isBlank()) {
        standalone.setPassword(options.password);
      }

      LettuceConnectionFactory factory = new LettuceConnectionFactory(standalone, clientConfig);
      factory.afterPropertiesSet();
      return new RedisCacheClient(factory);
    } catch (Exception e) {
      log.warn("Redis 初始化失败（已自动降级为不使用缓存）：{}", String.valueOf(e));
      return new NoopCacheClient();
    }
  }

  @Bean
  @Primary
  public CourseRepository courseRepository(
    JdbcCourseRepository dbRepo,
    CacheClient cacheClient,
    ObjectMapper objectMapper,
    Environment env
  ) {
    int ttlSeconds = readInt(env.getProperty("REDIS_TTL_SECONDS"), 15);
    return new CachedCourseRepository(dbRepo, cacheClient, objectMapper, ttlSeconds);
  }

  private static int readInt(String raw, int defaultValue) {
    if (raw == null || raw.isBlank()) return defaultValue;
    try {
      return Integer.parseInt(raw.trim());
    } catch (Exception e) {
      return defaultValue;
    }
  }

  private static RedisConnectOptions readRedisConnectOptions(Environment env) {
    String url = env.getProperty("REDIS_URL");
    if (url != null && !url.isBlank()) {
      return fromUrl(url.trim());
    }

    String host = env.getProperty("REDIS_HOST");
    String portRaw = env.getProperty("REDIS_PORT");
    if ((host == null || host.isBlank()) && (portRaw == null || portRaw.isBlank())) {
      return null;
    }

    String resolvedHost = (host == null || host.isBlank()) ? "127.0.0.1" : host.trim();
    int resolvedPort = readInt(portRaw, 6379);
    return new RedisConnectOptions(resolvedHost, resolvedPort, null);
  }

  private static RedisConnectOptions fromUrl(String redisUrl) {
    try {
      URI uri = URI.create(redisUrl);
      String host = uri.getHost();
      int port = uri.getPort() > 0 ? uri.getPort() : 6379;

      String password = null;
      String userInfo = uri.getUserInfo();
      if (userInfo != null && !userInfo.isBlank()) {
        String[] parts = userInfo.split(":", 2);
        password = parts.length == 2 ? parts[1] : parts[0];
      }

      return new RedisConnectOptions(host == null ? "127.0.0.1" : host, port, password);
    } catch (Exception e) {
      log.warn("REDIS_URL 解析失败：{}", redisUrl);
      return null;
    }
  }

  private static final class RedisConnectOptions {
    private final String host;
    private final int port;
    private final String password;

    private RedisConnectOptions(String host, int port, String password) {
      this.host = host;
      this.port = port;
      this.password = password;
    }
  }
}
