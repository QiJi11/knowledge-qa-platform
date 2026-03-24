package com.design.faq.repository;

import com.design.faq.entity.Faq;
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

@Repository("faqV1Repository")
public class FaqRepository {
  private static final DateTimeFormatter ISO_UTC_MILLIS =
    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneOffset.UTC);

  private final NamedParameterJdbcTemplate jdbc;

  public FaqRepository(NamedParameterJdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  private static String toIso(Timestamp ts) {
    if (ts == null) return null;
    return ISO_UTC_MILLIS.format(ts.toInstant());
  }

  private static final RowMapper<Faq> FAQ_MAPPER = new RowMapper<>() {
    @Override
    public Faq mapRow(ResultSet rs, int rowNum) throws SQLException {
      return new Faq(
        rs.getLong("id"),
        rs.getString("question"),
        rs.getString("answer"),
        rs.getString("keywords"),
        rs.getString("category"),
        rs.getInt("hit_count"),
        toIso(rs.getTimestamp("created_at")),
        toIso(rs.getTimestamp("updated_at"))
      );
    }
  };

  public Faq getFaq(long id) {
    MapSqlParameterSource params = new MapSqlParameterSource().addValue("id", id);
    List<Faq> list =
      jdbc.query(
        "SELECT id, question, answer, keywords, category, hit_count, created_at, updated_at FROM faq WHERE id = :id",
        params,
        FAQ_MAPPER
      );
    return list.isEmpty() ? null : list.get(0);
  }

  public Faq createFaq(String question, String answer, String keywords, String category, Integer hitCount) {
    String sql =
      "INSERT INTO faq (question, answer, keywords, category, hit_count) " +
      "VALUES (:question, :answer, :keywords, :category, :hitCount)";

    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("question", question);
    params.addValue("answer", answer);
    params.addValue("keywords", keywords);
    params.addValue("category", category);
    params.addValue("hitCount", hitCount);

    KeyHolder keyHolder = new GeneratedKeyHolder();
    jdbc.update(sql, params, keyHolder, new String[] {"id"});

    Number key = keyHolder.getKey();
    if (key == null) {
      throw new RuntimeException("创建 FAQ 失败：未获取到 FAQ ID");
    }

    Faq created = getFaq(key.longValue());
    if (created == null) {
      throw new RuntimeException("创建 FAQ 失败：未查询到 FAQ 数据");
    }
    return created;
  }

  public Faq updateFaq(long id, String question, String answer, String keywords, String category, Integer hitCount) {
    String sql =
      "UPDATE faq SET " +
      "question = :question, " +
      "answer = :answer, " +
      "keywords = :keywords, " +
      "category = :category, " +
      "hit_count = :hitCount " +
      "WHERE id = :id";

    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("id", id);
    params.addValue("question", question);
    params.addValue("answer", answer);
    params.addValue("keywords", keywords);
    params.addValue("category", category);
    params.addValue("hitCount", hitCount);

    int affected = jdbc.update(sql, params);
    if (affected <= 0) return null;

    return getFaq(id);
  }

  public boolean deleteFaq(long id) {
    MapSqlParameterSource params = new MapSqlParameterSource().addValue("id", id);
    int affected = jdbc.update("DELETE FROM faq WHERE id = :id", params);
    return affected > 0;
  }

  public List<Faq> searchByKeywords(String keyword) {
    if (keyword == null || keyword.isBlank()) {
      return new ArrayList<>();
    }

    String trimmed = keyword.trim();

    // 优先使用 FULLTEXT 全文检索（利用 idx_faq_fulltext 索引）
    // MATCH...AGAINST 支持 ngram 分词，相关性评分排序，性能远优于 LIKE
    try {
      String fulltextSql =
        "SELECT id, question, answer, keywords, category, hit_count, created_at, updated_at," +
        "  MATCH(question, keywords) AGAINST (:kw IN BOOLEAN MODE) AS score " +
        "FROM faq " +
        "WHERE MATCH(question, keywords) AGAINST (:kw IN BOOLEAN MODE) " +
        "ORDER BY score DESC, hit_count DESC " +
        "LIMIT 5";

      MapSqlParameterSource ftParams = new MapSqlParameterSource().addValue("kw", trimmed);
      List<Faq> ftResults = jdbc.query(fulltextSql, ftParams, FAQ_MAPPER);
      if (!ftResults.isEmpty()) {
        return ftResults;
      }
    } catch (Exception e) {
      // 全文检索失败（如 MySQL 版本不支持），降级到 LIKE
    }

    // LIKE 兜底：分词后多字段模糊匹配
    String[] tokens = trimmed.split("[\\s,，。？?！!、;；：:]+");
    List<String> validTokens = new ArrayList<>();
    for (String t : tokens) {
      String s = t.trim();
      if (s.length() >= 2) {
        validTokens.add(s);
      }
    }
    if (validTokens.isEmpty()) {
      validTokens.add(trimmed);
    }

    StringBuilder sql = new StringBuilder(
      "SELECT id, question, answer, keywords, category, hit_count, created_at, updated_at FROM faq WHERE ");
    MapSqlParameterSource params = new MapSqlParameterSource();
    List<String> conditions = new ArrayList<>();
    for (int i = 0; i < validTokens.size(); i++) {
      String paramName = "kw" + i;
      String likeVal = "%" + validTokens.get(i) + "%";
      conditions.add("(question LIKE :" + paramName + " OR COALESCE(keywords,'') LIKE :" + paramName + " OR answer LIKE :" + paramName + ")");
      params.addValue(paramName, likeVal);
    }
    sql.append(String.join(" OR ", conditions));
    sql.append(" ORDER BY hit_count DESC, id DESC LIMIT 5");

    return jdbc.query(sql.toString(), params, FAQ_MAPPER);
  }

  public List<String> findAllQuestions() {
    return jdbc.query(
      "SELECT question FROM faq",
      new MapSqlParameterSource(),
      (rs, rowNum) -> rs.getString("question")
    );
  }

  public List<Faq> findAll() {
    return jdbc.query(
      "SELECT id, question, answer, keywords, category, hit_count, created_at, updated_at FROM faq ORDER BY id",
      new MapSqlParameterSource(),
      FAQ_MAPPER
    );
  }

  public FaqPage listFaqs(String keyword, int page, int pageSize) {
    StringBuilder whereSql = new StringBuilder(" WHERE 1=1");
    MapSqlParameterSource params = new MapSqlParameterSource();

    if (keyword != null && !keyword.isBlank()) {
      whereSql.append(" AND (question LIKE :keyword OR COALESCE(keywords, '') LIKE :keyword)");
      params.addValue("keyword", "%" + keyword.trim() + "%");
    }

    Integer total = jdbc.queryForObject("SELECT COUNT(*) FROM faq" + whereSql, params, Integer.class);
    if (total == null) total = 0;

    int offset = (page - 1) * pageSize;
    params.addValue("limit", pageSize);
    params.addValue("offset", offset);

    String sql =
      "SELECT id, question, answer, keywords, category, hit_count, created_at, updated_at " +
      "FROM faq" +
      whereSql +
      " ORDER BY hit_count DESC, id DESC" +
      " LIMIT :limit OFFSET :offset";

    List<Faq> items = jdbc.query(sql, params, FAQ_MAPPER);
    return new FaqPage(items, total, page, pageSize);
  }

  public static class FaqPage {
    private final List<Faq> items;
    private final int total;
    private final int page;
    private final int pageSize;

    public FaqPage(List<Faq> items, int total, int page, int pageSize) {
      this.items = items == null ? new ArrayList<>() : items;
      this.total = total;
      this.page = page;
      this.pageSize = pageSize;
    }

    public List<Faq> getItems() {
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
