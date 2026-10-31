package com.pethome.task;

import com.pethome.service.AppointmentReminderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 预约提醒调度入口。
 *
 * 仅保留调度职责；候选筛选、去重与通知下发由 {@link AppointmentReminderService} 实现。
 */
@Component
public class AppointmentReminderTask {

    private static final Logger logger = LoggerFactory.getLogger(AppointmentReminderTask.class);

    private final AppointmentReminderService appointmentReminderService;

    public AppointmentReminderTask(AppointmentReminderService appointmentReminderService) {
        this.appointmentReminderService = appointmentReminderService;
    }

    @Scheduled(cron = "0 0 * * * ?")
    public void runReminders() {
        try {
            int sent = appointmentReminderService.runReminders();
            if (sent > 0) {
                logger.info("预约提醒任务完成：共发送 {} 条", sent);
            }
        } catch (Exception e) {
            logger.error("预约提醒任务异常", e);
        }
    }
}
