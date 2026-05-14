package com.pethome.service;

import java.time.LocalDate;
import java.util.Map;

/**
 * 签到服务接口
 */
public interface SignInService {
    
    /**
     * 获取用户签到数据
     * @param userId 用户ID
     * @return 签到数据（余额、连续签到天数、今日是否可签到、本周签到日历）
     */
    Map<String, Object> getSignInData(Long userId);
    
    /**
     * 执行签到
     * @param userId 用户ID
     * @return 签到结果（获得的积分、连续签到天数、当前余额）
     */
    Map<String, Object> doSignIn(Long userId);
    
    /**
     * 检查今日是否已签到
     * @param userId 用户ID
     * @param date 日期
     * @return 是否已签到
     */
    boolean hasSignedIn(Long userId, LocalDate date);
    
    /**
     * 获取连续签到天数
     * @param userId 用户ID
     * @return 连续签到天数
     */
    int getConsecutiveDays(Long userId);
    
    /**
     * 获取本周签到日历
     * @param userId 用户ID
     * @return 本周签到日历（7个boolean值，表示周一到周日是否签到）
     */
    boolean[] getWeekCalendar(Long userId);
}

