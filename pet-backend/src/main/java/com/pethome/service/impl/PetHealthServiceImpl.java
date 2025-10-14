package com.pethome.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.PetHealth;
import com.pethome.mapper.PetHealthMapper;
import com.pethome.service.PetHealthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PetHealthServiceImpl implements PetHealthService {

    @Autowired
    private PetHealthMapper petHealthMapper;

    @Override
    public IPage<PetHealth> getPetHealthList(Page<PetHealth> page) {
        return petHealthMapper.selectPage(page, null);
    }

    @Override
    public PetHealth createPetHealth(PetHealth petHealth) {
        petHealthMapper.insert(petHealth);
        return petHealth;
    }

    @Override
    public PetHealth updatePetHealth(PetHealth petHealth) {
        petHealthMapper.updateById(petHealth);
        return petHealth;
    }

    @Override
    public boolean deletePetHealth(Long id) {
        return petHealthMapper.deleteById(id) > 0;
    }

    @Override
    public PetHealth getPetHealthById(Long id) {
        return petHealthMapper.selectById(id);
    }
}


