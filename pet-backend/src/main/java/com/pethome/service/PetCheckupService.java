package com.pethome.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.PetCheckup;

public interface PetCheckupService {
    IPage<PetCheckup> getPetCheckupList(Page<PetCheckup> page);
    PetCheckup createPetCheckup(PetCheckup petCheckup);
    PetCheckup updatePetCheckup(PetCheckup petCheckup);
    boolean deletePetCheckup(Long id);
    PetCheckup getPetCheckupById(Long id);
}


