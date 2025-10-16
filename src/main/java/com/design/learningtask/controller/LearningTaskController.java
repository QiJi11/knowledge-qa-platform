package com.design.learningtask.controller;

import com.design.learningtask.entity.LearningTask;
import com.design.learningtask.repository.LearningTaskRepository;
import com.design.learningtask.service.LearningTaskService;
import com.design.todo.api.ApiException;
import com.design.todo.api.ApiResponse;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController("learningTaskV1Controller")
@RequestMapping("/api/v1/learners/{learnerId}/learning-tasks")
public class LearningTaskController {
  private final LearningTaskService service;

  public LearningTaskController(LearningTaskService service) {
    this.service = service;
  }

  private static void ensureObject(JsonNode body) {
    if (body == null || !body.isObject()) {
      throw ApiException.badRequest("请求体必须是 JSON 对象");
    }
  }

  private static long parsePositiveLongOrThrow(String raw, String field) {
    if (raw == null || raw.isBlank()) {
      throw ApiException.badRequest(field + " 必须是正整数");
    }

    try {
      long value = Long.parseLong(raw.trim());
      if (value <= 0) {
        throw ApiException.badRequest(field + " 必须是正整数");
      }
      return value;
    } catch (NumberFormatException e) {
      throw ApiException.badRequest(field + " 必须是正整数");
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

  private static Long readRequiredLong(JsonNode body, String field) {
    JsonNode node = body.get(field);
    if (node == null || !node.isIntegralNumber()) {
      throw ApiException.badRequest(field + " 必须是正整数");
    }

    long value = node.asLong();
    if (value <= 0) {
      throw ApiException.badRequest(field + " 必须是正整数");
    }

    return value;
  }

  private static String readOptionalString(JsonNode body, String field, int maxLen) {
    JsonNode node = body.get(field);
    if (node == null || node.isMissingNode()) return null;
    if (node.isNull()) return null;
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

  private static Integer readOptionalInt(JsonNode body, String field) {
    if (!body.has(field)) return null;

    JsonNode node = body.get(field);
    if (node == null || node.isNull() || !node.isIntegralNumber()) {
      throw ApiException.badRequest(field + " 必须是整数");
    }

    long value = node.asLong();
    if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
      throw ApiException.badRequest(field + " 过大");
    }

    return (int) value;
  }

  private static Date readOptionalDate(JsonNode body, String field) {
    if (!body.has(field)) return null;

    JsonNode node = body.get(field);
    if (node == null || node.isNull()) return null;

    if (!node.isTextual()) {
      throw ApiException.badRequest(field + " 必须是 YYYY-MM-DD 或 null");
    }

    String raw = node.asText().trim();
    if (raw.isEmpty()) return null;

    try {
      return Date.valueOf(LocalDate.parse(raw));
    } catch (Exception e) {
      throw ApiException.badRequest(field + " 格式必须是 YYYY-MM-DD");
    }
  }

  private static Date parseDateParamOrNull(String raw, String field) {
    if (raw == null || raw.isBlank()) return null;
    try {
      return Date.valueOf(LocalDate.parse(raw.trim()));
    } catch (Exception e) {
      throw ApiException.badRequest(field + " 格式必须是 YYYY-MM-DD");
    }
  }

  private static Integer parseIntParamOrNull(String raw, String field) {
    if (raw == null || raw.isBlank()) return null;
    try {
      return Integer.parseInt(raw.trim());
    } catch (Exception e) {
      throw ApiException.badRequest(field + " 必须是整数");
    }
  }

  private static Long parseLongParamOrNull(String raw, String field) {
    if (raw == null || raw.isBlank()) return null;
    try {
      return Long.parseLong(raw.trim());
    } catch (Exception e) {
      throw ApiException.badRequest(field + " 必须是正整数");
    }
  }

  private static void ensureStatus(int status) {
    if (status < 0 || status > 3) {
      throw ApiException.badRequest("status 只能是 0、1、2、3");
    }
  }

  private static void ensurePriority(int priority) {
    if (priority < 1 || priority > 3) {
      throw ApiException.badRequest("priority 只能是 1、2、3");
    }
  }

  @PostMapping
  public ApiResponse<LearningTask> createTask(
    @PathVariable("learnerId") String learnerIdRaw,
    @RequestBody JsonNode body
  ) {
    long learnerId = parsePositiveLongOrThrow(learnerIdRaw, "learnerId");
    ensureObject(body);

    long courseId = readRequiredLong(body, "courseId");
    String title = readRequiredString(body, "title", 200);

    String note = readOptionalString(body, "note", 500);

    Integer priority = readOptionalInt(body, "priority");
    if (priority != null) {
      ensurePriority(priority);
    }

    Date dueDate = readOptionalDate(body, "dueDate");

    Integer status = readOptionalInt(body, "status");
    if (status != null) {
      ensureStatus(status);
    }

    LearningTask created = service.createTask(learnerId, courseId, title, note, priority, dueDate, status);
    return ApiResponse.ok(created);
  }

  @GetMapping("/{taskId}")
  public ApiResponse<LearningTask> getTask(
    @PathVariable("learnerId") String learnerIdRaw,
    @PathVariable("taskId") String taskIdRaw
  ) {
    long learnerId = parsePositiveLongOrThrow(learnerIdRaw, "learnerId");
    long taskId = parsePositiveLongOrThrow(taskIdRaw, "taskId");

    LearningTask task = service.getTask(learnerId, taskId);
    if (task == null) {
      throw ApiException.notFound("未找到该任务");
    }

    return ApiResponse.ok(task);
  }

  @GetMapping
  public ApiResponse<Map<String, Object>> listTasks(
    @PathVariable("learnerId") String learnerIdRaw,
    @RequestParam(required = false) String courseId,
    @RequestParam(required = false) String status,
    @RequestParam(required = false) String priority,
    @RequestParam(required = false) String dueFrom,
    @RequestParam(required = false) String dueTo,
    @RequestParam(required = false) String keyword,
    @RequestParam(required = false) String sortBy,
    @RequestParam(required = false) String order,
    @RequestParam(required = false) String page,
    @RequestParam(required = false) String pageSize
  ) {
    long learnerId = parsePositiveLongOrThrow(learnerIdRaw, "learnerId");

    Long parsedCourseId = parseLongParamOrNull(courseId, "courseId");
    if (parsedCourseId != null && parsedCourseId <= 0) {
      throw ApiException.badRequest("courseId 必须是正整数");
    }

    Integer parsedStatus = parseIntParamOrNull(status, "status");
    if (parsedStatus != null) {
      ensureStatus(parsedStatus);
    }

    Integer parsedPriority = parseIntParamOrNull(priority, "priority");
    if (parsedPriority != null) {
      ensurePriority(parsedPriority);
    }

    Date parsedDueFrom = parseDateParamOrNull(dueFrom, "dueFrom");
    Date parsedDueTo = parseDateParamOrNull(dueTo, "dueTo");
    if (parsedDueFrom != null && parsedDueTo != null && parsedDueFrom.after(parsedDueTo)) {
      throw ApiException.badRequest("dueFrom 不能晚于 dueTo");
    }

    String resolvedSortBy = sortBy == null ? "dueDate" : sortBy.trim();
    if (
      !resolvedSortBy.equals("dueDate") &&
      !resolvedSortBy.equals("priority") &&
      !resolvedSortBy.equals("createdAt") &&
      !resolvedSortBy.equals("updatedAt")
    ) {
      throw ApiException.badRequest("sortBy 只能是 dueDate、priority、createdAt、updatedAt");
    }

    String resolvedOrder = order == null ? "asc" : order.trim().toLowerCase();
    if (!resolvedOrder.equals("asc") && !resolvedOrder.equals("desc")) {
      throw ApiException.badRequest("order 只能是 asc 或 desc");
    }

    Integer parsedPage = parseIntParamOrNull(page, "page");
    int resolvedPage = parsedPage == null ? 1 : parsedPage;
    if (resolvedPage < 1) {
      throw ApiException.badRequest("page 必须大于等于 1");
    }

    Integer parsedPageSize = parseIntParamOrNull(pageSize, "pageSize");
    int resolvedPageSize = parsedPageSize == null ? 20 : parsedPageSize;
    if (resolvedPageSize <= 0) {
      throw ApiException.badRequest("pageSize 必须大于 0");
    }
    if (resolvedPageSize > 50) {
      throw ApiException.badRequest("pageSize 不能超过 50");
    }

    LearningTaskRepository.TaskPage result =
      service.listTasks(
        learnerId,
        parsedCourseId,
        parsedStatus,
        parsedPriority,
        parsedDueFrom,
        parsedDueTo,
        keyword,
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

  @PatchMapping("/{taskId}")
  public ApiResponse<LearningTask> patchTask(
    @PathVariable("learnerId") String learnerIdRaw,
    @PathVariable("taskId") String taskIdRaw,
    @RequestBody JsonNode body
  ) {
    long learnerId = parsePositiveLongOrThrow(learnerIdRaw, "learnerId");
    long taskId = parsePositiveLongOrThrow(taskIdRaw, "taskId");
    ensureObject(body);

    boolean hasTitle = body.has("title");
    boolean hasNote = body.has("note");
    boolean hasPriority = body.has("priority");
    boolean hasDueDate = body.has("dueDate");
    boolean hasStatus = body.has("status");

    if (!hasTitle && !hasNote && !hasPriority && !hasDueDate && !hasStatus) {
      throw ApiException.badRequest("至少需要提供一个可更新字段（title、note、priority、dueDate、status）");
    }

    String title = null;
    if (hasTitle) {
      title = readRequiredString(body, "title", 200);
    }

    String note = null;
    if (hasNote) {
      note = readOptionalString(body, "note", 500);
    }

    Integer priority = null;
    if (hasPriority) {
      priority = readOptionalInt(body, "priority");
      if (priority == null) {
        throw ApiException.badRequest("priority 必须是整数");
      }
      ensurePriority(priority);
    }

    Date dueDate = null;
    if (hasDueDate) {
      dueDate = readOptionalDate(body, "dueDate");
    }

    Integer status = null;
    if (hasStatus) {
      status = readOptionalInt(body, "status");
      if (status == null) {
        throw ApiException.badRequest("status 必须是整数");
      }
      ensureStatus(status);
    }

    LearningTask updated =
      service.patchTask(
        learnerId,
        taskId,
        hasTitle,
        title,
        hasNote,
        note,
        hasPriority,
        priority,
        hasDueDate,
        dueDate,
        hasStatus,
        status
      );

    if (updated == null) {
      throw ApiException.notFound("未找到该任务");
    }

    return ApiResponse.ok(updated);
  }

  @DeleteMapping("/{taskId}")
  public ApiResponse<Map<String, Object>> deleteTask(
    @PathVariable("learnerId") String learnerIdRaw,
    @PathVariable("taskId") String taskIdRaw
  ) {
    long learnerId = parsePositiveLongOrThrow(learnerIdRaw, "learnerId");
    long taskId = parsePositiveLongOrThrow(taskIdRaw, "taskId");

    service.deleteTask(learnerId, taskId);

    Map<String, Object> data = new HashMap<>();
    data.put("deleted", true);
    return ApiResponse.ok(data);
  }
}