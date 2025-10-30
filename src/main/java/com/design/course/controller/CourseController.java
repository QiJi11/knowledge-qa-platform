package com.design.course.controller;

import com.design.course.entity.Course;
import com.design.course.repository.CourseRepository;
import com.design.course.service.CourseService;
import com.design.todo.api.ApiException;
import com.design.todo.api.ApiResponse;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController("courseV1Controller")
@RequestMapping("/api/v1/courses")
public class CourseController {
  private final CourseService service;

  public CourseController(CourseService service) {
    this.service = service;
  }

  private static void ensureObject(JsonNode body) {
    if (body == null || !body.isObject()) {
      throw ApiException.badRequest("请求体必须是 JSON 对象");
    }
  }

  private static String readRequiredString(JsonNode body, String field, int maxLen) {
    JsonNode node = body.get(field);
    if (node == null || !node.isTextual()) {
      throw ApiException.badRequest(field + " 必须是非空字符串");
    }

    String value = node.asText().trim();
    if (value.isEmpty()) {
      throw ApiException.badRequest(field + " 必须是非空字符串");
    }

    if (value.length() > maxLen) {
      throw ApiException.badRequest(field + " 长度不能超过 " + maxLen);
    }

    return value;
  }

  private static String readOptionalString(JsonNode body, String field, int maxLen) {
    JsonNode node = body.get(field);
    if (node == null || node.isNull()) return null;
    if (!node.isTextual()) {
      throw ApiException.badRequest(field + " 必须是字符串或 null");
    }

    String value = node.asText().trim();
    if (value.isEmpty()) return null;

    if (value.length() > maxLen) {
      throw ApiException.badRequest(field + " 长度不能超过 " + maxLen);
    }

    return value;
  }

  private static int readRequiredInt(JsonNode body, String field) {
    JsonNode node = body.get(field);
    if (node == null || !node.isIntegralNumber()) {
      throw ApiException.badRequest(field + " 必须是整数");
    }
    return node.asInt();
  }

  private static int readOptionalIntWithDefault(JsonNode body, String field, int defaultValue) {
    if (!body.has(field)) return defaultValue;

    JsonNode node = body.get(field);
    if (node == null || node.isNull() || !node.isIntegralNumber()) {
      throw ApiException.badRequest(field + " 必须是整数");
    }

    return node.asInt();
  }

  private static int readOptionalNonNegativeInt(JsonNode body, String field, int defaultValue) {
    if (!body.has(field)) return defaultValue;

    JsonNode node = body.get(field);
    if (node == null || node.isNull() || !node.isIntegralNumber()) {
      throw ApiException.badRequest(field + " 必须是整数");
    }

    long value = node.asLong();
    if (value < 0) {
      throw ApiException.badRequest(field + " 不能为负数");
    }
    if (value > Integer.MAX_VALUE) {
      throw ApiException.badRequest(field + " 过大");
    }

    return (int) value;
  }

  private static Timestamp readOptionalTimestamp(JsonNode body, String field) {
    if (!body.has(field)) return null;

    JsonNode node = body.get(field);
    if (node == null || node.isNull()) return null;
    if (!node.isTextual()) {
      throw ApiException.badRequest(field + " 必须是 ISO8601 时间字符串或 null");
    }

    String raw = node.asText().trim();
    if (raw.isEmpty()) return null;

    try {
      return Timestamp.from(Instant.parse(raw));
    } catch (Exception e) {
      throw ApiException.badRequest(field + " 格式必须是 ISO8601（如 2026-01-01T00:00:00Z）");
    }
  }

  private static long parseCourseIdOrThrow(String raw) {
    if (raw == null || raw.isBlank()) {
      throw ApiException.badRequest("courseId 必须是正整数");
    }

    try {
      long id = Long.parseLong(raw.trim());
      if (id <= 0) {
        throw ApiException.badRequest("courseId 必须是正整数");
      }
      return id;
    } catch (NumberFormatException e) {
      throw ApiException.badRequest("courseId 必须是正整数");
    }
  }

  private static void ensureLevel(int level) {
    if (level < 1 || level > 3) {
      throw ApiException.badRequest("level 只能是 1、2、3");
    }
  }

  private static void ensureStatus(int status) {
    if (status < 0 || status > 2) {
      throw ApiException.badRequest("status 只能是 0、1、2");
    }
  }

