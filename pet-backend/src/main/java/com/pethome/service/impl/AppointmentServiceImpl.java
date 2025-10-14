package com.pethome.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.Appointment;
import com.pethome.mapper.AppointmentMapper;
import com.pethome.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Override
    public IPage<Appointment> getAppointmentList(Page<Appointment> page) {
        return appointmentMapper.selectPage(page, null);
    }

    @Override
    public Appointment createAppointment(Appointment appointment) {
        // 设置创建时间和更新时间
        appointment.setCreateTime(LocalDateTime.now());
        appointment.setUpdateTime(LocalDateTime.now());

        // 如果没有设置状态，默认设为待确认
        if (appointment.getStatus() == null) {
            appointment.setStatus("pending");
        }

        appointmentMapper.insert(appointment);
        return appointment;
    }

    @Override
    public Appointment updateAppointmentStatus(Long id, String status) {
        Appointment appointment = appointmentMapper.selectById(id);
        if (appointment != null) {
            appointment.setStatus(status);
            appointment.setUpdateTime(LocalDateTime.now());
            appointmentMapper.updateById(appointment);
        }
        return appointment;
    }

    @Override
    public Appointment getAppointmentById(Long id) {
        return appointmentMapper.selectById(id);
    }

    @Override
    public List<Appointment> getUserAppointments(Long userId) {
        QueryWrapper<Appointment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .orderByDesc("create_time");
        return appointmentMapper.selectList(queryWrapper);
    }
}