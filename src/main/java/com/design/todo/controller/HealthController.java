package com.design.todo.controller;

import com.design.todo.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {
  private static final DateTimeFormatter ISO_UTC_MILLIS =
    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneOffset.UTC);

  @GetMapping("/health")
  public ApiResponse<Map<String, Object>> health() {
    Map<String, Object> data = new HashMap<>();
    data.put("ok", true);
    data.put("service", "backend");
    data.put("time", ISO_UTC_MILLIS.format(Instant.now()));
    return ApiResponse.ok(data);
  }
}
