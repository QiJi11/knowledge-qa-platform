package com.design.todo.controller;

import com.design.todo.api.ApiException;
import com.design.todo.api.ApiResponse;
import com.design.todo.model.Course;
import com.design.todo.model.CourseListResult;
import com.design.todo.model.CourseQuery;
import com.design.todo.model.CourseStatus;
import com.design.todo.model.DeleteResult;
import com.design.todo.service.CourseService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/courses")
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

  private static String readTitleOrThrow(JsonNode body) {
    JsonNode node = body.get("title");
    if (node == null || !node.isTextual()) {
      throw ApiException.badRequest("title 必须是非空字符串");
    }

    String title = node.asText();
    if (title.trim().isEmpty()) {
      throw ApiException.badRequest("title 必须是非空字符串");
    }
    if (title.length() > 200) {
      throw ApiException.badRequest("title 长度不能超过 200");
    }

    return title.trim();
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

  private static Integer readStatusOrDefault(JsonNode body, Integer defaultValue) {
    if (!body.has("status")) return defaultValue;

    JsonNode node = body.get("status");
    if (node == null || node.isNull() || !node.isIntegralNumber()) {
      throw ApiException.badRequest("status 必须是整数");
    }

    int status = node.asInt();
    if (CourseStatus.fromCode(status) == null) {
      throw ApiException.badRequest("status 只能是 0、1、2");
    }
    return status;
  }

  private static Integer readPriceCentsOrNull(JsonNode body) {
    if (!body.has("priceCents")) return null;

    JsonNode node = body.get("priceCents");
    if (node == null || node.isNull()) return null;
    if (!node.isIntegralNumber()) {
      throw ApiException.badRequest("priceCents 必须是整数或 null");
    }

    long value = node.asLong();
    if (value < 0) {
      throw ApiException.badRequest("priceCents 不能为负数");
    }
    if (value > Integer.MAX_VALUE) {
      throw ApiException.badRequest("priceCents 过大");
    }
    return (int) value;
  }

  @GetMapping
  public ApiResponse<Map<String, Object>> listCourses(
    @RequestParam(required = false) Integer limit,
    @RequestParam(required = false) Integer offset,
    @RequestParam(required = false) Integer status,
    @RequestParam(required = false) String q,
    @RequestParam(required = false) String sortBy
  ) {
    // 参数校验
    if (limit != null && limit <= 0) {
      throw ApiException.badRequest("limit 必须大于 0");
    }
    if (offset != null && offset < 0) {
      throw ApiException.badRequest("offset 不能为负数");
    }
    // MySQL 限制：OFFSET 必须和 LIMIT 一起使用
    if (offset != null && offset > 0 && limit == null) {
      throw ApiException.badRequest("使用 offset 时必须同时指定 limit");
    }
    if (status != null && CourseStatus.fromCode(status) == null) {
      throw ApiException.badRequest("status 只能是 0、1、2");
    }
    // 校验 sortBy 参数
    if (sortBy != null && !sortBy.equals("price") && !sortBy.equals("time")) {
      throw ApiException.badRequest("sortBy 只能是 price 或 time");
    }

    CourseQuery query = new CourseQuery(limit, offset, status, q, sortBy);

    // 如果没有任何查询参数，使用旧接口（兼容性 + 缓存）
    if (query.isEmpty()) {
      List<Course> courses = service.listCourses();
      Map<String, Object> data = new HashMap<>();
      data.put("items", courses);
      return ApiResponse.ok(data);
    }

    // 有查询参数，使用新接口
    CourseListResult result = service.listCourses(query);
    Map<String, Object> data = new HashMap<>();
    data.put("items", result.getItems());
    data.put("total", result.getTotal());
    if (result.getLimit() != null) {
      data.put("limit", result.getLimit());
    }
    if (result.getOffset() != null) {
      data.put("offset", result.getOffset());
    }
    return ApiResponse.ok(data);
  }

  @GetMapping("/{id}")
  public ApiResponse<Map<String, Object>> getCourse(@PathVariable("id") String id) {
    Course course = service.getCourse(String.valueOf(id));
    if (course == null) throw ApiException.notFound("未找到该课程");

    Map<String, Object> data = new HashMap<>();
    data.put("item", course);
    return ApiResponse.ok(data);
  }

  @PostMapping
  public ApiResponse<Map<String, Object>> createCourse(@RequestBody JsonNode body) {
    ensureObject(body);

    String title = readTitleOrThrow(body);
    String summary = readOptionalString(body, "summary", 500);
    String description = readOptionalString(body, "description", 20000);
    Integer status = readStatusOrDefault(body, 0);
    Integer priceCents = readPriceCentsOrNull(body);
    String teacherName = readOptionalString(body, "teacherName", 100);

    Course course = service.createCourse(title, summary, description, status, priceCents, teacherName);

    Map<String, Object> data = new HashMap<>();
    data.put("item", course);
    return ApiResponse.ok(data, "创建成功");
  }

  @PatchMapping("/{id}")
  public ApiResponse<Map<String, Object>> patchCourse(
    @PathVariable("id") String id,
    @RequestBody JsonNode body
  ) {
    ensureObject(body);

    boolean hasTitle = body.has("title");
    boolean hasSummary = body.has("summary");
    boolean hasDescription = body.has("description");
    boolean hasStatus = body.has("status");
    boolean hasPriceCents = body.has("priceCents");
    boolean hasTeacherName = body.has("teacherName");

    if (!hasTitle && !hasSummary && !hasDescription && !hasStatus && !hasPriceCents && !hasTeacherName) {
      throw ApiException.badRequest(
        "至少需要提供一个可更新字段（title、summary、description、status、priceCents、teacherName）"
      );
    }

    String title = null;
    if (hasTitle) {
      title = readTitleOrThrow(body);
    }

    String summary = null;
    if (hasSummary) {
      summary = readOptionalString(body, "summary", 500);
    }

    String description = null;
    if (hasDescription) {
      description = readOptionalString(body, "description", 20000);
    }

    Integer status = null;
    if (hasStatus) {
      status = readStatusOrDefault(body, null);
    }

    Integer priceCents = null;
    if (hasPriceCents) {
      priceCents = readPriceCentsOrNull(body);
    }

    String teacherName = null;
    if (hasTeacherName) {
      teacherName = readOptionalString(body, "teacherName", 100);
    }

    Course course =
      service.patchCourse(
        String.valueOf(id),
        hasTitle,
        title,
        hasSummary,
        summary,
        hasDescription,
        description,
        hasStatus,
        status,
        hasPriceCents,
        priceCents,
        hasTeacherName,
        teacherName
      );

    if (course == null) throw ApiException.notFound("未找到该课程");

    Map<String, Object> data = new HashMap<>();
    data.put("item", course);
    return ApiResponse.ok(data, "更新成功");
  }

  @DeleteMapping("/{id}")
  public ApiResponse<Map<String, Object>> deleteCourse(@PathVariable("id") String id) {
    DeleteResult result = service.deleteCourse(String.valueOf(id));
    if (!result.isDeleted()) throw ApiException.notFound("未找到该课程");

    Map<String, Object> data = new HashMap<>();
    data.put("remaining", result.getRemaining());
    return ApiResponse.ok(data, "删除成功");
  }
}
