package com.pethome.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.Appointment;

public interface AppointmentService {
    IPage<Appointment> getAppointmentList(Page<Appointment> page);
    Appointment createAppointment(Appointment appointment);
    Appointment updateAppointmentStatus(Long id, String status);
    Appointment getAppointmentById(Long id);
    java.util.List<Appointment> getUserAppointments(Long userId);
}