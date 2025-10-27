package com.design.todo.cache;

import java.util.List;

public class NoopCacheClient implements CacheClient {
  @Override
  public String get(String key) {
    return null;
  }

  @Override
  public void set(String key, String value, int ttlSeconds) {
    // ignore
  }

  @Override
  public void del(List<String> keys) {
    // ignore
  }
}
