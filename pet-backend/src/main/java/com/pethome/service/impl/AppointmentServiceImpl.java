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
import java.util.UUID;

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

        // 生成核销码
        String verifyCode = UUID.randomUUID().toString().replace("-", "");
        appointment.setVerifyCode(verifyCode);
        
        // 默认未核销
        appointment.setIsVerified(0);

        appointmentMapper.insert(appointment);
        return appointment;
    }

    @Override
    public Appointment updateAppointment(Appointment appointment) {
        // 设置更新时间
        appointment.setUpdateTime(LocalDateTime.now());
        
        // 更新预约信息
        appointmentMapper.updateById(appointment);
        
        // 返回更新后的预约信息
        return appointmentMapper.selectById(appointment.getId());
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

    @Override
    public Appointment verifyAppointment(String verifyCode) {
        QueryWrapper<Appointment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("verify_code", verifyCode);
        Appointment appointment = appointmentMapper.selectOne(queryWrapper);
        
        if (appointment != null && appointment.getIsVerified() == 0) {
            // 核销成功，更新状态
            appointment.setIsVerified(1);
            appointment.setVerifyTime(LocalDateTime.now());
            appointment.setUpdateTime(LocalDateTime.now());
            appointmentMapper.updateById(appointment);
            return appointment;
        }
        
        return null; // 无效或已核销
    }
}