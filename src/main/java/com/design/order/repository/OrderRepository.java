package com.design.order.repository;

import com.design.order.entity.Order;
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
import java.util.List;

@Repository
public class OrderRepository {
  private static final DateTimeFormatter ISO_UTC =
    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneOffset.UTC);

  private final NamedParameterJdbcTemplate jdbc;

  public OrderRepository(NamedParameterJdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  private static String toIso(Timestamp ts) {
    return ts == null ? null : ISO_UTC.format(ts.toInstant());
  }

  private static final RowMapper<Order> ORDER_MAPPER = (ResultSet rs, int rowNum) -> {
    Order o = new Order();
    o.setId(rs.getLong("id"));
    o.setOrderNo(rs.getString("order_no"));
    o.setUserId(rs.getLong("user_id"));
    o.setCourseId(rs.getLong("course_id"));
    o.setAmount(rs.getBigDecimal("amount"));
    o.setPayType(rs.getString("pay_type"));
    o.setStatus(rs.getInt("status"));
    o.setPaidAt(toIso(rs.getTimestamp("paid_at")));
    o.setCreatedAt(toIso(rs.getTimestamp("created_at")));
    o.setUpdatedAt(toIso(rs.getTimestamp("updated_at")));
    return o;
  };

  public Order createOrder(String orderNo, long userId, long courseId, java.math.BigDecimal amount, String payType) {
    String sql = "INSERT INTO orders (order_no, user_id, course_id, amount, pay_type, status) " +
                 "VALUES (:orderNo, :userId, :courseId, :amount, :payType, 0)";
    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("orderNo", orderNo);
    params.addValue("userId", userId);
    params.addValue("courseId", courseId);
    params.addValue("amount", amount);
    params.addValue("payType", payType);

    KeyHolder keyHolder = new GeneratedKeyHolder();
    jdbc.update(sql, params, keyHolder, new String[]{"id"});
    return getOrderByNo(orderNo);
  }

  public Order getOrderByNo(String orderNo) {
    MapSqlParameterSource params = new MapSqlParameterSource().addValue("orderNo", orderNo);
    List<Order> list = jdbc.query(
      "SELECT * FROM orders WHERE order_no = :orderNo", params, ORDER_MAPPER);
    return list.isEmpty() ? null : list.get(0);
  }

  public int payOrder(String orderNo) {
    MapSqlParameterSource params = new MapSqlParameterSource()
      .addValue("orderNo", orderNo);
    return jdbc.update(
      "UPDATE orders SET status = 1, paid_at = NOW(3) WHERE order_no = :orderNo AND status = 0",
      params);
  }

  public int cancelOrder(String orderNo) {
    MapSqlParameterSource params = new MapSqlParameterSource()
      .addValue("orderNo", orderNo);
    return jdbc.update(
      "UPDATE orders SET status = 2 WHERE order_no = :orderNo AND status = 0",
      params);
  }

  public List<Order> listOrders(long userId, Integer status, int page, int pageSize) {
    StringBuilder sql = new StringBuilder("SELECT * FROM orders WHERE user_id = :userId");
    MapSqlParameterSource params = new MapSqlParameterSource().addValue("userId", userId);
    if (status != null) {
      sql.append(" AND status = :status");
      params.addValue("status", status);
    }
    sql.append(" ORDER BY created_at DESC LIMIT :limit OFFSET :offset");
    params.addValue("limit", pageSize);
    params.addValue("offset", (page - 1) * pageSize);
    return jdbc.query(sql.toString(), params, ORDER_MAPPER);
  }

  public int countOrders(long userId, Integer status) {
    StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM orders WHERE user_id = :userId");
    MapSqlParameterSource params = new MapSqlParameterSource().addValue("userId", userId);
    if (status != null) {
      sql.append(" AND status = :status");
      params.addValue("status", status);
    }
    Integer count = jdbc.queryForObject(sql.toString(), params, Integer.class);
    return count == null ? 0 : count;
  }

  public void incrementBuyCount(long courseId) {
    jdbc.update("UPDATE courses SET buy_count = buy_count + 1 WHERE id = :id",
      new MapSqlParameterSource().addValue("id", courseId));
  }
}
