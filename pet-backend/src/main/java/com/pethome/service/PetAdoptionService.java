package com.pethome.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.PetAdoption;

public interface PetAdoptionService {
    IPage<PetAdoption> getPetAdoptionList(Page<PetAdoption> page);
    PetAdoption createPetAdoption(PetAdoption petAdoption);
    PetAdoption updatePetAdoption(PetAdoption petAdoption);
    boolean deletePetAdoption(Long id);
    PetAdoption getPetAdoptionById(Long id);
}
