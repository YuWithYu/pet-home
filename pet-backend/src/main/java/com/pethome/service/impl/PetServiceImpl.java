package com.pethome.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.Pet;
import com.pethome.mapper.PetMapper;
import com.pethome.service.PetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PetServiceImpl implements PetService {

    @Autowired
    private PetMapper petMapper;

    @Override
    public IPage<Pet> getPetListByUserId(Page<Pet> page, Long userId) {
        QueryWrapper<Pet> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        return petMapper.selectPage(page, queryWrapper);
    }

    @Override
    public Pet createPet(Pet pet) {
        petMapper.insert(pet);
        return pet;
    }

    @Override
    public Pet updatePet(Pet pet) {
        petMapper.updateById(pet);
        return pet;
    }

    @Override
    public boolean deletePet(Long id) {
        return petMapper.deleteById(id) > 0;
    }

    @Override
    public Pet getPetById(Long id) {
        return petMapper.selectById(id);
    }
}
