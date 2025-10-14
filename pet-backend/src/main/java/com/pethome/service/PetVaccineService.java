package com.pethome.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.PetVaccine;

public interface PetVaccineService {
    IPage<PetVaccine> getPetVaccineList(Page<PetVaccine> page);
    PetVaccine createPetVaccine(PetVaccine petVaccine);
    PetVaccine updatePetVaccine(PetVaccine petVaccine);
    boolean deletePetVaccine(Long id);
    PetVaccine getPetVaccineById(Long id);
}

