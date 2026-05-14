package com.pethome.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pethome.entity.SignInRecord;
import com.pethome.entity.User;
import com.pethome.entity.PointsRecord;
import com.pethome.mapper.SignInRecordMapper;
import com.pethome.mapper.UserMapper;
import com.pethome.service.SignInService;
import com.pethome.service.TaskService;
import com.pethome.service.UserService;
import com.pethome.service.PointsRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 签到服务实现类
 */
@Service
public class SignInServiceImpl extends ServiceImpl<SignInRecordMapper, SignInRecord> implements SignInService {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    @Lazy
    private TaskService taskService;
    
    @Autowired
    private PointsRecordService pointsRecordService;
    
    @Override
    public Map<String, Object> getSignInData(Long userId) {
        System.out.println("========== SignInService.getSignInData ==========");
        System.out.println("用户ID: " + userId);
        
        Map<String, Object> data = new HashMap<>();
        
        // 获取用户信息（直接从数据库查询，绕过缓存，确保获取最新数据）
        userService.clearUserCache(userId);
        // 直接从数据库查询，不使用缓存
        User user = userMapper.selectById(userId);
        if (user != null) {
            Integer points = user.getPoints();
            System.out.println("用户信息 - ID: " + user.getId() + ", 用户名: " + user.getUsername() + ", 积分: " + points);
            data.put("balance", points != null ? points : 0);
        } else {
            System.out.println("警告: 用户不存在，用户ID: " + userId);
            data.put("balance", 0);
        }
        
        // 获取连续签到天数
        int consecutiveDays = getConsecutiveDays(userId);
        System.out.println("连续签到天数: " + consecutiveDays);
        data.put("consecutiveDays", consecutiveDays);
        
        // 检查今日是否可签到
        boolean hasSignedToday = hasSignedIn(userId, LocalDate.now());
        boolean canSignIn = !hasSignedToday;
        System.out.println("今日是否已签到: " + hasSignedToday + ", 可签到: " + canSignIn);
        data.put("canSignIn", canSignIn);
        
        // 获取本周签到日历
        boolean[] calendar = getWeekCalendar(userId);
        System.out.println("本周签到日历: " + java.util.Arrays.toString(calendar));
        data.put("calendar", calendar);
        
        System.out.println("返回的签到数据: " + data);
        System.out.println("====================================");
        
        return data;
    }
    
