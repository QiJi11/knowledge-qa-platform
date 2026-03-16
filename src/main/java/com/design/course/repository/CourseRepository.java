package com.design.course.repository;

import com.design.course.entity.Course;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Repository("courseV1Repository")
public class CourseRepository {
  private static final DateTimeFormatter ISO_UTC_MILLIS =
    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneOffset.UTC);

  private final NamedParameterJdbcTemplate jdbc;

  public CourseRepository(NamedParameterJdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  private static String toIso(Timestamp ts) {
    if (ts == null) return null;
    return ISO_UTC_MILLIS.format(ts.toInstant());
  }

  private static final RowMapper<Course> COURSE_MAPPER = new RowMapper<>() {
    @Override
    public Course mapRow(ResultSet rs, int rowNum) throws SQLException {
      Course c = new Course();
      c.setId(rs.getLong("id"));
      c.setTitle(rs.getString("title"));
      c.setCategory(rs.getString("category"));
      c.setLevel(rs.getInt("level"));
      c.setCoverUrl(rs.getString("cover_url"));
      c.setSummary(rs.getString("summary"));
      c.setDescription(rs.getString("description"));
      c.setPrice(rs.getBigDecimal("price"));
      c.setOriginalPrice(rs.getBigDecimal("original_price"));
      c.setViewCount(rs.getInt("view_count"));
      c.setBuyCount(rs.getInt("buy_count"));
      c.setTotalLessons(rs.getInt("total_lessons"));
      c.setTotalMinutes(rs.getInt("total_minutes"));
      c.setStatus(rs.getInt("status"));
      c.setPublishedAt(toIso(rs.getTimestamp("published_at")));
      c.setCreatedAt(toIso(rs.getTimestamp("created_at")));
      c.setUpdatedAt(toIso(rs.getTimestamp("updated_at")));
      return c;
    }
  };

  public Course getCourse(long id) {
    MapSqlParameterSource params = new MapSqlParameterSource().addValue("id", id);
    List<Course> list =
      jdbc.query(
        "SELECT id, title, category, level, cover_url, summary, description, price, original_price, view_count, buy_count, total_lessons, total_minutes, status, published_at, created_at, updated_at FROM courses WHERE id = :id",
        params,
        COURSE_MAPPER
      );
    return list.isEmpty() ? null : list.get(0);
  }

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
    String sql =
      "INSERT INTO courses (title, category, level, cover_url, summary, description, total_lessons, total_minutes, status, published_at) " +
      "VALUES (:title, :category, :level, :coverUrl, :summary, :description, :totalLessons, :totalMinutes, :status, :publishedAt)";

    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("title", title);
    params.addValue("category", category);
    params.addValue("level", level);
    params.addValue("coverUrl", coverUrl);
    params.addValue("summary", summary);
    params.addValue("description", description);
    params.addValue("totalLessons", totalLessons);
    params.addValue("totalMinutes", totalMinutes);
    params.addValue("status", status);
    params.addValue("publishedAt", publishedAt);

    KeyHolder keyHolder = new GeneratedKeyHolder();
    jdbc.update(sql, params, keyHolder, new String[] {"id"});

    Number key = keyHolder.getKey();
    if (key == null) {
      throw new RuntimeException("创建课程失败：未获取到课程ID");
    }

    Course created = getCourse(key.longValue());
    if (created == null) {
      throw new RuntimeException("创建课程失败：未查询到课程数据");
    }
    return created;
  }

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
    Timestamp publishedAt
  ) {
    String sql =
      "UPDATE courses SET " +
      "title = :title, " +
      "category = :category, " +
      "level = :level, " +
      "cover_url = :coverUrl, " +
      "summary = :summary, " +
      "description = :description, " +
      "total_lessons = :totalLessons, " +
      "total_minutes = :totalMinutes, " +
      "status = :status, " +
      "published_at = :publishedAt " +
      "WHERE id = :id";

    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("id", id);
    params.addValue("title", title);
    params.addValue("category", category);
    params.addValue("level", level);
    params.addValue("coverUrl", coverUrl);
    params.addValue("summary", summary);
    params.addValue("description", description);
    params.addValue("totalLessons", totalLessons);
    params.addValue("totalMinutes", totalMinutes);
    params.addValue("status", status);
    params.addValue("publishedAt", publishedAt);

    int affected = jdbc.update(sql, params);
    if (affected <= 0) return null;

    return getCourse(id);
  }

  public boolean deleteCourse(long id) {
    MapSqlParameterSource params = new MapSqlParameterSource().addValue("id", id);
    int affected = jdbc.update("DELETE FROM courses WHERE id = :id", params);
    return affected > 0;
  }

  public int countLearningTasks(long courseId) {
    MapSqlParameterSource params = new MapSqlParameterSource().addValue("courseId", courseId);
    Integer count =
      jdbc.queryForObject(
        "SELECT COUNT(*) FROM learning_tasks WHERE course_id = :courseId",
        params,
        Integer.class
      );
    return count == null ? 0 : count;
  }

  public CoursePage listCourses(
    String keyword,
    String category,
    Integer level,
    Integer status,
    String sortBy,
    String order,
    int page,
    int pageSize
  ) {
    StringBuilder whereSql = new StringBuilder(" WHERE 1=1");
    MapSqlParameterSource params = new MapSqlParameterSource();

    if (keyword != null && !keyword.isBlank()) {
      whereSql.append(" AND title LIKE :keyword");
      params.addValue("keyword", "%" + keyword.trim() + "%");
    }

    if (category != null && !category.isBlank()) {
      whereSql.append(" AND category = :category");
      params.addValue("category", category.trim());
    }

    if (level != null) {
      whereSql.append(" AND level = :level");
      params.addValue("level", level);
    }

    if (status != null) {
      whereSql.append(" AND status = :status");
      params.addValue("status", status);
    }

    Integer total = jdbc.queryForObject("SELECT COUNT(*) FROM courses" + whereSql, params, Integer.class);
    if (total == null) total = 0;

    String sortColumn = "published_at";
    if ("createdAt".equals(sortBy)) {
      sortColumn = "created_at";
    } else if ("publishedAt".equals(sortBy)) {
      sortColumn = "published_at";
    } else if ("viewCount".equals(sortBy)) {
      sortColumn = "view_count";
    } else if ("buyCount".equals(sortBy)) {
      sortColumn = "buy_count";
    } else if ("price".equals(sortBy)) {
      sortColumn = "price";
    }

    String sortOrder = "DESC";
    if (order != null && order.equalsIgnoreCase("asc")) {
      sortOrder = "ASC";
    } else if (order != null && order.equalsIgnoreCase("desc")) {
      sortOrder = "DESC";
    }

    int offset = (page - 1) * pageSize;
    params.addValue("limit", pageSize);
    params.addValue("offset", offset);

    String sql =
      "SELECT id, title, category, level, cover_url, summary, description, price, original_price, view_count, buy_count, total_lessons, total_minutes, status, published_at, created_at, updated_at FROM courses" +
      whereSql +
      " ORDER BY " + sortColumn + " " + sortOrder + ", id DESC" +
      " LIMIT :limit OFFSET :offset";

    List<Course> items = jdbc.query(sql, params, COURSE_MAPPER);

    return new CoursePage(items, total, page, pageSize);
  }

  public void deleteAllForTest() {
    jdbc.getJdbcOperations().update("DELETE FROM learning_tasks");
    jdbc.getJdbcOperations().update("DELETE FROM courses");
  }

  public static class CoursePage {
    private final List<Course> items;
    private final int total;
    private final int page;
    private final int pageSize;

    public CoursePage(List<Course> items, int total, int page, int pageSize) {
      this.items = items == null ? new ArrayList<>() : items;
      this.total = total;
      this.page = page;
      this.pageSize = pageSize;
    }

    public List<Course> getItems() {
      return items;
    }

    public int getTotal() {
      return total;
    }

    public int getPage() {
      return page;
    }

    public int getPageSize() {
      return pageSize;
    }
  }
}