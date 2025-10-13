package com.design.todo.repo;

import com.design.todo.model.Course;
import com.design.todo.model.CourseListResult;
import com.design.todo.model.CourseQuery;
import com.design.todo.model.DeleteResult;

import java.util.List;

public interface CourseRepository {
  List<Course> listCourses();

  /** 支持分页、过滤、搜索的列表查询 */
  CourseListResult listCourses(CourseQuery query);

  Course getCourse(String id);

  Course createCourse(
    String title,
    String summary,
    String description,
    Integer status,
    Integer priceCents,
    String teacherName
  );

  Course patchCourse(
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
  );

  DeleteResult deleteCourse(String id);

  /** 删除所有课程（仅用于测试） */
  void deleteAll();
}
