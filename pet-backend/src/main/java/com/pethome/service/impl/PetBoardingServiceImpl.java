package com.pethome.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.PetBoarding;
import com.pethome.mapper.PetBoardingMapper;
import com.pethome.service.PetBoardingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PetBoardingServiceImpl implements PetBoardingService {

    @Autowired
    private PetBoardingMapper petBoardingMapper;

    @Override
    public IPage<PetBoarding> getPetBoardingList(Page<PetBoarding> page) {
        return petBoardingMapper.selectPage(page, null);
    }

    @Override
    public PetBoarding createPetBoarding(PetBoarding petBoarding) {
        petBoardingMapper.insert(petBoarding);
        return petBoarding;
    }

    @Override
    public PetBoarding updatePetBoardingStatus(Long id, String status) {
        PetBoarding petBoarding = petBoardingMapper.selectById(id);
        if (petBoarding != null) {
            petBoarding.setStatus(status);
            petBoardingMapper.updateById(petBoarding);
        }
        return petBoarding;
    }

    @Override
    public PetBoarding getPetBoardingById(Long id) {
        return petBoardingMapper.selectById(id);
    }
}
