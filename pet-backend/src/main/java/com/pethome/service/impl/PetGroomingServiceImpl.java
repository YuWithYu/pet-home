package com.pethome.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.PetGrooming;
import com.pethome.mapper.PetGroomingMapper;
import com.pethome.service.PetGroomingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PetGroomingServiceImpl implements PetGroomingService {

    @Autowired
    private PetGroomingMapper petGroomingMapper;

    @Override
    public IPage<PetGrooming> getPetGroomingList(Page<PetGrooming> page) {
        return petGroomingMapper.selectPage(page, null);
    }

    @Override
    public PetGrooming createPetGrooming(PetGrooming petGrooming) {
        petGroomingMapper.insert(petGrooming);
        return petGrooming;
    }

    @Override
    public PetGrooming updatePetGrooming(PetGrooming petGrooming) {
        petGroomingMapper.updateById(petGrooming);
        return petGrooming;
    }

    @Override
    public boolean deletePetGrooming(Long id) {
        return petGroomingMapper.deleteById(id) > 0;
    }

    @Override
    public PetGrooming getPetGroomingById(Long id) {
        return petGroomingMapper.selectById(id);
    }
}


