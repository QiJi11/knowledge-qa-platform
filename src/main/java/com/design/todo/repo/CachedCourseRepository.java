package com.design.todo.repo;

import com.design.todo.cache.CacheClient;
import com.design.todo.model.Course;
import com.design.todo.model.CourseListResult;
import com.design.todo.model.CourseQuery;
import com.design.todo.model.DeleteResult;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

/**
 * 课程仓库缓存装饰器
 * 实现 Cache-Aside 模式：
 * - 读操作：先查缓存，未命中再查数据库，然后回写缓存（带 TTL）
 * - 写操作：先写数据库，再删除相关缓存
 */
public class CachedCourseRepository implements CourseRepository {
  private static final String LIST_KEY = "courses:list:v1";

  private static String itemKey(String id) {
    return "courses:item:" + id + ":v1";
  }

  private final CourseRepository delegate;
  private final CacheClient cache;
  private final ObjectMapper objectMapper;
  private final int ttlSeconds;

  public CachedCourseRepository(
    CourseRepository delegate,
    CacheClient cache,
    ObjectMapper objectMapper,
    int ttlSeconds
  ) {
    this.delegate = delegate;
    this.cache = cache;
    this.objectMapper = objectMapper;
    this.ttlSeconds = ttlSeconds;
  }

  private List<Course> tryParseCourseList(String raw) {
    try {
      List<Course> list = objectMapper.readValue(raw, new TypeReference<>() {});
      return list == null ? null : list;
    } catch (Exception e) {
      return null;
    }
  }

  private Course tryParseCourse(String raw) {
    try {
      return objectMapper.readValue(raw, Course.class);
    } catch (Exception e) {
      return null;
    }
  }

  private void tryCacheSet(String key, Object value) {
    try {
      String raw = objectMapper.writeValueAsString(value);
      cache.set(key, raw, ttlSeconds);
    } catch (Exception e) {
      // ignore
    }
  }

  @Override
  public List<Course> listCourses() {
    String cached = cache.get(LIST_KEY);
    if (cached != null) {
      List<Course> list = tryParseCourseList(cached);
      if (list != null) return list;
    }

    List<Course> courses = delegate.listCourses();
    tryCacheSet(LIST_KEY, courses);
    return courses;
  }

  @Override
  public CourseListResult listCourses(CourseQuery query) {
    // 带查询参数的列表不缓存（组合太多）
    // 只有无参数查询才走缓存
    return delegate.listCourses(query);
  }

  @Override
  public Course getCourse(String id) {
    String cached = cache.get(itemKey(id));
    if (cached != null) {
      Course course = tryParseCourse(cached);
      if (course != null) return course;
    }

    Course course = delegate.getCourse(id);
    if (course != null) {
      tryCacheSet(itemKey(id), course);
    }
    return course;
  }

  @Override
  public Course createCourse(
    String title,
    String summary,
    String description,
    Integer status,
    Integer priceCents,
    String teacherName
  ) {
    Course course = delegate.createCourse(title, summary, description, status, priceCents, teacherName);
    cache.del(List.of(LIST_KEY));
    if (course != null && course.getId() != null) {
      tryCacheSet(itemKey(String.valueOf(course.getId())), course);
    }
    return course;
  }

  @Override
  public Course patchCourse(
    String id,
    boolean updateTitle,
    String title,
    boolean updateSummary,
    String summary,
    boolean updateDescription,
    String description,
    boolean updateStatus,
    Integer status,
    boolean updatePriceCents,
    Integer priceCents,
    boolean updateTeacherName,
    String teacherName
  ) {
    Course course =
      delegate.patchCourse(
        id,
        updateTitle,
        title,
        updateSummary,
        summary,
        updateDescription,
        description,
        updateStatus,
        status,
        updatePriceCents,
        priceCents,
        updateTeacherName,
        teacherName
      );

    cache.del(List.of(LIST_KEY, itemKey(id)));
    if (course != null) {
      tryCacheSet(itemKey(id), course);
    }
    return course;
  }

  @Override
  public DeleteResult deleteCourse(String id) {
    DeleteResult result = delegate.deleteCourse(id);
    cache.del(List.of(LIST_KEY, itemKey(id)));
    return result;
  }

  @Override
  public void deleteAll() {
    delegate.deleteAll();
    cache.del(List.of(LIST_KEY));
  }
}
