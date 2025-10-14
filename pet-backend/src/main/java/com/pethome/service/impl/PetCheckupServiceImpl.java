package com.pethome.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.PetCheckup;
import com.pethome.mapper.PetCheckupMapper;
import com.pethome.service.PetCheckupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PetCheckupServiceImpl implements PetCheckupService {

    @Autowired
    private PetCheckupMapper petCheckupMapper;

    @Override
    public IPage<PetCheckup> getPetCheckupList(Page<PetCheckup> page) {
        return petCheckupMapper.selectPage(page, null);
    }

    @Override
    public PetCheckup createPetCheckup(PetCheckup petCheckup) {
        petCheckupMapper.insert(petCheckup);
        return petCheckup;
    }

    @Override
    public PetCheckup updatePetCheckup(PetCheckup petCheckup) {
        petCheckupMapper.updateById(petCheckup);
        return petCheckup;
    }

    @Override
    public boolean deletePetCheckup(Long id) {
        return petCheckupMapper.deleteById(id) > 0;
    }

    @Override
    public PetCheckup getPetCheckupById(Long id) {
        return petCheckupMapper.selectById(id);
    }
}


