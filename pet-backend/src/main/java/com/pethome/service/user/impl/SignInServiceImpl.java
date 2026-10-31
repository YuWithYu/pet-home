package com.pethome.service.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pethome.entity.SignInRecord;
import com.pethome.entity.User;
import com.pethome.entity.PointsRecord;
import com.pethome.mapper.SignInRecordMapper;
import com.pethome.mapper.UserMapper;
import com.pethome.service.user.SignInService;
import com.pethome.service.admin.TaskService;
import com.pethome.service.user.UserService;
import com.pethome.service.order.PointsRecordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(SignInServiceImpl.class);

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
        Map<String, Object> data = new HashMap<>();

        // 获取用户信息（直接从数据库查询，绕过缓存，确保获取最新数据）
        userService.clearUserCache(userId);
        // 直接从数据库查询，不使用缓存
        User user = userMapper.selectById(userId);
        if (user != null) {
            Integer points = user.getPoints();
            data.put("balance", points != null ? points : 0);
        } else {
            log.warn("用户不存在，用户ID: {}", userId);
            data.put("balance", 0);
        }

        // 获取连续签到天数
        int consecutiveDays = getConsecutiveDays(userId);
        data.put("consecutiveDays", consecutiveDays);

        // 检查今日是否可签到
        boolean hasSignedToday = hasSignedIn(userId, LocalDate.now());
        boolean canSignIn = !hasSignedToday;
        data.put("canSignIn", canSignIn);

        // 获取本周签到日历
        boolean[] calendar = getWeekCalendar(userId);
        data.put("calendar", calendar);

        return data;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
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
            log.info("用户 {} 连续签到满 {} 天，获得7天奖励150g", userId, newConsecutiveDays);
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

            // 更新用户的魅力值和等级
            if (userService != null) {
                userService.updateCharmAndLevel(userId);
            }
        } catch (Exception e) {
            log.error("创建签到积分记录失败", e);
        }
        
        // 更新每日签到任务进度
        try {
            taskService.updateTaskProgress(userId, "daily_signin", 1);
        } catch (Exception e) {
            // 任务进度更新失败不影响签到流程
            log.error("更新任务进度失败", e);
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

        // 查询本周的签到记录
        QueryWrapper<SignInRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.ge("sign_date", monday);
        wrapper.le("sign_date", sunday);
        wrapper.orderByAsc("sign_date");
        List<SignInRecord> records = this.list(wrapper);

        // 构建日历数组（索引0=周一，索引6=周日）
        for (SignInRecord record : records) {
            LocalDate signDate = record.getSignDate();
            int dayIndex = (int) java.time.temporal.ChronoUnit.DAYS.between(monday, signDate);
            if (dayIndex >= 0 && dayIndex < 7) {
                calendar[dayIndex] = true;
            } else {
                log.warn("签到日期索引超出范围: {}", dayIndex);
            }
        }

        return calendar;
    }
}

