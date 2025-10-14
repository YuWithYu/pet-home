package com.pethome.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.PetAdoption;
import com.pethome.mapper.PetAdoptionMapper;
import com.pethome.service.PetAdoptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PetAdoptionServiceImpl implements PetAdoptionService {

    @Autowired
    private PetAdoptionMapper petAdoptionMapper;

    @Override
    public IPage<PetAdoption> getPetAdoptionList(Page<PetAdoption> page) {
        return petAdoptionMapper.selectPage(page, null);
    }

    @Override
    public PetAdoption createPetAdoption(PetAdoption petAdoption) {
        petAdoptionMapper.insert(petAdoption);
        return petAdoption;
    }

    @Override
    public PetAdoption updatePetAdoption(PetAdoption petAdoption) {
        petAdoptionMapper.updateById(petAdoption);
        return petAdoption;
    }

    @Override
    public boolean deletePetAdoption(Long id) {
        return petAdoptionMapper.deleteById(id) > 0;
    }

    @Override
    public PetAdoption getPetAdoptionById(Long id) {
        return petAdoptionMapper.selectById(id);
    }
}
