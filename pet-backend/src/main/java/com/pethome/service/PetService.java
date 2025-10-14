package com.pethome.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.Pet;

public interface PetService {
    IPage<Pet> getPetListByUserId(Page<Pet> page, Long userId);
    Pet createPet(Pet pet);
    Pet updatePet(Pet pet);
    boolean deletePet(Long id);
    Pet getPetById(Long id);
}
