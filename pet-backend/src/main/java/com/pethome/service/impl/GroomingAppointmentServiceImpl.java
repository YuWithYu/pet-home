package com.pethome.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pethome.entity.GroomingAppointment;
import com.pethome.mapper.GroomingAppointmentMapper;
import com.pethome.service.GroomingAppointmentService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GroomingAppointmentServiceImpl extends ServiceImpl<GroomingAppointmentMapper, GroomingAppointment> implements GroomingAppointmentService {

    @Override
    public boolean createGroomingAppointment(GroomingAppointment appointment) {
        return this.save(appointment);
    }

    @Override
    public List<GroomingAppointment> getAppointmentsByUserId(Long userId) {
        QueryWrapper<GroomingAppointment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                   .orderByDesc("create_time");
        return this.list(queryWrapper);
    }

    @Override
    public GroomingAppointment getAppointmentById(Long id) {
        return this.getById(id);
    }

    @Override
    public boolean updateAppointmentStatus(Long id, String status) {
        GroomingAppointment appointment = this.getById(id);
        if (appointment != null) {
            appointment.setStatus(status);
            return this.updateById(appointment);
        }
        return false;
    }
}

