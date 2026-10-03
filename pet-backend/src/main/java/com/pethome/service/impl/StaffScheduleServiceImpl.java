package com.pethome.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.pethome.entity.StaffSchedule;
import com.pethome.mapper.StaffScheduleMapper;
import com.pethome.service.StaffScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 员工排班服务实现
 */
@Service
public class StaffScheduleServiceImpl implements StaffScheduleService {
    
    @Autowired
    private StaffScheduleMapper staffScheduleMapper;
    
    @Override
    public StaffSchedule createSchedule(StaffSchedule schedule) {
        if (schedule.getCapacity() == null || schedule.getCapacity() <= 0) {
            schedule.setCapacity(1);
        }
        if (hasConflict(schedule)) {
            throw new IllegalArgumentException("排班时间与已有排班冲突，请调整时间段");
        }
        staffScheduleMapper.insert(schedule);
        return schedule;
    }
    
    @Override
    public StaffSchedule updateSchedule(StaffSchedule schedule) {
        if (schedule.getId() == null) {
            throw new IllegalArgumentException("排班ID不能为空");
        }
        if (schedule.getCapacity() == null || schedule.getCapacity() <= 0) {
            schedule.setCapacity(1);
        }
        if (hasConflict(schedule)) {
            throw new IllegalArgumentException("排班时间与已有排班冲突，请调整时间段");
        }
        staffScheduleMapper.updateById(schedule);
        return schedule;
    }

    @Override
    public List<Long> findAvailableStaffIds(Long departmentId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        // 如果Mapper有自定义方法，使用自定义方法
        // 否则使用QueryWrapper查询
        if (departmentId != null) {
            return staffScheduleMapper.findAvailableStaffIds(departmentId, date, startTime, endTime);
        }
        
        // 备用方案：使用QueryWrapper
        QueryWrapper<StaffSchedule> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("date", date);
        queryWrapper.le("start_time", startTime);
        queryWrapper.ge("end_time", endTime);
        List<StaffSchedule> schedules = staffScheduleMapper.selectList(queryWrapper);
        return schedules.stream()
                .map(StaffSchedule::getAdminId)
                .distinct()
                .collect(java.util.stream.Collectors.toList());
    }
    
    @Override
    public List<StaffSchedule> findByAdminIdAndDate(Long adminId, LocalDate date) {
        return staffScheduleMapper.findByAdminIdAndDate(adminId, date);
    }
    
    @Override
    public List<StaffSchedule> listSchedules(Long adminId, Long departmentId, LocalDate date) {
        QueryWrapper<StaffSchedule> wrapper = Wrappers.query();
        if (adminId != null) {
            wrapper.eq("admin_id", adminId);
        }
        if (departmentId != null) {
            wrapper.eq("department_id", departmentId);
        }
        if (date != null) {
            wrapper.eq("date", date);
        }
        wrapper.orderByAsc("date", "start_time");
        return staffScheduleMapper.selectList(wrapper);
    }

    @Override
    public List<StaffSchedule> listAvailableSchedules(Long departmentId, LocalDate date) {
        QueryWrapper<StaffSchedule> wrapper = Wrappers.query();
        if (departmentId != null) {
            wrapper.eq("department_id", departmentId);
        }
        if (date != null) {
            wrapper.eq("date", date);
        }
        wrapper.orderByAsc("start_time");
        return staffScheduleMapper.selectList(wrapper);
    }

    @Override
    public StaffSchedule getById(Long id) {
        if (id == null) {
            return null;
        }
        return staffScheduleMapper.selectById(id);
    }

    @Override
    public boolean hasConflict(StaffSchedule schedule) {
        if (schedule == null || schedule.getAdminId() == null || schedule.getDate() == null
                || schedule.getStartTime() == null || schedule.getEndTime() == null) {
            return false;
        }
        QueryWrapper<StaffSchedule> wrapper = Wrappers.query();
        wrapper.eq("admin_id", schedule.getAdminId())
                .eq("date", schedule.getDate())
                .lt("start_time", schedule.getEndTime())
                .gt("end_time", schedule.getStartTime());
        if (schedule.getId() != null) {
            wrapper.ne("id", schedule.getId());
        }
        Long count = staffScheduleMapper.selectCount(wrapper);
        return count != null && count > 0;
    }

    @Override
    public boolean deleteSchedule(Long id) {
        return staffScheduleMapper.deleteById(id) > 0;
    }
}

