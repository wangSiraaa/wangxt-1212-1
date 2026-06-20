package com.airport.deicing.controller;

import com.airport.deicing.common.Result;
import com.airport.deicing.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "看板管理", description = "实时看板数据接口")
@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "获取完整看板数据")
    @GetMapping("/data")
    public Result<Map<String, Object>> getDashboardData() {
        return Result.success(dashboardService.getDashboardData());
    }

    @Operation(summary = "获取看板统计数据")
    @GetMapping("/stats")
    public Result<Map<String, Object>> getDashboardStats() {
        return Result.success(dashboardService.getDashboardStats());
    }

    @Operation(summary = "获取实时状态")
    @GetMapping("/realtime")
    public Result<Map<String, Object>> getRealtimeStatus() {
        return Result.success(dashboardService.getRealtimeStatus());
    }

    @Operation(summary = "刷新看板缓存")
    @PostMapping("/refresh")
    public Result<Void> refreshDashboard() {
        dashboardService.refreshDashboardCache();
        return Result.success();
    }
}
