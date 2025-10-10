package com.design.todo;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.design")
@EnableCaching
public class DesignTodoBackendApplication {
  public static void main(String[] args) {
    SpringApplication.run(DesignTodoBackendApplication.class, args);
  }
}
