package com.design.learningtask.service;

import com.design.learningtask.entity.LearningTask;
import com.design.learningtask.repository.LearningTaskRepository;
import com.design.todo.api.ApiException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;

@Service("learningTaskV1Service")
public class LearningTaskService {
  private final LearningTaskRepository repo;

  public LearningTaskService(LearningTaskRepository repo) {
    this.repo = repo;
  }

  public LearningTask getTask(long learnerId, long taskId) {
    return repo.getTask(learnerId, taskId);
  }

  public LearningTaskRepository.TaskPage listTasks(
    long learnerId,
    Long courseId,
    Integer status,
    Integer priority,
    Date dueFrom,
    Date dueTo,
    String keyword,
    String sortBy,
    String order,
    int page,
    int pageSize
  ) {
    return repo.listTasks(learnerId, courseId, status, priority, dueFrom, dueTo, keyword, sortBy, order, page, pageSize);
  }

  @Transactional
  @CacheEvict(cacheNames = {"stats:overview", "stats:by-course", "stats:trend"}, allEntries = true)
  public LearningTask createTask(
    long learnerId,
    long courseId,
    String title,
    String note,
    Integer priority,
    Date dueDate,
    Integer status
  ) {
    if (!repo.courseExists(courseId)) {
      throw ApiException.badRequest("courseId 不存在");
    }

    int resolvedPriority = priority == null ? 2 : priority;
    int resolvedStatus = status == null ? 0 : status;

    Timestamp completedAt = resolvedStatus == 2 ? Timestamp.from(Instant.now()) : null;

    return repo.createTask(
      learnerId,
      courseId,
      title,
      note,
      resolvedStatus,
      resolvedPriority,
      dueDate,
      completedAt
    );
  }

  private static void ensureStatusTransitionAllowed(int from, int to) {
    if (from == to) return;

    if (from == 0) {
      if (to == 1 || to == 3) return;
      throw ApiException.badRequest("状态流转不允许：" + from + " -> " + to);
    }

    if (from == 1) {
      if (to == 2 || to == 3) return;
      throw ApiException.badRequest("状态流转不允许：" + from + " -> " + to);
    }

    if (from == 2) {
      if (to == 1) return;
      throw ApiException.badRequest("状态流转不允许：" + from + " -> " + to);
    }

    if (from == 3) {
      throw ApiException.badRequest("状态流转不允许：" + from + " -> " + to);
    }

    throw ApiException.badRequest("status 不合法");
  }

  private static Date toSqlDateOrNull(String ymd) {
    if (ymd == null || ymd.isBlank()) return null;
    try {
      return Date.valueOf(LocalDate.parse(ymd));
    } catch (Exception e) {
      return null;
    }
  }

  private static Timestamp toTimestampOrNull(String iso) {
    if (iso == null || iso.isBlank()) return null;
    try {
      return Timestamp.from(Instant.parse(iso));
    } catch (Exception e) {
      return null;
    }
  }

  @Transactional
  @CacheEvict(cacheNames = {"stats:overview", "stats:by-course", "stats:trend"}, allEntries = true)
  public LearningTask patchTask(
    long learnerId,
    long taskId,
    boolean updateTitle,
    String title,
    boolean updateNote,
    String note,
    boolean updatePriority,
    Integer priority,
    boolean updateDueDate,
    Date dueDate,
    boolean updateStatus,
    Integer status
  ) {
    LearningTask existing = repo.getTask(learnerId, taskId);
    if (existing == null) return null;

    String resolvedTitle = updateTitle ? title : existing.getTitle();
    String resolvedNote = updateNote ? note : existing.getNote();
    int resolvedPriority = updatePriority ? priority : existing.getPriority();
    Date resolvedDueDate = updateDueDate ? dueDate : toSqlDateOrNull(existing.getDueDate());

    int existingStatus = existing.getStatus();
    int resolvedStatus = updateStatus ? status : existingStatus;

    Timestamp resolvedCompletedAt = toTimestampOrNull(existing.getCompletedAt());

    if (updateStatus) {
      ensureStatusTransitionAllowed(existingStatus, resolvedStatus);

      // status 变成 2 时：自动设置 completedAt = now
      if (resolvedStatus == 2 && existingStatus != 2) {
        resolvedCompletedAt = Timestamp.from(Instant.now());
      }

      // status 从 2 改回 1 时：清空 completedAt
      if (existingStatus == 2 && resolvedStatus == 1) {
        resolvedCompletedAt = null;
      }

      // 取消任务时，不应该保留完成时间
      if (resolvedStatus == 3) {
        resolvedCompletedAt = null;
      }
    }

    return repo.updateTask(
      learnerId,
      taskId,
      resolvedTitle,
      resolvedNote,
      resolvedStatus,
      resolvedPriority,
      resolvedDueDate,
      resolvedCompletedAt
    );
  }

  @Transactional
  @CacheEvict(cacheNames = {"stats:overview", "stats:by-course", "stats:trend"}, allEntries = true)
  public void deleteTask(long learnerId, long taskId) {
    boolean deleted = repo.deleteTask(learnerId, taskId);
    if (!deleted) {
      throw ApiException.notFound("未找到该任务");
    }
  }
}
