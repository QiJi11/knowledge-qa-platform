package com.design.stats.controller;

import com.design.common.Result;
import com.design.stats.dto.ByCourseStatsResponse;
import com.design.stats.dto.OverviewStatsResponse;
import com.design.stats.dto.TrendStatsResponse;
import com.design.stats.service.StatsService;
import org.springframework.web.bind.annotation.*;

/**
 * 统计接口控制器
 */
@RestController
@RequestMapping("/api/v1/learners/{learnerId}/stats")
public class StatsController {
    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    /**
     * 总览统计
     * GET /api/v1/learners/{learnerId}/stats/overview
     */
    @GetMapping("/overview")
    public Result<OverviewStatsResponse> getOverviewStats(
            @PathVariable Long learnerId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {

        OverviewStatsResponse stats = statsService.getOverviewStats(learnerId, from, to);
        return Result.ok(stats);
    }

    /**
     * 按课程聚合统计
     * GET /api/v1/learners/{learnerId}/stats/by-course
     */
    @GetMapping("/by-course")
    public Result<ByCourseStatsResponse> getStatsByCourse(
            @PathVariable Long learnerId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false, defaultValue = "totalTasks") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String order) {

        ByCourseStatsResponse stats = statsService.getStatsByCourse(learnerId, from, to, sortBy, order);
        return Result.ok(stats);
    }

    /**
     * 趋势统计
     * GET /api/v1/learners/{learnerId}/stats/trend
     */
    @GetMapping("/trend")
    public Result<TrendStatsResponse> getTrendStats(
            @PathVariable Long learnerId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false, defaultValue = "day") String granularity) {

        TrendStatsResponse stats = statsService.getTrendStats(learnerId, from, to, granularity);
        return Result.ok(stats);
    }
}
