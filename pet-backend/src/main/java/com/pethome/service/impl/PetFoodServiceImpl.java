package com.pethome.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.PetFood;
import com.pethome.mapper.PetFoodMapper;
import com.pethome.service.PetFoodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PetFoodServiceImpl implements PetFoodService {

    @Autowired
    private PetFoodMapper petFoodMapper;

    @Override
    public IPage<PetFood> getPetFoodList(Page<PetFood> page) {
        return petFoodMapper.selectPage(page, null);
    }

    @Override
    public PetFood createPetFood(PetFood petFood) {
        petFoodMapper.insert(petFood);
        return petFood;
    }

    @Override
    public PetFood updatePetFood(PetFood petFood) {
        petFoodMapper.updateById(petFood);
        return petFood;
    }

    @Override
    public boolean deletePetFood(Long id) {
        return petFoodMapper.deleteById(id) > 0;
    }

    @Override
    public PetFood getPetFoodById(Long id) {
        return petFoodMapper.selectById(id);
    }
}


