package com.pethome.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pethome.entity.BoardingAppointment;
import com.pethome.mapper.BoardingAppointmentMapper;
import com.pethome.service.BoardingAppointmentService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BoardingAppointmentServiceImpl extends ServiceImpl<BoardingAppointmentMapper, BoardingAppointment> implements BoardingAppointmentService {

    @Override
    public boolean createBoardingAppointment(BoardingAppointment appointment) {
        return this.save(appointment);
    }

    @Override
    public List<BoardingAppointment> getAppointmentsByUserId(Long userId) {
        QueryWrapper<BoardingAppointment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                   .orderByDesc("create_time");
        return this.list(queryWrapper);
    }

    @Override
    public BoardingAppointment getAppointmentById(Long id) {
        return this.getById(id);
    }

    @Override
    public boolean updateAppointmentStatus(Long id, String status) {
        BoardingAppointment appointment = this.getById(id);
        if (appointment != null) {
            appointment.setStatus(status);
            return this.updateById(appointment);
        }
        return false;
    }
}

