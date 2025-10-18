package com.design.stats.dto;

import java.util.Map;

/**
 * 总览统计响应
 */
public class OverviewStatsResponse {
    private DateRange dateRange;
    private Summary summary;
    private Map<String, Integer> byStatus;
    private Map<String, Integer> byPriority;

    public OverviewStatsResponse() {
    }

    // Getters and Setters
    public DateRange getDateRange() {
        return dateRange;
    }

    public void setDateRange(DateRange dateRange) {
        this.dateRange = dateRange;
    }

    public Summary getSummary() {
        return summary;
    }

    public void setSummary(Summary summary) {
        this.summary = summary;
    }

    public Map<String, Integer> getByStatus() {
        return byStatus;
    }

    public void setByStatus(Map<String, Integer> byStatus) {
        this.byStatus = byStatus;
    }

    public Map<String, Integer> getByPriority() {
        return byPriority;
    }

    public void setByPriority(Map<String, Integer> byPriority) {
        this.byPriority = byPriority;
    }

    /**
     * 日期范围
     */
    public static class DateRange {
        private String from;
        private String to;

        public DateRange() {
        }

        public DateRange(String from, String to) {
            this.from = from;
            this.to = to;
        }

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getTo() {
            return to;
        }

        public void setTo(String to) {
            this.to = to;
        }
    }

    /**
     * 汇总统计
     */
    public static class Summary {
        private int totalTasks;
        private int completedTasks;
        private double completionRate;
        private int overdueTasks;
        private int inProgressTasks;

        public Summary() {
        }

        public int getTotalTasks() {
            return totalTasks;
        }

        public void setTotalTasks(int totalTasks) {
            this.totalTasks = totalTasks;
        }

        public int getCompletedTasks() {
            return completedTasks;
        }

        public void setCompletedTasks(int completedTasks) {
            this.completedTasks = completedTasks;
        }

        public double getCompletionRate() {
            return completionRate;
        }

        public void setCompletionRate(double completionRate) {
            this.completionRate = completionRate;
        }

        public int getOverdueTasks() {
            return overdueTasks;
        }

        public void setOverdueTasks(int overdueTasks) {
            this.overdueTasks = overdueTasks;
        }

        public int getInProgressTasks() {
            return inProgressTasks;
        }

        public void setInProgressTasks(int inProgressTasks) {
            this.inProgressTasks = inProgressTasks;
        }
    }
}
