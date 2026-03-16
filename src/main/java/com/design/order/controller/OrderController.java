package com.design.order.controller;

import com.design.common.Result;
import com.design.order.entity.Order;
import com.design.order.service.OrderService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/order")
public class OrderController {
  private final OrderService orderService;

  public OrderController(OrderService orderService) {
    this.orderService = orderService;
  }

  @PostMapping("/create")
  public Result<Order> createOrder(@RequestBody JsonNode body) {
    long courseId = body.path("courseId").asLong();
    String payType = body.has("payType") ? body.get("payType").asText() : "alipay";
    Order order = orderService.createOrder(courseId, payType);
    return Result.ok(order);
  }

  @GetMapping("/{orderNo}")
  public Result<Order> getOrder(@PathVariable String orderNo) {
    Order order = orderService.getOrder(orderNo);
    return Result.ok(order);
  }

  @PostMapping("/pay")
  public Result<Map<String, Object>> payOrder(@RequestBody JsonNode body) {
    String orderNo = body.path("orderNo").asText();
    String payType = body.has("payType") ? body.get("payType").asText() : "alipay";
    Order order = orderService.payOrder(orderNo, payType);
    Map<String, Object> data = new HashMap<>();
    data.put("success", true);
    data.put("order", order);
    return Result.ok(data);
  }

  @PostMapping("/{orderNo}/cancel")
  public Result<Void> cancelOrder(@PathVariable String orderNo) {
    orderService.cancelOrder(orderNo);
    return Result.ok(null);
  }

  @GetMapping("/list")
  public Result<Map<String, Object>> listOrders(
    @RequestParam(required = false) Integer page,
    @RequestParam(required = false) Integer pageSize,
    @RequestParam(required = false) Integer status
  ) {
    int resolvedPage = page == null ? 1 : page;
    int resolvedPageSize = pageSize == null ? 20 : pageSize;

    List<Order> items = orderService.listOrders(status, resolvedPage, resolvedPageSize);
    int total = orderService.countOrders(status);

    Map<String, Object> data = new HashMap<>();
    data.put("items", items);
    data.put("total", total);
    data.put("page", resolvedPage);
    data.put("pageSize", resolvedPageSize);
    return Result.ok(data);
  }
}
