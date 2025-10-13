package com.design.todo.service;

import com.design.todo.model.Course;
import com.design.todo.model.CourseListResult;
import com.design.todo.model.CourseQuery;
import com.design.todo.model.DeleteResult;
import com.design.todo.repo.CourseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CourseService {
  private final CourseRepository repo;

  public CourseService(CourseRepository repo) {
    this.repo = repo;
  }

  public List<Course> listCourses() {
    return repo.listCourses();
  }

  public CourseListResult listCourses(CourseQuery query) {
    return repo.listCourses(query);
  }

  public Course getCourse(String id) {
    return repo.getCourse(id);
  }

  @Transactional
  public Course createCourse(
    String title,
    String summary,
    String description,
    Integer status,
    Integer priceCents,
    String teacherName
  ) {
    return repo.createCourse(title, summary, description, status, priceCents, teacherName);
  }

  @Transactional
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
    return repo.patchCourse(
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
  }

  @Transactional
  public DeleteResult deleteCourse(String id) {
    return repo.deleteCourse(id);
  }

  /**
   * 批量创建课程（测试用途：用于演示事务回滚）
   * 如果任一 title 包含 "ROLLBACK"，则抛出异常触发回滚
   */
  @Transactional
  public void batchCreateForTest(List<String> titles) {
    for (String title : titles) {
      repo.createCourse(title, null, null, 0, null, null);
      if (title.contains("ROLLBACK")) {
        throw new RuntimeException("模拟异常：触发事务回滚");
      }
    }
  }
}
