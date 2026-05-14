package com.pethome.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pethome.common.Result;
import com.pethome.entity.*;
import com.pethome.mapper.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * 管理后台仪表盘统计数据
 */
@RestController
@RequestMapping("/api/admin/dashboard")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Api(tags = "仪表盘统计")
public class DashboardController {

    @Autowired
    private PetMapper petMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired(required = false)
    private com.pethome.mapper.NoticeMapper noticeMapper;
    // 无统一 appointment 表，预约按三张表：grooming_appointments、hospital_appointments、door_cleaning_appointment + bookings
    @Autowired(required = false)
    private GroomingAppointmentMapper groomingAppointmentMapper;
    @Autowired(required = false)
    private HospitalAppointmentMapper hospitalAppointmentMapper;
    @Autowired(required = false)
    private DoorCleaningAppointmentMapper doorCleaningAppointmentMapper;
    // 仅用三张预约表统计营收

    @GetMapping("/statistics")
    @ApiOperation("获取仪表盘统计数据")
    public Result<Map<String, Object>> getStatistics() {
        Map<String, Object> data = new HashMap<>();

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.atTime(LocalTime.MAX);
        LocalDateTime yesterdayStart = yesterday.atStartOfDay();
        LocalDateTime yesterdayEnd = yesterday.atTime(LocalTime.MAX);
        LocalDateTime monthStart = today.withDayOfMonth(1).atStartOfDay();

        // 总宠物数
        long totalPets = petMapper.selectCount(null);
        data.put("totalPets", totalPets);

        // 今日新增宠物
        QueryWrapper<Pet> petTodayWrapper = new QueryWrapper<>();
        petTodayWrapper.ge("create_time", todayStart).le("create_time", todayEnd);
        long todayNewPets = petMapper.selectCount(petTodayWrapper);
        data.put("todayNewPets", todayNewPets);

        // 昨日新增宠物
        QueryWrapper<Pet> petYesterdayWrapper = new QueryWrapper<>();
        petYesterdayWrapper.ge("create_time", yesterdayStart).le("create_time", yesterdayEnd);
        long yesterdayNewPets = petMapper.selectCount(petYesterdayWrapper);
        data.put("yesterdayNewPets", yesterdayNewPets);

        // 宠物注册趋势（近7天）
        List<Map<String, Object>> petTrend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            LocalDateTime start = d.atStartOfDay();
            LocalDateTime end = d.atTime(LocalTime.MAX);
            QueryWrapper<Pet> w = new QueryWrapper<>();
            w.ge("create_time", start).le("create_time", end);
            long count = petMapper.selectCount(w);
            Map<String, Object> point = new HashMap<>();
            point.put("date", d.toString());
            point.put("count", count);
            petTrend.add(point);
        }
        data.put("petTrend", petTrend);

        // 总订单数
        long totalOrders = orderMapper.selectCount(null);
        data.put("totalOrders", totalOrders);

        // 今日新增订单
        QueryWrapper<Order> orderTodayWrapper = new QueryWrapper<>();
        orderTodayWrapper.ge("create_time", todayStart).le("create_time", todayEnd);
        long todayNewOrders = orderMapper.selectCount(orderTodayWrapper);
        data.put("todayNewOrders", todayNewOrders);

        // 昨日新增订单
        QueryWrapper<Order> orderYesterdayWrapper = new QueryWrapper<>();
        orderYesterdayWrapper.ge("create_time", yesterdayStart).le("create_time", yesterdayEnd);
        long yesterdayNewOrders = orderMapper.selectCount(orderYesterdayWrapper);
        data.put("yesterdayNewOrders", yesterdayNewOrders);

        // 订单时段分布（今日每4小时）
        List<Integer> orderByHour = new ArrayList<>();
        String[] hours = {"0时", "4时", "8时", "12时", "16时", "20时", "24时"};
        for (int i = 0; i < 6; i++) {
            LocalDateTime hStart = today.atTime(i * 4, 0);
            LocalDateTime hEnd = (i < 5) ? today.atTime((i + 1) * 4, 0).minusNanos(1) : todayEnd;
            QueryWrapper<Order> w = new QueryWrapper<>();
            w.ge("create_time", hStart).le("create_time", hEnd);
            Long cnt = orderMapper.selectCount(w);
            orderByHour.add(cnt != null ? cnt.intValue() : 0);
        }
        data.put("orderByHour", orderByHour);
        data.put("orderHourLabels", hours);

        // 总用户数（作为活跃用户近似）
        long totalUsers = userMapper.selectCount(null);
        data.put("totalUsers", totalUsers);
        data.put("activeUsers", totalUsers);

        // 通知/公告总数
        long totalNotices = 0;
        if (noticeMapper != null) {
            totalNotices = noticeMapper.selectCount(null);
        }
        data.put("totalNotices", totalNotices);
        data.put("noticeCount", totalNotices);

