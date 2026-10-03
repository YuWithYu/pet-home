package com.pethome.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pethome.common.Result;
import com.pethome.entity.Order;
import com.pethome.mapper.DoorCleaningAppointmentMapper;
import com.pethome.mapper.GroomingAppointmentMapper;
import com.pethome.mapper.HospitalAppointmentMapper;
import com.pethome.mapper.NoticeMapper;
import com.pethome.mapper.OrderMapper;
import com.pethome.mapper.PetMapper;
import com.pethome.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 管理后台仪表盘统计数据
 * 性能设计：
 * 1. 全部指标改为 SQL 聚合查询（GROUP BY / SUM），替代旧的逐日循环 COUNT 与全量加载内存求和，
 *    单次请求由约 60 次数据库往返（含整行加载）降为 13 条聚合 SQL；
 * 2. 营收部分一次范围查询同时覆盖今日/本月/近7天趋势（范围取两者较小起点）；
 * 3. 结果加 60 秒内存缓存，多个管理员同时刷新仪表盘时不再重复压库。
 */
@RestController
@RequestMapping("/api/admin/dashboard")
@Tag(name = "仪表盘统计")
public class DashboardController {

    /** 仪表盘统计缓存时长（毫秒） */
    private static final long CACHE_TTL_MS = 60_000L;

    @Autowired
    private PetMapper petMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired(required = false)
    private NoticeMapper noticeMapper;
    // 无统一 appointment 表，预约按三张表：grooming_appointments、hospital_appointments、door_cleaning_appointment
    @Autowired(required = false)
    private GroomingAppointmentMapper groomingAppointmentMapper;
    @Autowired(required = false)
    private HospitalAppointmentMapper hospitalAppointmentMapper;
    @Autowired(required = false)
    private DoorCleaningAppointmentMapper doorCleaningAppointmentMapper;

    private volatile Map<String, Object> cachedData;
    private volatile long cachedAt = 0L;

    @GetMapping("/statistics")
    @Operation(summary = "获取仪表盘统计数据")
    public Result<Map<String, Object>> getStatistics() {
        Map<String, Object> data = cachedData;
        if (data != null && System.currentTimeMillis() - cachedAt < CACHE_TTL_MS) {
            return Result.success(data);
        }
        synchronized (this) {
            if (cachedData != null && System.currentTimeMillis() - cachedAt < CACHE_TTL_MS) {
                return Result.success(cachedData);
            }
            cachedData = buildStatistics();
            cachedAt = System.currentTimeMillis();
            return Result.success(cachedData);
        }
    }

    private Map<String, Object> buildStatistics() {
        Map<String, Object> data = new HashMap<>();

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDateTime weekStart = today.minusDays(6).atStartOfDay();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.atTime(LocalTime.MAX);
        LocalDateTime monthStart = today.withDayOfMonth(1).atStartOfDay();

        // ===== 宠物统计 =====
        data.put("totalPets", petMapper.selectCount(null));
        Map<String, Long> petCountByDay = countByDay(petMapper, "create_time", weekStart, todayEnd);
        data.put("todayNewPets", petCountByDay.getOrDefault(today.toString(), 0L));
        data.put("yesterdayNewPets", petCountByDay.getOrDefault(yesterday.toString(), 0L));
        List<Map<String, Object>> petTrend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            Map<String, Object> point = new HashMap<>();
            point.put("date", d.toString());
            point.put("count", petCountByDay.getOrDefault(d.toString(), 0L));
            petTrend.add(point);
        }
        data.put("petTrend", petTrend);

        // ===== 订单统计 =====
        data.put("totalOrders", orderMapper.selectCount(null));
        Map<String, Long> orderCountByDay = countByDay(orderMapper, "create_time", weekStart, todayEnd);
        data.put("todayNewOrders", orderCountByDay.getOrDefault(today.toString(), 0L));
        data.put("yesterdayNewOrders", orderCountByDay.getOrDefault(yesterday.toString(), 0L));

