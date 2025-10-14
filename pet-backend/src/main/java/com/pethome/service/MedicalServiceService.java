package com.pethome.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.MedicalService;

public interface MedicalServiceService {
    IPage<MedicalService> getMedicalServiceList(Page<MedicalService> page);
    MedicalService createMedicalService(MedicalService medicalService);
    MedicalService updateMedicalService(MedicalService medicalService);
    boolean deleteMedicalService(Long id);
    MedicalService getMedicalServiceById(Long id);
}
