package com.pethome.task;

import com.pethome.service.AppointmentAutoProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 预约自动处理调度入口（兜底机制）。
 *
 * 仅保留调度职责；业务规则与批量更新由 {@link AppointmentAutoProcessService} 实现，
 * 避免定时任务绕过服务层直连数据库（历史事故：批量 UPDATE 绕过 Service 导致库存状态被锁死）。
 */
@Component
public class AppointmentAutoExpireTask {

    private static final Logger logger = LoggerFactory.getLogger(AppointmentAutoExpireTask.class);

    private final AppointmentAutoProcessService appointmentAutoProcessService;

    public AppointmentAutoExpireTask(AppointmentAutoProcessService appointmentAutoProcessService) {
        this.appointmentAutoProcessService = appointmentAutoProcessService;
    }

    @Scheduled(cron = "0 0 * * * ?")
    public void autoProcessAppointments() {
        try {
            int totalProcessed = appointmentAutoProcessService.autoProcessAppointments();
            if (totalProcessed > 0) {
                logger.info("预约自动处理任务完成：共处理 {} 条", totalProcessed);
            }
        } catch (Exception e) {
            logger.error("预约自动处理任务异常", e);
        }
    }
}
