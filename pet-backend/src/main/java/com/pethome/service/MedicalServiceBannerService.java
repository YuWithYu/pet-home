package com.pethome.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.MedicalServiceBanner;

public interface MedicalServiceBannerService {
    IPage<MedicalServiceBanner> getMedicalServiceBannerList(Page<MedicalServiceBanner> page);
    MedicalServiceBanner createMedicalServiceBanner(MedicalServiceBanner banner);
    MedicalServiceBanner updateMedicalServiceBanner(MedicalServiceBanner banner);
    boolean deleteMedicalServiceBanner(Long id);
    MedicalServiceBanner getBannerByPosition(String position);
    MedicalServiceBanner getMedicalServiceBannerById(Long id);
}
