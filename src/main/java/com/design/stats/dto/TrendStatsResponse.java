package com.design.stats.dto;

import java.util.List;

/**
 * 趋势统计响应
 */
public class TrendStatsResponse {
    private DateRange dateRange;
    private String granularity;
    private List<DataPoint> dataPoints;

    public TrendStatsResponse() {
    }

    // Getters and Setters
    public DateRange getDateRange() {
        return dateRange;
    }

    public void setDateRange(DateRange dateRange) {
        this.dateRange = dateRange;
    }

    public String getGranularity() {
        return granularity;
    }

    public void setGranularity(String granularity) {
        this.granularity = granularity;
    }

    public List<DataPoint> getDataPoints() {
        return dataPoints;
    }

    public void setDataPoints(List<DataPoint> dataPoints) {
        this.dataPoints = dataPoints;
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
     * 数据点
     */
    public static class DataPoint {
        private String date;
        private int createdCount;
        private int completedCount;

        public DataPoint() {
        }

        public DataPoint(String date, int createdCount, int completedCount) {
            this.date = date;
            this.createdCount = createdCount;
            this.completedCount = completedCount;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public int getCreatedCount() {
            return createdCount;
        }

        public void setCreatedCount(int createdCount) {
            this.createdCount = createdCount;
        }

        public int getCompletedCount() {
            return completedCount;
        }

        public void setCompletedCount(int completedCount) {
            this.completedCount = completedCount;
        }
    }
}
