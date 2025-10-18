package com.design.stats.service;

import com.design.stats.dto.ByCourseStatsResponse;
import com.design.stats.dto.OverviewStatsResponse;
import com.design.stats.dto.TrendStatsResponse;
import com.design.stats.repository.StatsRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 统计服务
 */
@Service
public class StatsService {
    private final StatsRepository statsRepo;

    public StatsService(StatsRepository statsRepo) {
        this.statsRepo = statsRepo;
    }

    private static final class ResolvedRange {
        final LocalDate from;
        final LocalDate to;

        private ResolvedRange(LocalDate from, LocalDate to) {
            this.from = from;
            this.to = to;
        }
    }

    private static ResolvedRange resolveRange(String fromStr, String toStr) {
        LocalDate to = (toStr != null && !toStr.isBlank())
            ? LocalDate.parse(toStr)
            : LocalDate.now();

        LocalDate from = (fromStr != null && !fromStr.isBlank())
            ? LocalDate.parse(fromStr)
            : to.minusDays(6);

        return new ResolvedRange(from, to);
    }

    /**
     * 获取总览统计
     */
    public OverviewStatsResponse getOverviewStats(long learnerId, String fromStr, String toStr) {
        ResolvedRange r = resolveRange(fromStr, toStr);
        return getOverviewStatsCached(learnerId, r.from.toString(), r.to.toString());
    }

    @Cacheable(cacheNames = "stats:overview", key = "#learnerId + ':' + #from + ':' + #to")
    public OverviewStatsResponse getOverviewStatsCached(long learnerId, String from, String to) {
        LocalDate fromDateLocal = LocalDate.parse(from);
        LocalDate toDateLocal = LocalDate.parse(to);

        Date fromDate = Date.valueOf(fromDateLocal);
        // 修复：to 需要加1天作为排他上界，避免 to 当天的数据被过滤
        Date toDate = Date.valueOf(toDateLocal.plusDays(1));

        OverviewStatsResponse.Summary summary = statsRepo.getOverviewStats(learnerId, fromDate, toDate);
        Map<String, Integer> byStatus = statsRepo.getStatsByStatus(learnerId, fromDate, toDate);
        Map<String, Integer> byPriority = statsRepo.getStatsByPriority(learnerId, fromDate, toDate);

        OverviewStatsResponse response = new OverviewStatsResponse();
        response.setDateRange(new OverviewStatsResponse.DateRange(from, to));
        response.setSummary(summary);
        response.setByStatus(byStatus);
        response.setByPriority(byPriority);
        return response;
    }

    /**
     * 获取按课程聚合统计
     */
    public ByCourseStatsResponse getStatsByCourse(
            long learnerId, String fromStr, String toStr, String sortBy, String order) {
        ResolvedRange r = resolveRange(fromStr, toStr);
        String resolvedSortBy = (sortBy == null || sortBy.isBlank()) ? "totalTasks" : sortBy;
        String resolvedOrder = (order == null || order.isBlank()) ? "desc" : order;
        return getStatsByCourseCached(
            learnerId,
            r.from.toString(),
            r.to.toString(),
            resolvedSortBy,
            resolvedOrder
        );
    }

    @Cacheable(
        cacheNames = "stats:by-course",
        key = "#learnerId + ':' + #from + ':' + #to + ':' + #sortBy + ':' + #order"
    )
    public ByCourseStatsResponse getStatsByCourseCached(
        long learnerId,
        String from,
        String to,
        String sortBy,
        String order
    ) {
        LocalDate fromLocal = LocalDate.parse(from);
        LocalDate toLocal = LocalDate.parse(to);

        Date fromDate = Date.valueOf(fromLocal);
        Date toDate = Date.valueOf(toLocal.plusDays(1));

        List<ByCourseStatsResponse.CourseStats> courses =
            statsRepo.getStatsByCourse(learnerId, fromDate, toDate, sortBy, order);

        ByCourseStatsResponse response = new ByCourseStatsResponse();
        response.setDateRange(new ByCourseStatsResponse.DateRange(from, to));
        response.setCourses(courses);
        return response;
    }

    /**
     * 获取趋势统计
     */
    public TrendStatsResponse getTrendStats(long learnerId, String fromStr, String toStr, String granularity) {
        ResolvedRange r = resolveRange(fromStr, toStr);
        String resolvedGranularity = (granularity == null || granularity.isBlank()) ? "day" : granularity;
        return getTrendStatsCached(learnerId, r.from.toString(), r.to.toString(), resolvedGranularity);
    }

    @Cacheable(
        cacheNames = "stats:trend",
        key = "#learnerId + ':' + #from + ':' + #to + ':' + #granularity"
    )
    public TrendStatsResponse getTrendStatsCached(
        long learnerId,
        String from,
        String to,
        String granularity
    ) {
        LocalDate fromLocal = LocalDate.parse(from);
        LocalDate toLocal = LocalDate.parse(to);

        Date fromDate = Date.valueOf(fromLocal);
        Date toDate = Date.valueOf(toLocal.plusDays(1));

        Map<String, Integer> createdCounts = statsRepo.getCreatedCountsByDate(learnerId, fromDate, toDate);
        Map<String, Integer> completedCounts = statsRepo.getCompletedCountsByDate(learnerId, fromDate, toDate);

        List<TrendStatsResponse.DataPoint> dataPoints =
            statsRepo.fillDateGaps(createdCounts, completedCounts, fromLocal, toLocal);

        TrendStatsResponse response = new TrendStatsResponse();
        response.setDateRange(new TrendStatsResponse.DateRange(from, to));
        response.setGranularity(granularity);
        response.setDataPoints(dataPoints);
        return response;
    }
}
