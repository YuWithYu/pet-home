package com.pethome.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pethome.entity.AdoptionAppointment;
import com.pethome.mapper.AdoptionAppointmentMapper;
import com.pethome.service.AdoptionAppointmentService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdoptionAppointmentServiceImpl extends ServiceImpl<AdoptionAppointmentMapper, AdoptionAppointment> implements AdoptionAppointmentService {

    @Override
    public boolean createAdoptionAppointment(AdoptionAppointment appointment) {
        return this.save(appointment);
    }

    @Override
    public List<AdoptionAppointment> getAppointmentsByUserId(Long userId) {
        QueryWrapper<AdoptionAppointment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                   .orderByDesc("create_time");
        return this.list(queryWrapper);
    }

    @Override
    public AdoptionAppointment getAppointmentById(Long id) {
        return this.getById(id);
    }

    @Override
    public boolean updateAppointmentStatus(Long id, String status) {
        AdoptionAppointment appointment = this.getById(id);
        if (appointment != null) {
            appointment.setStatus(status);
            return this.updateById(appointment);
        }
        return false;
    }
}

