package com.design.course.dto;

import com.design.course.entity.Course;
import java.util.List;

/**
 * 课程列表分页响应
 */
public class CourseListResponse {
    private List<Course> items;
    private int page;
    private int pageSize;
    private long total;

    public CourseListResponse() {
    }

    public CourseListResponse(List<Course> items, int page, int pageSize, long total) {
        this.items = items;
        this.page = page;
        this.pageSize = pageSize;
        this.total = total;
    }

    // Getters and Setters
    public List<Course> getItems() {
        return items;
    }

    public void setItems(List<Course> items) {
        this.items = items;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }
}
