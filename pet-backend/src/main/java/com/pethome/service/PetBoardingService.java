package com.pethome.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.PetBoarding;

public interface PetBoardingService {
    IPage<PetBoarding> getPetBoardingList(Page<PetBoarding> page);
    PetBoarding createPetBoarding(PetBoarding petBoarding);
    PetBoarding updatePetBoardingStatus(Long id, String status);
    PetBoarding getPetBoardingById(Long id);
}
