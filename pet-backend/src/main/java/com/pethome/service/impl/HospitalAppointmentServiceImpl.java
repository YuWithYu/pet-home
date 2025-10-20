package com.pethome.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pethome.entity.HospitalAppointment;
import com.pethome.mapper.HospitalAppointmentMapper;
import com.pethome.service.HospitalAppointmentService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HospitalAppointmentServiceImpl extends ServiceImpl<HospitalAppointmentMapper, HospitalAppointment> implements HospitalAppointmentService {

    @Override
    public boolean createHospitalAppointment(HospitalAppointment appointment) {
        return this.save(appointment);
    }

    @Override
    public List<HospitalAppointment> getAppointmentsByUserId(Long userId) {
        QueryWrapper<HospitalAppointment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                   .orderByDesc("create_time");
        return this.list(queryWrapper);
    }

    @Override
    public HospitalAppointment getAppointmentById(Long id) {
        return this.getById(id);
    }

    @Override
    public boolean updateAppointmentStatus(Long id, String status) {
        HospitalAppointment appointment = this.getById(id);
        if (appointment != null) {
            appointment.setStatus(status);
            return this.updateById(appointment);
        }
        return false;
    }
}
