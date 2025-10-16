package com.design.learningtask.repository;

import com.design.learningtask.entity.LearningTask;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Repository("learningTaskV1Repository")
public class LearningTaskRepository {
  private static final DateTimeFormatter ISO_UTC_MILLIS =
    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneOffset.UTC);

  private final NamedParameterJdbcTemplate jdbc;

  public LearningTaskRepository(NamedParameterJdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  private static String toIso(Timestamp ts) {
    if (ts == null) return null;
    return ISO_UTC_MILLIS.format(ts.toInstant());
  }

  private static String toYmd(Date date) {
    if (date == null) return null;
    return date.toString();
  }

  private static final RowMapper<LearningTask> TASK_MAPPER = new RowMapper<>() {
    @Override
    public LearningTask mapRow(ResultSet rs, int rowNum) throws SQLException {
      return new LearningTask(
        rs.getLong("id"),
        rs.getLong("learner_id"),
        rs.getLong("course_id"),
        rs.getString("title"),
        rs.getString("note"),
        rs.getInt("status"),
        rs.getInt("priority"),
        toYmd(rs.getDate("due_date")),
        toIso(rs.getTimestamp("completed_at")),
        toIso(rs.getTimestamp("created_at")),
        toIso(rs.getTimestamp("updated_at"))
      );
    }
  };

  public boolean courseExists(long courseId) {
    MapSqlParameterSource params = new MapSqlParameterSource().addValue("courseId", courseId);
    Integer count =
      jdbc.queryForObject("SELECT COUNT(*) FROM courses WHERE id = :courseId", params, Integer.class);
    return count != null && count > 0;
  }

  public LearningTask getTask(long learnerId, long taskId) {
    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("learnerId", learnerId);
    params.addValue("taskId", taskId);

    List<LearningTask> list =
      jdbc.query(
        "SELECT id, learner_id, course_id, title, note, status, priority, due_date, completed_at, created_at, updated_at " +
        "FROM learning_tasks WHERE learner_id = :learnerId AND id = :taskId",
        params,
        TASK_MAPPER
      );
    return list.isEmpty() ? null : list.get(0);
  }

  public LearningTask createTask(
    long learnerId,
    long courseId,
    String title,
    String note,
    int status,
    int priority,
    Date dueDate,
    Timestamp completedAt
  ) {
    String sql =
      "INSERT INTO learning_tasks (learner_id, course_id, title, note, status, priority, due_date, completed_at) " +
      "VALUES (:learnerId, :courseId, :title, :note, :status, :priority, :dueDate, :completedAt)";

    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("learnerId", learnerId);
    params.addValue("courseId", courseId);
    params.addValue("title", title);
    params.addValue("note", note);
    params.addValue("status", status);
    params.addValue("priority", priority);
    params.addValue("dueDate", dueDate);
    params.addValue("completedAt", completedAt);

    KeyHolder keyHolder = new GeneratedKeyHolder();
    jdbc.update(sql, params, keyHolder, new String[] {"id"});

    Number key = keyHolder.getKey();
    if (key == null) {
      throw new RuntimeException("创建学习任务失败：未获取到任务ID");
    }

    LearningTask created = getTask(learnerId, key.longValue());
    if (created == null) {
      throw new RuntimeException("创建学习任务失败：未查询到任务数据");
    }

    return created;
  }

  public LearningTask updateTask(
    long learnerId,
    long taskId,
    String title,
    String note,
    int status,
    int priority,
    Date dueDate,
    Timestamp completedAt
  ) {
    String sql =
      "UPDATE learning_tasks SET " +
      "title = :title, " +
      "note = :note, " +
      "status = :status, " +
      "priority = :priority, " +
      "due_date = :dueDate, " +
      "completed_at = :completedAt " +
      "WHERE learner_id = :learnerId AND id = :taskId";

    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("learnerId", learnerId);
    params.addValue("taskId", taskId);
    params.addValue("title", title);
    params.addValue("note", note);
    params.addValue("status", status);
    params.addValue("priority", priority);
    params.addValue("dueDate", dueDate);
    params.addValue("completedAt", completedAt);

    int affected = jdbc.update(sql, params);
    if (affected <= 0) return null;

    return getTask(learnerId, taskId);
  }

  public boolean deleteTask(long learnerId, long taskId) {
    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("learnerId", learnerId);
    params.addValue("taskId", taskId);

    int affected = jdbc.update("DELETE FROM learning_tasks WHERE learner_id = :learnerId AND id = :taskId", params);
    return affected > 0;
  }

  public TaskPage listTasks(
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
    StringBuilder whereSql = new StringBuilder(" WHERE learner_id = :learnerId");
    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("learnerId", learnerId);

    if (courseId != null) {
      whereSql.append(" AND course_id = :courseId");
      params.addValue("courseId", courseId);
    }

    if (status != null) {
      whereSql.append(" AND status = :status");
      params.addValue("status", status);
    }

    if (priority != null) {
      whereSql.append(" AND priority = :priority");
      params.addValue("priority", priority);
    }

    if (dueFrom != null) {
      whereSql.append(" AND due_date >= :dueFrom");
      params.addValue("dueFrom", dueFrom);
    }

    if (dueTo != null) {
      whereSql.append(" AND due_date <= :dueTo");
      params.addValue("dueTo", dueTo);
    }

    if (keyword != null && !keyword.isBlank()) {
      whereSql.append(" AND (title LIKE :keyword OR note LIKE :keyword)");
      params.addValue("keyword", "%" + keyword.trim() + "%");
    }

    Integer total = jdbc.queryForObject("SELECT COUNT(*) FROM learning_tasks" + whereSql, params, Integer.class);
    if (total == null) total = 0;

    String sortColumn = "due_date";
    if ("priority".equals(sortBy)) {
      sortColumn = "priority";
    } else if ("createdAt".equals(sortBy)) {
      sortColumn = "created_at";
    } else if ("updatedAt".equals(sortBy)) {
      sortColumn = "updated_at";
    } else if ("dueDate".equals(sortBy)) {
      sortColumn = "due_date";
    }

    String sortOrder = "ASC";
    if (order != null && order.equalsIgnoreCase("desc")) {
      sortOrder = "DESC";
    } else if (order != null && order.equalsIgnoreCase("asc")) {
      sortOrder = "ASC";
    }

    int offset = (page - 1) * pageSize;
    params.addValue("limit", pageSize);
    params.addValue("offset", offset);

    String sql =
      "SELECT id, learner_id, course_id, title, note, status, priority, due_date, completed_at, created_at, updated_at " +
      "FROM learning_tasks" +
      whereSql +
      " ORDER BY " + sortColumn + " " + sortOrder + ", id DESC" +
      " LIMIT :limit OFFSET :offset";

    List<LearningTask> items = jdbc.query(sql, params, TASK_MAPPER);

    return new TaskPage(items, total, page, pageSize);
  }

  public static class TaskPage {
    private final List<LearningTask> items;
    private final int total;
    private final int page;
    private final int pageSize;

    public TaskPage(List<LearningTask> items, int total, int page, int pageSize) {
      this.items = items == null ? new ArrayList<>() : items;
      this.total = total;
      this.page = page;
      this.pageSize = pageSize;
    }

    public List<LearningTask> getItems() {
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