package com.pethome.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.Pet;

import java.util.List;

public interface PetService {
    IPage<Pet> getPetListByUserId(Page<Pet> page, Long userId);
    
    /**
     * 根据用户ID获取宠物列表（不分页）
     * @param userId 用户ID
     * @return 宠物列表
     */
    List<Pet> getPetsByUserId(Long userId);
    
    Pet createPet(Pet pet);
    Pet updatePet(Pet pet);
    boolean deletePet(Long id);
    Pet getPetById(Long id);
    
    /**
     * 清除指定用户的宠物列表缓存
     * @param userId 用户ID
     */
    void clearPetListCache(Long userId);
}
