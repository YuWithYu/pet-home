package com.pethome.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.MedicalService;
import com.pethome.mapper.MedicalServiceMapper;
import com.pethome.service.MedicalServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MedicalServiceServiceImpl implements MedicalServiceService {

    @Autowired
    private MedicalServiceMapper medicalServiceMapper;

    @Override
    public IPage<MedicalService> getMedicalServiceList(Page<MedicalService> page) {
        return medicalServiceMapper.selectPage(page, null);
    }

    @Override
    public MedicalService createMedicalService(MedicalService medicalService) {
        medicalServiceMapper.insert(medicalService);
        return medicalService;
    }

    @Override
    public MedicalService updateMedicalService(MedicalService medicalService) {
        medicalServiceMapper.updateById(medicalService);
        return medicalService;
    }

    @Override
    public boolean deleteMedicalService(Long id) {
        return medicalServiceMapper.deleteById(id) > 0;
    }

    @Override
    public MedicalService getMedicalServiceById(Long id) {
        return medicalServiceMapper.selectById(id);
    }
}
