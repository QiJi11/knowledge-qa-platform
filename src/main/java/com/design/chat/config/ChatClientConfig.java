package com.design.chat.config;

import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class ChatClientConfig {
  @Bean
  public OkHttpClient chatOkHttpClient() {
    return new OkHttpClient.Builder()
      .connectTimeout(Duration.ofSeconds(15))
      .readTimeout(Duration.ofSeconds(15))
      .writeTimeout(Duration.ofSeconds(15))
      .callTimeout(Duration.ofSeconds(15))
      .build();
  }
}
