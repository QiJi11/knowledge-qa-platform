package com.design.stats.repository;

import com.design.stats.dto.ByCourseStatsResponse;
import com.design.stats.dto.OverviewStatsResponse;
import com.design.stats.dto.TrendStatsResponse;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 统计数据仓库
 */
@Repository
public class StatsRepository {
    private final NamedParameterJdbcTemplate jdbc;

    public StatsRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * 获取总览统计
     */
    public OverviewStatsResponse.Summary getOverviewStats(long learnerId, Date from, Date to) {
        String sql = """
            SELECT
              COUNT(*) AS totalTasks,
              SUM(CASE WHEN status = 2 THEN 1 ELSE 0 END) AS completedTasks,
              SUM(CASE WHEN due_date < CURDATE() AND status != 2 THEN 1 ELSE 0 END) AS overdueTasks,
              SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) AS inProgressTasks,
              SUM(CASE WHEN status = 0 THEN 1 ELSE 0 END) AS pendingTasks,
              SUM(CASE WHEN status = 3 THEN 1 ELSE 0 END) AS cancelledTasks,
              SUM(CASE WHEN priority = 1 THEN 1 ELSE 0 END) AS lowPriority,
              SUM(CASE WHEN priority = 2 THEN 1 ELSE 0 END) AS mediumPriority,
              SUM(CASE WHEN priority = 3 THEN 1 ELSE 0 END) AS highPriority
            FROM learning_tasks
            WHERE learner_id = :learnerId
              AND created_at >= :from
              AND created_at < :to
            """;

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("learnerId", learnerId);
        params.addValue("from", from);
        params.addValue("to", to);

        return jdbc.queryForObject(sql, params, (rs, rowNum) -> {
            OverviewStatsResponse.Summary summary = new OverviewStatsResponse.Summary();
            int totalTasks = rs.getInt("totalTasks");
            int completedTasks = rs.getInt("completedTasks");

            summary.setTotalTasks(totalTasks);
            summary.setCompletedTasks(completedTasks);
            summary.setCompletionRate(totalTasks > 0 ? (double) completedTasks / totalTasks : 0.0);
            summary.setOverdueTasks(rs.getInt("overdueTasks"));
            summary.setInProgressTasks(rs.getInt("inProgressTasks"));

            return summary;
        });
    }

    /**
     * 获取按状态统计
     */
    public Map<String, Integer> getStatsByStatus(long learnerId, Date from, Date to) {
        String sql = """
            SELECT
              SUM(CASE WHEN status = 0 THEN 1 ELSE 0 END) AS pending,
              SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) AS inProgress,
              SUM(CASE WHEN status = 2 THEN 1 ELSE 0 END) AS completed,
              SUM(CASE WHEN status = 3 THEN 1 ELSE 0 END) AS cancelled
            FROM learning_tasks
            WHERE learner_id = :learnerId
              AND created_at >= :from
              AND created_at < :to
            """;

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("learnerId", learnerId);
        params.addValue("from", from);
        params.addValue("to", to);

        return jdbc.queryForObject(sql, params, (rs, rowNum) -> {
            Map<String, Integer> map = new LinkedHashMap<>();
            map.put("pending", rs.getInt("pending"));
            map.put("inProgress", rs.getInt("inProgress"));
            map.put("completed", rs.getInt("completed"));
            map.put("cancelled", rs.getInt("cancelled"));
            return map;
        });
    }

    /**
     * 获取按优先级统计
     */
    public Map<String, Integer> getStatsByPriority(long learnerId, Date from, Date to) {
        String sql = """
            SELECT
              SUM(CASE WHEN priority = 1 THEN 1 ELSE 0 END) AS low,
              SUM(CASE WHEN priority = 2 THEN 1 ELSE 0 END) AS medium,
              SUM(CASE WHEN priority = 3 THEN 1 ELSE 0 END) AS high
            FROM learning_tasks
            WHERE learner_id = :learnerId
              AND created_at >= :from
              AND created_at < :to
            """;

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("learnerId", learnerId);
        params.addValue("from", from);
        params.addValue("to", to);

        return jdbc.queryForObject(sql, params, (rs, rowNum) -> {
            Map<String, Integer> map = new LinkedHashMap<>();
            map.put("low", rs.getInt("low"));
            map.put("medium", rs.getInt("medium"));
            map.put("high", rs.getInt("high"));
            return map;
        });
    }

