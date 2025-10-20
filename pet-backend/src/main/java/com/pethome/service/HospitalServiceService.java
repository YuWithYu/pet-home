package com.pethome.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pethome.entity.HospitalService;

public interface HospitalServiceService extends IService<HospitalService> {
    
    /**
     * 根据ID获取宠物医院服务
     */
    HospitalService getHospitalServiceById(Long id);
    
    /**
     * 更新宠物医院服务
     */
    boolean updateHospitalService(HospitalService hospitalService);
}
