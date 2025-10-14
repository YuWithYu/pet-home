package com.pethome.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.Doctor;
import com.pethome.mapper.DoctorMapper;
import com.pethome.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DoctorServiceImpl implements DoctorService {

    @Autowired
    private DoctorMapper doctorMapper;

    @Override
    public IPage<Doctor> getDoctorList(Page<Doctor> page) {
        return doctorMapper.selectPage(page, null);
    }

    @Override
    public Doctor createDoctor(Doctor doctor) {
        doctorMapper.insert(doctor);
        return doctor;
    }

    @Override
    public Doctor updateDoctor(Doctor doctor) {
        doctorMapper.updateById(doctor);
        return doctor;
    }

    @Override
    public boolean deleteDoctor(Long id) {
        return doctorMapper.deleteById(id) > 0;
    }

    @Override
    public Doctor getDoctorById(Long id) {
        return doctorMapper.selectById(id);
    }
}