        // 订单时段分布（今日每4小时，一条 GROUP BY 替代 6 次循环 COUNT）
        Map<Integer, Long> hourBuckets = countByHourBucket(todayStart, todayEnd);
        List<Integer> orderByHour = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            orderByHour.add(hourBuckets.getOrDefault(i, 0L).intValue());
        }
        data.put("orderByHour", orderByHour);
        // 6 个桶对应 6 个标签（0-4/4-8/8-12/12-16/16-20/20-24），此前多出一个悬空的「24时」
        data.put("orderHourLabels", new String[]{"0时", "4时", "8时", "12时", "16时", "20时"});

        // ===== 用户/通知统计 =====
        long totalUsers = userMapper.selectCount(null);
        data.put("totalUsers", totalUsers);
        data.put("activeUsers", totalUsers);
        long totalNotices = noticeMapper != null ? noticeMapper.selectCount(null) : 0L;
        data.put("totalNotices", totalNotices);
        data.put("noticeCount", totalNotices);

        // 用户活跃趋势（近7天新增用户）
        Map<String, Long> userCountByDay = countByDay(userMapper, "create_time", weekStart, todayEnd);
        String[] weekDays = {"", "周一", "周二", "周三", "周四", "周五", "周六", "周日"};
        List<Map<String, Object>> userTrend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            Map<String, Object> point = new HashMap<>();
            point.put("day", weekDays[d.getDayOfWeek().getValue()]);
            // 补充标准日期（yyyy-MM-dd）：前端按日期精确取"今日/昨日"，星期名仅作展示标签
            point.put("date", d.toString());
            point.put("count", userCountByDay.getOrDefault(d.toString(), 0L));
            userTrend.add(point);
        }
        data.put("userActivityTrend", userTrend);

        // 订单状态分布（一条 GROUP BY 替代 5 次循环 COUNT）
        data.put("orderStatusCounts", orderStatusCounts());

        // ===== 营收统计 =====
        // 一次范围查询同时覆盖：今日营收、本月营收、近7天趋势（起点取月初与7天前的较小值）
        LocalDateTime revenueStart = monthStart.isBefore(weekStart) ? monthStart : weekStart;
        Map<String, BigDecimal> revenueByDay = new TreeMap<>();
        mergeSum(revenueByDay, mallRevenueByDay(revenueStart, todayEnd));
        mergeSum(revenueByDay, serviceRevenueByDay(revenueStart, todayEnd));

        BigDecimal todayRevenue = revenueByDay.getOrDefault(today.toString(), BigDecimal.ZERO);
        data.put("todayRevenue", todayRevenue.setScale(2, RoundingMode.HALF_UP));

        BigDecimal monthlyRevenue = BigDecimal.ZERO;
        for (Map.Entry<String, BigDecimal> e : revenueByDay.entrySet()) {
            // key 为 yyyy-MM-dd 格式，字典序即时间序
            if (e.getKey().compareTo(monthStart.toLocalDate().toString()) >= 0) {
                monthlyRevenue = monthlyRevenue.add(e.getValue());
            }
        }
        data.put("monthlyRevenue", monthlyRevenue.setScale(2, RoundingMode.HALF_UP));

        List<Map<String, Object>> revenueTrend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            Map<String, Object> point = new HashMap<>();
            point.put("date", d.toString());
            point.put("revenue", revenueByDay.getOrDefault(d.toString(), BigDecimal.ZERO).doubleValue());
            revenueTrend.add(point);
        }
        data.put("revenueTrend", revenueTrend);

        return data;
    }

    /**
     * 按天分组 COUNT：一条 GROUP BY SQL 替代逐日循环 COUNT。
     * key 为 yyyy-MM-dd，value 为当日条数。
     */
    private <T> Map<String, Long> countByDay(BaseMapper<T> mapper, String timeColumn, LocalDateTime start, LocalDateTime end) {
        QueryWrapper<T> wrapper = new QueryWrapper<>();
        wrapper.select("DATE(" + timeColumn + ") AS stat_date", "COUNT(*) AS cnt")
                .ge(timeColumn, start)
                .le(timeColumn, end)
                .groupBy("DATE(" + timeColumn + ")");
        return toLongMap(mapper.selectMaps(wrapper));
    }

    /** 今日订单按 4 小时桶分组（0/4/8/12/16/20 点起） */
    private Map<Integer, Long> countByHourBucket(LocalDateTime start, LocalDateTime end) {
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.select("FLOOR(HOUR(create_time) / 4) AS bucket", "COUNT(*) AS cnt")
                .ge("create_time", start)
                .le("create_time", end)
                .groupBy("FLOOR(HOUR(create_time) / 4)");
        List<Map<String, Object>> rows = orderMapper.selectMaps(wrapper);
        Map<Integer, Long> result = new HashMap<>();
        if (rows != null) {
            for (Map<String, Object> row : rows) {
                Object bucket = row.get("bucket");
                Object cnt = row.get("cnt");
                if (bucket instanceof Number && cnt instanceof Number) {
                    result.put(((Number) bucket).intValue(), ((Number) cnt).longValue());
                }
            }
        }
        return result;
    }

    /** 订单状态分布：一条 GROUP BY 替代按状态逐个 COUNT（0=待付款, 1=已付款, 2=已发货, 3=已完成, -1=已取消） */
    private List<Map<String, Object>> orderStatusCounts() {
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.select("status", "COUNT(*) AS cnt").groupBy("status");
        List<Map<String, Object>> rows = orderMapper.selectMaps(wrapper);
        Map<Integer, Integer> statusMap = new HashMap<>();
        if (rows != null) {
            for (Map<String, Object> row : rows) {
                Object status = row.get("status");
                Object cnt = row.get("cnt");
                if (status instanceof Number && cnt instanceof Number) {
                    statusMap.put(((Number) status).intValue(), ((Number) cnt).intValue());
                }
            }
        }
        List<Map<String, Object>> orderStatusCounts = new ArrayList<>();
        int[] statuses = {0, 1, 2, 3, -1};
        String[] labels = {"待付款", "已付款", "已发货", "已完成", "已取消"};
        for (int i = 0; i < statuses.length; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", labels[i]);
            item.put("value", statusMap.getOrDefault(statuses[i], 0));
            item.put("status", statuses[i]);
            orderStatusCounts.add(item);
        }
        return orderStatusCounts;
    }

    /** 商城订单营收（已支付）：SQL SUM 替代全量加载内存求和，按天分组 */
    private Map<String, BigDecimal> mallRevenueByDay(LocalDateTime start, LocalDateTime end) {
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.select("DATE(create_time) AS stat_date", "IFNULL(SUM(total_amount), 0) AS total")
                .ge("create_time", start)
                .le("create_time", end)
                .and(w -> w.eq("payment_status", 1).or().ge("status", 1))
                .groupBy("DATE(create_time)");
        return toBigDecimalMap(orderMapper.selectMaps(wrapper));
    }

    /** 全部服务部门营收（已确认/已完成的预约）：三张预约表按天 SUM 后合并 */
    private Map<String, BigDecimal> serviceRevenueByDay(LocalDateTime start, LocalDateTime end) {
        Map<String, BigDecimal> result = new HashMap<>();
        if (groomingAppointmentMapper != null) {
            mergeSum(result, sumPriceByDay(groomingAppointmentMapper, start, end));
        }
        if (hospitalAppointmentMapper != null) {
            mergeSum(result, sumPriceByDay(hospitalAppointmentMapper, start, end));
        }
        if (doorCleaningAppointmentMapper != null) {
            mergeSum(result, sumPriceByDay(doorCleaningAppointmentMapper, start, end));
        }
        return result;
    }

    /** 服务预约表按天 SUM(price)：SQL 聚合替代全量加载内存求和 */
    private <T> Map<String, BigDecimal> sumPriceByDay(BaseMapper<T> mapper, LocalDateTime start, LocalDateTime end) {
        QueryWrapper<T> wrapper = new QueryWrapper<>();
        wrapper.select("DATE(create_time) AS stat_date", "IFNULL(SUM(price), 0) AS total")
                .ge("create_time", start)
                .le("create_time", end)
                .in("status", "confirmed", "completed")
                .groupBy("DATE(create_time)");
        return toBigDecimalMap(mapper.selectMaps(wrapper));
    }

    private Map<String, Long> toLongMap(List<Map<String, Object>> rows) {
        Map<String, Long> result = new HashMap<>();
        if (rows != null) {
            for (Map<String, Object> row : rows) {
                Object date = row.get("stat_date");
                Object cnt = row.get("cnt");
                if (date != null && cnt instanceof Number) {
                    result.put(date.toString(), ((Number) cnt).longValue());
                }
            }
        }
        return result;
    }

    private Map<String, BigDecimal> toBigDecimalMap(List<Map<String, Object>> rows) {
        Map<String, BigDecimal> result = new HashMap<>();
        if (rows != null) {
            for (Map<String, Object> row : rows) {
                Object date = row.get("stat_date");
                Object total = row.get("total");
                if (date != null && total instanceof Number) {
                    result.put(date.toString(), new BigDecimal(((Number) total).toString()));
                }
            }
        }
        return result;
    }

    private void mergeSum(Map<String, BigDecimal> target, Map<String, BigDecimal> source) {
        for (Map.Entry<String, BigDecimal> e : source.entrySet()) {
            target.merge(e.getKey(), e.getValue(), BigDecimal::add);
        }
    }
}
