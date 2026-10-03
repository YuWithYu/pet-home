package com.pethome.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.MedicalService;
import com.pethome.mapper.MedicalServiceMapper;
import com.pethome.service.MedicalServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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
        // 设置创建时间和更新时间
        LocalDateTime now = LocalDateTime.now();
        medicalService.setCreateTime(now);
        medicalService.setUpdateTime(now);
        
        // 如果没有设置状态，默认设为active
        if (medicalService.getStatus() == null) {
            medicalService.setStatus("active");
        }
        
        medicalServiceMapper.insert(medicalService);
        return medicalService;
    }

    @Override
    public MedicalService updateMedicalService(MedicalService medicalService) {
        // 设置更新时间
        medicalService.setUpdateTime(LocalDateTime.now());
        
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
