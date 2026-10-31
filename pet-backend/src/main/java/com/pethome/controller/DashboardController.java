package com.pethome.controller;

import com.pethome.common.Result;
import com.pethome.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 管理后台仪表盘统计数据。
 *
 * 仅承担协议转换与路由；聚合查询、缓存策略由 {@link DashboardService} 实现。
 */
@RestController
@RequestMapping("/api/admin/dashboard")
@Tag(name = "仪表盘统计")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/statistics")
    @Operation(summary = "获取仪表盘统计数据")
    public Result<Map<String, Object>> getStatistics() {
        return Result.success(dashboardService.getStatistics());
    }
}
