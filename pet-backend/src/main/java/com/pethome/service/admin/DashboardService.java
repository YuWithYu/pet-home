package com.pethome.service.admin;

import java.util.Map;

/**
 * 管理后台仪表盘统计。
 *
 * 应用服务边界：Controller 只负责协议转换，聚合查询与缓存策略属于本层。
 */
public interface DashboardService {

    /**
     * 获取仪表盘统计数据（结果带 60 秒本地缓存）
     */
    Map<String, Object> getStatistics();
}
