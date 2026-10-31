package com.pethome.service.appointment.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pethome.common.BusinessException;
import com.pethome.entity.TimeSlot;
import com.pethome.mapper.TimeSlotMapper;
import com.pethome.service.appointment.TimeSlotService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TimeSlotServiceImpl extends ServiceImpl<TimeSlotMapper, TimeSlot> implements TimeSlotService {

    @Override
    public List<TimeSlot> listTimeSlots(String serviceType, Long storeId) {
        QueryWrapper<TimeSlot> wrapper = new QueryWrapper<>();
        wrapper.eq("service_type", serviceType);
        if (storeId != null) {
            wrapper.eq("store_id", storeId);
        } else {
            wrapper.isNull("store_id");
        }
        wrapper.orderByAsc("time_slot");
        return list(wrapper);
    }

    @Override
    public TimeSlot createTimeSlot(TimeSlot timeSlot) {
        QueryWrapper<TimeSlot> wrapper = new QueryWrapper<>();
        wrapper.eq("service_type", timeSlot.getServiceType());
        wrapper.eq("time_slot", timeSlot.getTimeSlot());
        if (timeSlot.getStoreId() != null) {
            wrapper.eq("store_id", timeSlot.getStoreId());
        } else {
            wrapper.isNull("store_id");
        }
        if (count(wrapper) > 0) {
            throw new BusinessException("该时间段已存在");
        }
        if (!save(timeSlot)) {
            throw new BusinessException("添加失败");
        }
        return timeSlot;
    }

    @Override
    public TimeSlot updateTimeSlot(TimeSlot timeSlot) {
        if (!updateById(timeSlot)) {
            throw new BusinessException("更新失败");
        }
        return timeSlot;
    }

    @Override
    public boolean updateTimeSlotStatus(TimeSlot timeSlot, Boolean isActive) {
        timeSlot.setIsActive(isActive);
        if (!updateById(timeSlot)) {
            throw new BusinessException("更新失败");
        }
        return true;
    }

    @Override
    public boolean deleteTimeSlot(Long id) {
        if (!removeById(id)) {
            throw new BusinessException("删除失败");
        }
        return true;
    }
}
