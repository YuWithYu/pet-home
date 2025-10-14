package com.pethome.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.PetToy;

public interface PetToyService {
    IPage<PetToy> getPetToyList(Page<PetToy> page);
    PetToy createPetToy(PetToy petToy);
    PetToy updatePetToy(PetToy petToy);
    boolean deletePetToy(Long id);
    PetToy getPetToyById(Long id);
}

