package com.pethome.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.PetHealth;

public interface PetHealthService {
    IPage<PetHealth> getPetHealthList(Page<PetHealth> page);
    PetHealth createPetHealth(PetHealth petHealth);
    PetHealth updatePetHealth(PetHealth petHealth);
    boolean deletePetHealth(Long id);
    PetHealth getPetHealthById(Long id);
}

