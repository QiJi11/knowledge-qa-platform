package com.design.order.service;

import com.design.common.ApiException;
import com.design.course.entity.Course;
import com.design.course.service.CourseService;
import com.design.order.entity.Order;
import com.design.order.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {
  private final OrderRepository orderRepository;
  private final CourseService courseService;

  public OrderService(OrderRepository orderRepository, CourseService courseService) {
    this.orderRepository = orderRepository;
    this.courseService = courseService;
  }

  public Order createOrder(long courseId, String payType) {
    Course course = courseService.getCourse(courseId);
    if (course == null) {
      throw ApiException.notFound("课程不存在");
    }
    if (course.getPrice() == null || course.getPrice().doubleValue() <= 0) {
      throw ApiException.badRequest("该课程为免费课程，无需购买");
    }

    String orderNo = "ORD" + System.currentTimeMillis() + (int)(Math.random() * 1000);
    Order order = orderRepository.createOrder(orderNo, 1L, courseId, course.getPrice(), payType);
    if (order != null) {
      order.setCourseTitle(course.getTitle());
      order.setCourseCoverUrl(course.getCoverUrl());
    }
    return order;
  }

  public Order getOrder(String orderNo) {
    Order order = orderRepository.getOrderByNo(orderNo);
    if (order == null) {
      throw ApiException.notFound("订单不存在");
    }
    // 关联课程信息
    Course course = courseService.getCourse(order.getCourseId());
    if (course != null) {
      order.setCourseTitle(course.getTitle());
      order.setCourseCoverUrl(course.getCoverUrl());
    }
    return order;
  }

  public Order payOrder(String orderNo, String payType) {
    Order order = orderRepository.getOrderByNo(orderNo);
    if (order == null) {
      throw ApiException.notFound("订单不存在");
    }
    if (order.getStatus() != 0) {
      throw ApiException.badRequest("订单状态不允许支付");
    }

    int affected = orderRepository.payOrder(orderNo);
    if (affected <= 0) {
      throw ApiException.badRequest("支付失败");
    }

    // 购买量 +1
    orderRepository.incrementBuyCount(order.getCourseId());

    return getOrder(orderNo);
  }

  public void cancelOrder(String orderNo) {
    int affected = orderRepository.cancelOrder(orderNo);
    if (affected <= 0) {
      throw ApiException.badRequest("取消失败，订单可能已支付或已取消");
    }
  }

  public List<Order> listOrders(Integer status, int page, int pageSize) {
    List<Order> orders = orderRepository.listOrders(1L, status, page, pageSize);
    for (Order o : orders) {
      Course course = courseService.getCourse(o.getCourseId());
      if (course != null) {
        o.setCourseTitle(course.getTitle());
        o.setCourseCoverUrl(course.getCoverUrl());
      }
    }
    return orders;
  }

  public int countOrders(Integer status) {
    return orderRepository.countOrders(1L, status);
  }
}
