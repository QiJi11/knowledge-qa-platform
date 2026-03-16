package com.design;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 知识库问答与学习助手后端平台 启动类
 */
@SpringBootApplication
@EnableCaching
public class KnowledgeQaPlatformApplication {
  public static void main(String[] args) {
    SpringApplication.run(KnowledgeQaPlatformApplication.class, args);
  }
}
