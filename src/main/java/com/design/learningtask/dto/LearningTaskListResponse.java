package com.design.learningtask.dto;

import com.design.learningtask.entity.LearningTask;
import java.util.List;

/**
 * 学习任务列表分页响应
 */
public class LearningTaskListResponse {
    private List<LearningTask> items;
    private int page;
    private int pageSize;
    private long total;

    public LearningTaskListResponse() {
    }

    public LearningTaskListResponse(List<LearningTask> items, int page, int pageSize, long total) {
        this.items = items;
        this.page = page;
        this.pageSize = pageSize;
        this.total = total;
    }

    // Getters and Setters
    public List<LearningTask> getItems() {
        return items;
    }

    public void setItems(List<LearningTask> items) {
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
