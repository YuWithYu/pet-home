package com.pethome.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.PetVaccine;
import com.pethome.mapper.PetVaccineMapper;
import com.pethome.service.PetVaccineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PetVaccineServiceImpl implements PetVaccineService {

    @Autowired
    private PetVaccineMapper petVaccineMapper;

    @Override
    public IPage<PetVaccine> getPetVaccineList(Page<PetVaccine> page) {
        return petVaccineMapper.selectPage(page, null);
    }

    @Override
    public PetVaccine createPetVaccine(PetVaccine petVaccine) {
        petVaccineMapper.insert(petVaccine);
        return petVaccine;
    }

    @Override
    public PetVaccine updatePetVaccine(PetVaccine petVaccine) {
        petVaccineMapper.updateById(petVaccine);
        return petVaccine;
    }

    @Override
    public boolean deletePetVaccine(Long id) {
        return petVaccineMapper.deleteById(id) > 0;
    }

    @Override
    public PetVaccine getPetVaccineById(Long id) {
        return petVaccineMapper.selectById(id);
    }
}