  @PostMapping
  public ApiResponse<Course> createCourse(@RequestBody JsonNode body) {
    ensureObject(body);

    String title = readRequiredString(body, "title", 200);
    String category = readRequiredString(body, "category", 50);

    int level = readRequiredInt(body, "level");
    ensureLevel(level);

    String coverUrl = readOptionalString(body, "coverUrl", 500);
    String summary = readOptionalString(body, "summary", 500);
    String description = readOptionalString(body, "description", 20000);

    int totalLessons = readOptionalNonNegativeInt(body, "totalLessons", 0);
    int totalMinutes = readOptionalNonNegativeInt(body, "totalMinutes", 0);

    int status = readOptionalIntWithDefault(body, "status", 1);
    ensureStatus(status);

    Timestamp publishedAt = readOptionalTimestamp(body, "publishedAt");

    Course created =
      service.createCourse(
        title,
        category,
        level,
        coverUrl,
        summary,
        description,
        totalLessons,
        totalMinutes,
        status,
        publishedAt
      );

    return ApiResponse.ok(created);
  }

  @GetMapping("/{courseId}")
  public ApiResponse<Course> getCourse(@PathVariable("courseId") String courseId) {
    long id = parseCourseIdOrThrow(courseId);

    Course course = service.getCourse(id);
    if (course == null) {
      throw ApiException.notFound("未找到该课程");
    }

    return ApiResponse.ok(course);
  }

  @GetMapping
  public ApiResponse<Map<String, Object>> listCourses(
    @RequestParam(required = false) String keyword,
    @RequestParam(required = false) String category,
    @RequestParam(required = false) Integer level,
    @RequestParam(required = false) Integer status,
    @RequestParam(required = false) String sortBy,
    @RequestParam(required = false) String order,
    @RequestParam(required = false) Integer page,
    @RequestParam(required = false) Integer pageSize
  ) {
    if (level != null) {
      ensureLevel(level);
    }
    if (status != null) {
      ensureStatus(status);
    }

    String resolvedSortBy = sortBy == null ? "publishedAt" : sortBy.trim();
    if (!resolvedSortBy.equals("publishedAt") && !resolvedSortBy.equals("createdAt")) {
      throw ApiException.badRequest("sortBy 只能是 publishedAt 或 createdAt");
    }

    String resolvedOrder = order == null ? "desc" : order.trim().toLowerCase();
    if (!resolvedOrder.equals("asc") && !resolvedOrder.equals("desc")) {
      throw ApiException.badRequest("order 只能是 asc 或 desc");
    }

    int resolvedPage = page == null ? 1 : page;
    if (resolvedPage < 1) {
      throw ApiException.badRequest("page 必须大于等于 1");
    }

    int resolvedPageSize = pageSize == null ? 20 : pageSize;
    if (resolvedPageSize <= 0) {
      throw ApiException.badRequest("pageSize 必须大于 0");
    }
    if (resolvedPageSize > 50) {
      throw ApiException.badRequest("pageSize 不能超过 50");
    }

    CourseRepository.CoursePage result =
      service.listCourses(
        keyword,
        category,
        level,
        status,
        resolvedSortBy,
        resolvedOrder,
        resolvedPage,
        resolvedPageSize
      );

    Map<String, Object> data = new HashMap<>();
    data.put("items", result.getItems());
    data.put("page", result.getPage());
    data.put("pageSize", result.getPageSize());
    data.put("total", result.getTotal());

    return ApiResponse.ok(data);
  }

  @PutMapping("/{courseId}")
  public ApiResponse<Course> updateCourse(@PathVariable("courseId") String courseId, @RequestBody JsonNode body) {
    long id = parseCourseIdOrThrow(courseId);
    ensureObject(body);

    String title = readRequiredString(body, "title", 200);
    String category = readRequiredString(body, "category", 50);

    int level = readRequiredInt(body, "level");
    ensureLevel(level);

    String coverUrl = readOptionalString(body, "coverUrl", 500);
    String summary = readOptionalString(body, "summary", 500);
    String description = readOptionalString(body, "description", 20000);

    int totalLessons = readOptionalNonNegativeInt(body, "totalLessons", 0);
    int totalMinutes = readOptionalNonNegativeInt(body, "totalMinutes", 0);

    int status = readOptionalIntWithDefault(body, "status", 1);
    ensureStatus(status);

    boolean hasPublishedAt = body.has("publishedAt");
    Timestamp publishedAt = readOptionalTimestamp(body, "publishedAt");

    Course updated =
      service.updateCourse(
        id,
        title,
        category,
        level,
        coverUrl,
        summary,
        description,
        totalLessons,
        totalMinutes,
        status,
        hasPublishedAt,
        publishedAt
      );

    if (updated == null) {
      throw ApiException.notFound("未找到该课程");
    }

    return ApiResponse.ok(updated);
  }

  @DeleteMapping("/{courseId}")
  public ApiResponse<Map<String, Object>> deleteCourse(@PathVariable("courseId") String courseId) {
    long id = parseCourseIdOrThrow(courseId);

    service.deleteCourse(id);

    Map<String, Object> data = new HashMap<>();
    data.put("deleted", true);
    return ApiResponse.ok(data);
  }
}