        // 订单状态分布（0=待付款, 1=已付款, 2=已发货, 3=已完成, -1=已取消）
        List<Map<String, Object>> orderStatusCounts = new ArrayList<>();
        for (int status : new int[] { 0, 1, 2, 3, -1 }) {
            QueryWrapper<Order> sw = new QueryWrapper<>();
            sw.eq("status", status);
            long cnt = orderMapper.selectCount(sw);
            String label = status == 0 ? "待付款" : status == 1 ? "已付款" : status == 2 ? "已发货" : status == 3 ? "已完成" : "已取消";
            Map<String, Object> item = new HashMap<>();
            item.put("name", label);
            item.put("value", (int) cnt);
            item.put("status", status);
            orderStatusCounts.add(item);
        }
        data.put("orderStatusCounts", orderStatusCounts);

        // 用户活跃趋势（近7天新增用户）
        List<Map<String, Object>> userTrend = new ArrayList<>();
        String[] weekDays = {"", "周一", "周二", "周三", "周四", "周五", "周六", "周日"};
        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            LocalDateTime start = d.atStartOfDay();
            LocalDateTime end = d.atTime(LocalTime.MAX);
            QueryWrapper<User> w = new QueryWrapper<>();
            w.ge("create_time", Timestamp.valueOf(start)).le("create_time", Timestamp.valueOf(end));
            long count = userMapper.selectCount(w);
            Map<String, Object> point = new HashMap<>();
            point.put("day", weekDays[d.getDayOfWeek().getValue()]);
            point.put("count", count);
            userTrend.add(point);
        }
        data.put("userActivityTrend", userTrend);

        // 今日营收 = 商城订单 + 全部服务预约（统一预约、洗护、医院、上门铲屎、bookings）
        BigDecimal todayRevenue = sumMallRevenue(todayStart, todayEnd);
        todayRevenue = todayRevenue.add(sumServiceRevenue(todayStart, todayEnd));
        data.put("todayRevenue", todayRevenue.setScale(2, RoundingMode.HALF_UP));

        // 本月营收
        BigDecimal monthlyRevenue = sumMallRevenue(monthStart, todayEnd);
        monthlyRevenue = monthlyRevenue.add(sumServiceRevenue(monthStart, todayEnd));
        data.put("monthlyRevenue", monthlyRevenue.setScale(2, RoundingMode.HALF_UP));

        // 营收趋势（近7天每日营收：商城+服务）
        List<Map<String, Object>> revenueTrend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            LocalDateTime start = d.atStartOfDay();
            LocalDateTime end = d.atTime(LocalTime.MAX);
            BigDecimal dayRev = sumMallRevenue(start, end).add(sumServiceRevenue(start, end));
            Map<String, Object> point = new HashMap<>();
            point.put("date", d.toString());
            point.put("revenue", dayRev.doubleValue());
            revenueTrend.add(point);
        }
        data.put("revenueTrend", revenueTrend);

        return Result.success(data);
    }

    /** 商城订单营收（已支付） */
    private BigDecimal sumMallRevenue(LocalDateTime start, LocalDateTime end) {
        List<Order> orders = orderMapper.selectList(
            new QueryWrapper<Order>()
                .ge("create_time", start).le("create_time", end)
                .and(w -> w.eq("payment_status", 1).or().ge("status", 1))
        );
        BigDecimal sum = BigDecimal.ZERO;
        for (Order o : orders) {
            if (o.getTotalAmount() != null && (o.getPaymentStatus() != null && o.getPaymentStatus() == 1 || o.getStatus() != null && o.getStatus() >= 1)) {
                sum = sum.add(o.getTotalAmount());
            }
        }
        return sum;
    }

    /** 全部服务部门营收（已确认/已完成的预约）：仅统计三张预约表，不查统一 appointment 表 */
    private BigDecimal sumServiceRevenue(LocalDateTime start, LocalDateTime end) {
        BigDecimal sum = BigDecimal.ZERO;

        if (groomingAppointmentMapper != null) {
            List<GroomingAppointment> list = groomingAppointmentMapper.selectList(
                new QueryWrapper<GroomingAppointment>()
                    .ge("create_time", start).le("create_time", end)
                    .in("status", "confirmed", "completed")
            );
            for (GroomingAppointment g : list) {
                if (g.getPrice() != null) sum = sum.add(g.getPrice());
            }
        }
        if (hospitalAppointmentMapper != null) {
            List<HospitalAppointment> list = hospitalAppointmentMapper.selectList(
                new QueryWrapper<HospitalAppointment>()
                    .ge("create_time", start).le("create_time", end)
                    .in("status", "confirmed", "completed")
            );
            for (HospitalAppointment h : list) {
                if (h.getPrice() != null) sum = sum.add(h.getPrice());
            }
        }
        if (doorCleaningAppointmentMapper != null) {
            List<DoorCleaningAppointment> list = doorCleaningAppointmentMapper.selectList(
                new QueryWrapper<DoorCleaningAppointment>()
                    .ge("create_time", start).le("create_time", end)
                    .in("status", "confirmed", "completed")
            );
            for (DoorCleaningAppointment d : list) {
                if (d.getPrice() != null) sum = sum.add(d.getPrice());
            }
        }
        return sum;
    }
}
