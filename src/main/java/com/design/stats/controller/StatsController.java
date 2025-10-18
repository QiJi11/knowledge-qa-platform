package com.design.stats.controller;

import com.design.todo.api.ApiResponse;
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
    public ApiResponse<OverviewStatsResponse> getOverviewStats(
            @PathVariable Long learnerId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {

        OverviewStatsResponse stats = statsService.getOverviewStats(learnerId, from, to);
        return ApiResponse.ok(stats);
    }

    /**
     * 按课程聚合统计
     * GET /api/v1/learners/{learnerId}/stats/by-course
     */
    @GetMapping("/by-course")
    public ApiResponse<ByCourseStatsResponse> getStatsByCourse(
            @PathVariable Long learnerId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false, defaultValue = "totalTasks") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String order) {

        ByCourseStatsResponse stats = statsService.getStatsByCourse(learnerId, from, to, sortBy, order);
        return ApiResponse.ok(stats);
    }

    /**
     * 趋势统计
     * GET /api/v1/learners/{learnerId}/stats/trend
     */
    @GetMapping("/trend")
    public ApiResponse<TrendStatsResponse> getTrendStats(
            @PathVariable Long learnerId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false, defaultValue = "day") String granularity) {

        TrendStatsResponse stats = statsService.getTrendStats(learnerId, from, to, granularity);
        return ApiResponse.ok(stats);
    }
}
