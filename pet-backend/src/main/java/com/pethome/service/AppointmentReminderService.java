package com.pethome.service;

/**
 * 预约提醒（服务前 24 小时 / 2 小时推送通知）。
 */
public interface AppointmentReminderService {

    /**
     * 扫描即将开始的三类预约并发送提醒（按预约单 + 提醒类型幂等）。
     *
     * @return 本次实际发送的提醒条数
     */
    int runReminders();
}
