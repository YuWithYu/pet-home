package com.pethome.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.PetGrooming;

public interface PetGroomingService {
    IPage<PetGrooming> getPetGroomingList(Page<PetGrooming> page);
    PetGrooming createPetGrooming(PetGrooming petGrooming);
    PetGrooming updatePetGrooming(PetGrooming petGrooming);
    boolean deletePetGrooming(Long id);
    PetGrooming getPetGroomingById(Long id);
}