    @Override
    @Transactional
    public Map<String, Object> doSignIn(Long userId) {
        LocalDate today = LocalDate.now();
        
        // 检查今日是否已签到
        if (hasSignedIn(userId, today)) {
            throw new RuntimeException("今日已签到，请勿重复签到");
        }
        
        // 获取当前连续签到天数（签到前的天数）
        int currentConsecutiveDays = getConsecutiveDays(userId);
        
        // 计算本次签到获得的积分（连续签到天数越多，积分越多）
        int basePoints = 10;
        int bonusPoints = Math.min(currentConsecutiveDays / 7, 5) * 5; // 每连续7天额外奖励5积分，最多25积分
        int totalPoints = basePoints + bonusPoints;
        
        // 签到后的连续签到天数
        int newConsecutiveDays = currentConsecutiveDays + 1;
        
        // 检查是否达到7天的倍数，如果是则额外奖励150g
        int weekReward = 0;
        if (newConsecutiveDays > 0 && newConsecutiveDays % 7 == 0) {
            // 连续签到满7天（或7的倍数），额外奖励150g
            weekReward = 150;
            totalPoints += weekReward;
            System.out.println("用户 " + userId + " 连续签到满 " + newConsecutiveDays + " 天，获得7天奖励150g");
        }
        
        // 创建签到记录
        SignInRecord record = new SignInRecord();
        record.setUserId(userId);
        record.setSignDate(today);
        record.setPoints(totalPoints);
        record.setConsecutiveDays(newConsecutiveDays);
        record.setCreateTime(LocalDateTime.now());
        this.save(record);
        
        // 更新用户积分（直接从数据库获取，绕过缓存）
        userService.clearUserCache(userId);
        User user = userMapper.selectById(userId);
        if (user != null) {
            int currentPoints = user.getPoints() != null ? user.getPoints() : 0;
            user.setPoints(currentPoints + totalPoints);
            userService.updateUser(user);
            // 清除用户缓存，确保下次查询获取最新数据
            userService.clearUserCache(userId);
            // 重新从数据库获取最新积分
            user = userMapper.selectById(userId);
            System.out.println("签到后用户积分: " + (user != null ? user.getPoints() : 0));
        }
        
        // 创建积分记录
        try {
            PointsRecord pointsRecord = new PointsRecord();
            pointsRecord.setUserId(userId);
            pointsRecord.setType("earn");
            pointsRecord.setPoints(totalPoints);
            pointsRecord.setDescription("每日签到（连续" + newConsecutiveDays + "天）");
            pointsRecord.setSource("signin");
            pointsRecord.setCreateTime(LocalDateTime.now());
            pointsRecordService.addRecord(pointsRecord);
            System.out.println("创建签到积分记录成功，积分: " + totalPoints);
            
            // 更新用户的魅力值和等级
            if (userService != null) {
                userService.updateCharmAndLevel(userId);
            }
        } catch (Exception e) {
            System.err.println("创建签到积分记录失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        // 更新每日签到任务进度
        try {
            taskService.updateTaskProgress(userId, "daily_signin", 1);
        } catch (Exception e) {
            // 任务进度更新失败不影响签到流程
            System.err.println("更新任务进度失败: " + e.getMessage());
        }
        
        // 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("points", totalPoints);
        result.put("consecutiveDays", newConsecutiveDays);
        result.put("balance", user != null ? user.getPoints() : totalPoints);
        result.put("weekReward", weekReward); // 7天奖励（如果有）
        
        return result;
    }
    
    @Override
    public boolean hasSignedIn(Long userId, LocalDate date) {
        QueryWrapper<SignInRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("sign_date", date);
        return this.count(wrapper) > 0;
    }
    
    @Override
    public int getConsecutiveDays(Long userId) {
        LocalDate today = LocalDate.now();
        int consecutiveDays = 0;
        
        // 从今天往前查找连续签到的天数
        // 如果今天已签到，从今天开始计算；如果今天没签到，从昨天开始计算
        LocalDate startDate = today;
        if (!hasSignedIn(userId, today)) {
            startDate = today.minusDays(1);
        }
        
        // 从开始日期往前查找连续签到的天数
        for (int i = 0; i < 365; i++) { // 最多查找365天
            LocalDate checkDate = startDate.minusDays(i);
            if (hasSignedIn(userId, checkDate)) {
                consecutiveDays++;
            } else {
                // 如果遇到未签到的日期，停止计算
                break;
            }
        }
        
        return consecutiveDays;
    }
    
    @Override
    public boolean[] getWeekCalendar(Long userId) {
        boolean[] calendar = new boolean[7];
        LocalDate today = LocalDate.now();
        
        // 获取本周一的日期
        DayOfWeek dayOfWeek = today.getDayOfWeek();
        int daysFromMonday = dayOfWeek.getValue() - 1; // Monday = 1, Sunday = 7
        LocalDate monday = today.minusDays(daysFromMonday);
        LocalDate sunday = monday.plusDays(6);
        
        System.out.println("========== 获取本周签到日历 ==========");
        System.out.println("用户ID: " + userId);
        System.out.println("今天日期: " + today + " (" + dayOfWeek + ")");
        System.out.println("本周一: " + monday);
        System.out.println("本周日: " + sunday);
        
        // 查询本周的签到记录
        QueryWrapper<SignInRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.ge("sign_date", monday);
        wrapper.le("sign_date", sunday);
        wrapper.orderByAsc("sign_date");
        List<SignInRecord> records = this.list(wrapper);
        
        System.out.println("查询到的签到记录数量: " + records.size());
        for (SignInRecord record : records) {
            System.out.println("  - 签到日期: " + record.getSignDate() + ", 积分: " + record.getPoints() + ", 连续天数: " + record.getConsecutiveDays());
        }
        
        // 构建日历数组（索引0=周一，索引6=周日）
        for (SignInRecord record : records) {
            LocalDate signDate = record.getSignDate();
            int dayIndex = (int) java.time.temporal.ChronoUnit.DAYS.between(monday, signDate);
            System.out.println("签到日期: " + signDate + ", 距离周一的天数: " + dayIndex + ", 索引: " + dayIndex);
            if (dayIndex >= 0 && dayIndex < 7) {
                calendar[dayIndex] = true;
            } else {
                System.out.println("警告: 签到日期索引超出范围: " + dayIndex);
            }
        }
        
        System.out.println("最终日历数组: " + java.util.Arrays.toString(calendar));
        System.out.println("====================================");
        
        return calendar;
    }
}

