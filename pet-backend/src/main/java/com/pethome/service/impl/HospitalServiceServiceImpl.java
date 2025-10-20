package com.pethome.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pethome.entity.HospitalService;
import com.pethome.mapper.HospitalServiceMapper;
import com.pethome.service.HospitalServiceService;
import org.springframework.stereotype.Service;

@Service
public class HospitalServiceServiceImpl extends ServiceImpl<HospitalServiceMapper, HospitalService> implements HospitalServiceService {

    @Override
    public HospitalService getHospitalServiceById(Long id) {
        return this.getById(id);
    }

    @Override
    public boolean updateHospitalService(HospitalService hospitalService) {
        return this.updateById(hospitalService);
    }
}