    /**
     * 获取按课程聚合统计
     */
    public List<ByCourseStatsResponse.CourseStats> getStatsByCourse(
            long learnerId, Date from, Date to, String sortBy, String order) {

        String sql = """
            SELECT
              c.id AS courseId,
              c.title AS courseTitle,
              c.category,
              COUNT(t.id) AS totalTasks,
              SUM(CASE WHEN t.status = 2 THEN 1 ELSE 0 END) AS completedTasks,
              SUM(CASE WHEN t.due_date < CURDATE() AND t.status != 2 THEN 1 ELSE 0 END) AS overdueTasks,
              AVG(CASE WHEN t.status = 2 THEN DATEDIFF(t.completed_at, t.created_at) ELSE NULL END) AS avgCompletionDays
            FROM learning_tasks t
            INNER JOIN courses c ON t.course_id = c.id
            WHERE t.learner_id = :learnerId
              AND t.created_at >= :from
              AND t.created_at < :to
            GROUP BY c.id, c.title, c.category
            ORDER BY %s %s
            """;

        // 排序字段白名单
        String sortColumn = switch (sortBy != null ? sortBy : "totalTasks") {
            case "completionRate" -> "completedTasks";
            case "overdueTasks" -> "overdueTasks";
            default -> "totalTasks";
        };

        String sortOrder = (order != null && order.equalsIgnoreCase("asc")) ? "ASC" : "DESC";
        sql = String.format(sql, sortColumn, sortOrder);

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("learnerId", learnerId);
        params.addValue("from", from);
        params.addValue("to", to);

        return jdbc.query(sql, params, (rs, rowNum) -> {
            ByCourseStatsResponse.CourseStats stats = new ByCourseStatsResponse.CourseStats();
            stats.setCourseId(rs.getLong("courseId"));
            stats.setCourseTitle(rs.getString("courseTitle"));
            stats.setCategory(rs.getString("category"));

            int totalTasks = rs.getInt("totalTasks");
            int completedTasks = rs.getInt("completedTasks");

            stats.setTotalTasks(totalTasks);
            stats.setCompletedTasks(completedTasks);
            stats.setCompletionRate(totalTasks > 0 ? (double) completedTasks / totalTasks : 0.0);
            stats.setOverdueTasks(rs.getInt("overdueTasks"));

            double avgDays = rs.getDouble("avgCompletionDays");
            stats.setAvgCompletionDays(rs.wasNull() ? null : avgDays);

            return stats;
        });
    }

    /**
     * 获取趋势统计 - 创建数
     */
    public Map<String, Integer> getCreatedCountsByDate(long learnerId, Date from, Date to) {
        String sql = """
            SELECT
              DATE(created_at) AS date,
              COUNT(*) AS createdCount
            FROM learning_tasks
            WHERE learner_id = :learnerId
              AND created_at >= :from
              AND created_at < :to
            GROUP BY DATE(created_at)
            ORDER BY date
            """;

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("learnerId", learnerId);
        params.addValue("from", from);
        params.addValue("to", to);

        List<Map<String, Object>> results = jdbc.queryForList(sql, params);

        Map<String, Integer> map = new HashMap<>();
        for (Map<String, Object> row : results) {
            Date date = (Date) row.get("date");
            Integer count = ((Number) row.get("createdCount")).intValue();
            map.put(date.toString(), count);
        }

        return map;
    }

    /**
     * 获取趋势统计 - 完成数
     */
    public Map<String, Integer> getCompletedCountsByDate(long learnerId, Date from, Date to) {
        String sql = """
            SELECT
              DATE(completed_at) AS date,
              COUNT(*) AS completedCount
            FROM learning_tasks
            WHERE learner_id = :learnerId
              AND completed_at >= :from
              AND completed_at < :to
              AND status = 2
            GROUP BY DATE(completed_at)
            ORDER BY date
            """;

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("learnerId", learnerId);
        params.addValue("from", from);
        params.addValue("to", to);

        List<Map<String, Object>> results = jdbc.queryForList(sql, params);

        Map<String, Integer> map = new HashMap<>();
        for (Map<String, Object> row : results) {
            Date date = (Date) row.get("date");
            Integer count = ((Number) row.get("completedCount")).intValue();
            map.put(date.toString(), count);
        }

        return map;
    }

    /**
     * 补齐日期序列（Java 实现）
     */
    public List<TrendStatsResponse.DataPoint> fillDateGaps(
            Map<String, Integer> createdCounts,
            Map<String, Integer> completedCounts,
            LocalDate from,
            LocalDate to) {

        List<TrendStatsResponse.DataPoint> dataPoints = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

        LocalDate current = from;
        while (!current.isAfter(to)) {
            String dateStr = current.format(formatter);
            int createdCount = createdCounts.getOrDefault(dateStr, 0);
            int completedCount = completedCounts.getOrDefault(dateStr, 0);

            dataPoints.add(new TrendStatsResponse.DataPoint(dateStr, createdCount, completedCount));
            current = current.plusDays(1);
        }

        return dataPoints;
    }
}
