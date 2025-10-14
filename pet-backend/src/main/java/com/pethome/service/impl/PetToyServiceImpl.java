package com.pethome.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.PetToy;
import com.pethome.mapper.PetToyMapper;
import com.pethome.service.PetToyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PetToyServiceImpl implements PetToyService {

    @Autowired
    private PetToyMapper petToyMapper;

    @Override
    public IPage<PetToy> getPetToyList(Page<PetToy> page) {
        return petToyMapper.selectPage(page, null);
    }

    @Override
    public PetToy createPetToy(PetToy petToy) {
        petToyMapper.insert(petToy);
        return petToy;
    }

    @Override
    public PetToy updatePetToy(PetToy petToy) {
        petToyMapper.updateById(petToy);
        return petToy;
    }

    @Override
    public boolean deletePetToy(Long id) {
        return petToyMapper.deleteById(id) > 0;
    }

    @Override
    public PetToy getPetToyById(Long id) {
        return petToyMapper.selectById(id);
    }
}


