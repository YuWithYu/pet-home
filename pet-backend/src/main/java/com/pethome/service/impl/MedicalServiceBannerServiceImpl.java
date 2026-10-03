package com.pethome.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.MedicalServiceBanner;
import com.pethome.mapper.MedicalServiceBannerMapper;
import com.pethome.service.MedicalServiceBannerService;

@Service
public class MedicalServiceBannerServiceImpl implements MedicalServiceBannerService {

    @Autowired
    private MedicalServiceBannerMapper medicalServiceBannerMapper;

    @Override
    public IPage<MedicalServiceBanner> getMedicalServiceBannerList(Page<MedicalServiceBanner> page) {
        return medicalServiceBannerMapper.selectPage(page, null);
    }

    @Override
    public MedicalServiceBanner createMedicalServiceBanner(MedicalServiceBanner banner) {
        medicalServiceBannerMapper.insert(banner);
        return banner;
    }

    @Override
    public MedicalServiceBanner updateMedicalServiceBanner(MedicalServiceBanner banner) {
        medicalServiceBannerMapper.updateById(banner);
        return banner;
    }

    @Override
    public boolean deleteMedicalServiceBanner(Long id) {
        return medicalServiceBannerMapper.deleteById(id) > 0;
    }

    @Override
    public MedicalServiceBanner getBannerByPosition(String position) {
        QueryWrapper<MedicalServiceBanner> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("position", position)
                   .eq("status", "active")
                   .eq("is_deleted", false)
                   .orderByDesc("sort_order")
                   .last("limit 1");
        return medicalServiceBannerMapper.selectOne(queryWrapper);
    }

    @Override
    public MedicalServiceBanner getMedicalServiceBannerById(Long id) {
        return medicalServiceBannerMapper.selectById(id);
    }
}
