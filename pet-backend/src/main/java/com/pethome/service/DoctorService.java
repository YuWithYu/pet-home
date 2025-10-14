package com.pethome.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.Doctor;

public interface DoctorService {
    IPage<Doctor> getDoctorList(Page<Doctor> page);
    Doctor createDoctor(Doctor doctor);
    Doctor updateDoctor(Doctor doctor);
    boolean deleteDoctor(Long id);
    Doctor getDoctorById(Long id);
}
