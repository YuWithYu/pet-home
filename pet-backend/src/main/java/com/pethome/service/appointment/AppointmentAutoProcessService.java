package com.pethome.service.appointment;

/**
 * 预约批处理（业务闭环兜底）。
 *
 * 从定时任务中剥离：调度属于基础设施，业务规则（何时自动确认、何时判定爽约）属于领域服务。
 */
public interface AppointmentAutoProcessService {

    /**
     * 自动确认临期未处理预约，并将超时未核销的预约标记为爽约。
     *
     * @return 本次处理的预约总条数
     */
    int autoProcessAppointments();
}
