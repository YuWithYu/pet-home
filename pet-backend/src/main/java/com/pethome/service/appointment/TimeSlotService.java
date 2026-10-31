package com.pethome.service.appointment;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pethome.entity.TimeSlot;

import java.util.List;

public interface TimeSlotService extends IService<TimeSlot> {

    /**
     * 查询时间段列表：storeId 非空时查该门店，为空时查不限门店（store_id 为空）的配置
     */
    List<TimeSlot> listTimeSlots(String serviceType, Long storeId);

    /**
     * 新增时间段，同一服务类型/门店下时间已存在时抛出业务异常
     */
    TimeSlot createTimeSlot(TimeSlot timeSlot);

    /**
     * 更新时间段，写库失败时抛出业务异常
     */
    TimeSlot updateTimeSlot(TimeSlot timeSlot);

    /**
     * 更新已加载时间段的启用状态，写库失败时抛出业务异常
     */
    boolean updateTimeSlotStatus(TimeSlot timeSlot, Boolean isActive);

    /**
     * 删除时间段，写库失败时抛出业务异常
     */
    boolean deleteTimeSlot(Long id);
}
