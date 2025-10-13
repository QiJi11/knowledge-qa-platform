package com.design.todo.repo;

import com.design.todo.model.Course;
import com.design.todo.model.CourseListResult;
import com.design.todo.model.CourseQuery;
import com.design.todo.model.DeleteResult;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class JdbcCourseRepository implements CourseRepository {
  private static final DateTimeFormatter ISO_UTC_MILLIS =
    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneOffset.UTC);

  private final JdbcTemplate jdbc;

  public JdbcCourseRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  private static String toIso(Timestamp ts) {
    if (ts == null) return null;
    return ISO_UTC_MILLIS.format(ts.toInstant());
  }

  private static String toIso(Instant instant) {
    if (instant == null) return null;
    return ISO_UTC_MILLIS.format(instant);
  }

  private static final RowMapper<Course> COURSE_MAPPER = new RowMapper<>() {
    @Override
    public Course mapRow(ResultSet rs, int rowNum) throws SQLException {
      return new Course(
        rs.getString("id"),
        rs.getString("title"),
        rs.getString("summary"),
        rs.getString("description"),
        rs.getInt("status"),
        rs.getObject("price_cents", Integer.class),
        rs.getString("teacher_name"),
        toIso(rs.getTimestamp("created_at")),
        toIso(rs.getTimestamp("updated_at"))
      );
    }
  };

  @Override
  public List<Course> listCourses() {
    return jdbc.query(
      "SELECT id, title, summary, description, status, price_cents, teacher_name, created_at, updated_at FROM courses ORDER BY created_at DESC, id DESC",
      COURSE_MAPPER
    );
  }

  @Override
  public CourseListResult listCourses(CourseQuery query) {
    // 构建 WHERE 条件
    StringBuilder whereSql = new StringBuilder();
    List<Object> whereArgs = new ArrayList<>();

    if (query.getStatus() != null) {
      whereSql.append(" WHERE status = ?");
      whereArgs.add(query.getStatus());
    }

    if (query.getQ() != null && !query.getQ().trim().isEmpty()) {
      if (whereSql.length() == 0) {
        whereSql.append(" WHERE");
      } else {
        whereSql.append(" AND");
      }
      whereSql.append(" title LIKE ?");
      whereArgs.add("%" + query.getQ().trim() + "%");
    }

    // 查询总数
    String countSql = "SELECT COUNT(*) FROM courses" + whereSql;
    Integer total = jdbc.queryForObject(countSql, Integer.class, whereArgs.toArray());
    if (total == null) total = 0;

    // 查询数据（带分页和排序）
    StringBuilder dataSql = new StringBuilder();
    dataSql.append("SELECT id, title, summary, description, status, price_cents, teacher_name, created_at, updated_at FROM courses");
    dataSql.append(whereSql);

    // 动态排序
    String sortBy = query.getSortBy();
    if ("price".equals(sortBy)) {
      dataSql.append(" ORDER BY price_cents ASC, id DESC");  // 价格升序
    } else if ("time".equals(sortBy)) {
      dataSql.append(" ORDER BY created_at DESC, id DESC");  // 时间降序（显式）
    } else {
      dataSql.append(" ORDER BY created_at DESC, id DESC");  // 默认：时间降序
    }

    List<Object> dataArgs = new ArrayList<>(whereArgs);
    if (query.getLimit() != null && query.getLimit() > 0) {
      dataSql.append(" LIMIT ?");
      dataArgs.add(query.getLimit());
    }

    if (query.getOffset() != null && query.getOffset() > 0) {
      dataSql.append(" OFFSET ?");
      dataArgs.add(query.getOffset());
    }

    List<Course> items = jdbc.query(dataSql.toString(), COURSE_MAPPER, dataArgs.toArray());

    return new CourseListResult(items, total, query.getLimit(), query.getOffset());
  }

  @Override
  public Course getCourse(String id) {
    List<Course> list = jdbc.query(
      "SELECT id, title, summary, description, status, price_cents, teacher_name, created_at, updated_at FROM courses WHERE id = ?",
      COURSE_MAPPER,
      id
    );
    return list.isEmpty() ? null : list.get(0);
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
    String id = UUID.randomUUID().toString();
    Instant now = Instant.now();
    int resolvedStatus = status == null ? 0 : status;

    jdbc.update(
      "INSERT INTO courses (id, title, summary, description, status, price_cents, teacher_name, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
      id,
      title,
      summary,
      description,
      resolvedStatus,
      priceCents,
      teacherName,
      Timestamp.from(now),
      Timestamp.from(now)
    );

    return new Course(
      id,
      title,
      summary,
      description,
      resolvedStatus,
      priceCents,
      teacherName,
      toIso(now),
      toIso(now)
    );
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
    Course existing = getCourse(id);
    if (existing == null) return null;

    List<String> sets = new ArrayList<>();
    List<Object> args = new ArrayList<>();

    if (updateTitle) {
      sets.add("title = ?");
      args.add(title);
    }

    if (updateSummary) {
      sets.add("summary = ?");
      args.add(summary);
    }

    if (updateDescription) {
      sets.add("description = ?");
      args.add(description);
    }

    if (updateStatus) {
      sets.add("status = ?");
      args.add(status);
    }

    if (updatePriceCents) {
      sets.add("price_cents = ?");
      args.add(priceCents);
    }

    if (updateTeacherName) {
      sets.add("teacher_name = ?");
      args.add(teacherName);
    }

    Instant now = Instant.now();
    sets.add("updated_at = ?");
    args.add(Timestamp.from(now));

    args.add(id);

    String sql = "UPDATE courses SET " + String.join(", ", sets) + " WHERE id = ?";
    jdbc.update(sql, args.toArray());

    Course updated = getCourse(id);
    return updated != null
      ? updated
      : new Course(
        existing.getId(),
        updateTitle ? title : existing.getTitle(),
        updateSummary ? summary : existing.getSummary(),
        updateDescription ? description : existing.getDescription(),
        updateStatus ? status : existing.getStatus(),
        updatePriceCents ? priceCents : existing.getPriceCents(),
        updateTeacherName ? teacherName : existing.getTeacherName(),
        existing.getCreatedAt(),
        toIso(now)
      );
  }

  @Override
  public DeleteResult deleteCourse(String id) {
    int affected = jdbc.update("DELETE FROM courses WHERE id = ?", id);
    if (affected <= 0) {
      return new DeleteResult(false, 0);
    }

    Integer remaining = jdbc.queryForObject("SELECT COUNT(*) FROM courses", Integer.class);
    return new DeleteResult(true, remaining == null ? 0 : remaining);
  }

  @Override
  public void deleteAll() {
    jdbc.update("DELETE FROM courses");
  }
}
