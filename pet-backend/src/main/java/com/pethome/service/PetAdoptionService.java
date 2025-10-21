package com.pethome.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pethome.entity.PetAdoption;

import java.util.List;

public interface PetAdoptionService extends IService<PetAdoption> {
    
    /**
     * 获取可领养的宠物列表
     */
    List<PetAdoption> getAvailablePets();
    
    /**
     * 根据ID获取宠物详情
     */
    PetAdoption getPetById(Long id);
}