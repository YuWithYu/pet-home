package com.pethome.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.PetFood;

public interface PetFoodService {
    IPage<PetFood> getPetFoodList(Page<PetFood> page);
    PetFood createPetFood(PetFood petFood);
    PetFood updatePetFood(PetFood petFood);
    boolean deletePetFood(Long id);
    PetFood getPetFoodById(Long id);
}

