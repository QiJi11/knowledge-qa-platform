package com.design.chat.config;

import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class ChatClientConfig {
  private static final Logger log = LoggerFactory.getLogger(ChatClientConfig.class);

  @Bean
  public OkHttpClient chatOkHttpClient() {
    log.info("初始化 OkHttpClient（直连模式，超时 30s）");
    return new OkHttpClient.Builder()
      .connectTimeout(Duration.ofSeconds(30))
      .readTimeout(Duration.ofSeconds(30))
      .writeTimeout(Duration.ofSeconds(30))
      .callTimeout(Duration.ofSeconds(60))
      .build();
  }
}

