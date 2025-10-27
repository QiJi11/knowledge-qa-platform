package com.design.todo.cache;

import java.util.List;

public interface CacheClient {
  String get(String key);

  void set(String key, String value, int ttlSeconds);

  void del(List<String> keys);
}
