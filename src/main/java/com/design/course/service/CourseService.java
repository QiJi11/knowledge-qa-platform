package com.design.course.service;

import com.design.course.entity.Course;
import com.design.course.repository.CourseRepository;
import com.design.todo.api.ApiException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;

@Service("courseV1Service")
public class CourseService {
  private final CourseRepository repo;

  public CourseService(CourseRepository repo) {
    this.repo = repo;
  }

  public Course getCourse(long id) {
    return repo.getCourse(id);
  }

  public CourseRepository.CoursePage listCourses(
    String keyword,
    String category,
    Integer level,
    Integer status,
    String sortBy,
    String order,
    int page,
    int pageSize
  ) {
    return repo.listCourses(keyword, category, level, status, sortBy, order, page, pageSize);
  }

  @Transactional
  @CacheEvict(cacheNames = {"stats:overview", "stats:by-course", "stats:trend"}, allEntries = true)
  public Course createCourse(
    String title,
    String category,
    Integer level,
    String coverUrl,
    String summary,
    String description,
    Integer totalLessons,
    Integer totalMinutes,
    Integer status,
    Timestamp publishedAt
  ) {
    int resolvedStatus = status == null ? 1 : status;
    Timestamp resolvedPublishedAt = publishedAt;

    if (resolvedStatus == 1 && resolvedPublishedAt == null) {
      resolvedPublishedAt = Timestamp.from(Instant.now());
    }

    return repo.createCourse(
      title,
      category,
      level,
      coverUrl,
      summary,
      description,
      totalLessons,
      totalMinutes,
      resolvedStatus,
      resolvedPublishedAt
    );
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
  public Course updateCourse(
    long id,
    String title,
    String category,
    Integer level,
    String coverUrl,
    String summary,
    String description,
    Integer totalLessons,
    Integer totalMinutes,
    Integer status,
    boolean hasPublishedAt,
    Timestamp publishedAt
  ) {
    Course existing = repo.getCourse(id);
    if (existing == null) return null;

    int resolvedStatus = status == null ? existing.getStatus() : status;

    Timestamp resolvedPublishedAt;
    if (hasPublishedAt) {
      resolvedPublishedAt = publishedAt;
    } else {
      resolvedPublishedAt = toTimestampOrNull(existing.getPublishedAt());
    }

    if (resolvedStatus == 1 && resolvedPublishedAt == null) {
      resolvedPublishedAt = Timestamp.from(Instant.now());
    }

    return repo.updateCourse(
      id,
      title,
      category,
      level,
      coverUrl,
      summary,
      description,
      totalLessons,
      totalMinutes,
      resolvedStatus,
      resolvedPublishedAt
    );
  }

  @Transactional
  @CacheEvict(cacheNames = {"stats:overview", "stats:by-course", "stats:trend"}, allEntries = true)
  public void deleteCourse(long id) {
    int taskCount = repo.countLearningTasks(id);
    if (taskCount > 0) {
      throw ApiException.conflict("该课程下还有学习任务，无法删除");
    }

    boolean deleted = repo.deleteCourse(id);
    if (!deleted) {
      throw ApiException.notFound("未找到该课程");
    }
  }
}
