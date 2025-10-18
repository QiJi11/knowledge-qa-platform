package com.design.stats.dto;

import java.util.List;

/**
 * 按课程聚合统计响应
 */
public class ByCourseStatsResponse {
    private DateRange dateRange;
    private List<CourseStats> courses;

    public ByCourseStatsResponse() {
    }

    // Getters and Setters
    public DateRange getDateRange() {
        return dateRange;
    }

    public void setDateRange(DateRange dateRange) {
        this.dateRange = dateRange;
    }

    public List<CourseStats> getCourses() {
        return courses;
    }

    public void setCourses(List<CourseStats> courses) {
        this.courses = courses;
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
     * 课程统计
     */
    public static class CourseStats {
        private Long courseId;
        private String courseTitle;
        private String category;
        private int totalTasks;
        private int completedTasks;
        private double completionRate;
        private int overdueTasks;
        private Double avgCompletionDays;

        public CourseStats() {
        }

        public Long getCourseId() {
            return courseId;
        }

        public void setCourseId(Long courseId) {
            this.courseId = courseId;
        }

        public String getCourseTitle() {
            return courseTitle;
        }

        public void setCourseTitle(String courseTitle) {
            this.courseTitle = courseTitle;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
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

        public Double getAvgCompletionDays() {
            return avgCompletionDays;
        }

        public void setAvgCompletionDays(Double avgCompletionDays) {
            this.avgCompletionDays = avgCompletionDays;
        }
    }
}
