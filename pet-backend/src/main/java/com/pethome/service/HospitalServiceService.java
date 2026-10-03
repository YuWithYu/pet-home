package com.pethome.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.pethome.entity.HospitalService;

public interface HospitalServiceService extends IService<HospitalService> {

    IPage<HospitalService> getHospitalServicePage(Page<HospitalService> page, QueryWrapper<HospitalService> queryWrapper);

    HospitalService createHospitalService(HospitalService hospitalService);

    HospitalService updateHospitalService(HospitalService hospitalService);

    boolean deleteHospitalService(Long id);

    HospitalService getHospitalServiceById(Long id);
}
