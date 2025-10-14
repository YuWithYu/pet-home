package com.pethome.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.PetProduct;

public interface PetProductService {
    IPage<PetProduct> getPetProductList(Page<PetProduct> page);
    PetProduct createPetProduct(PetProduct petProduct);
    PetProduct updatePetProduct(PetProduct petProduct);
    boolean deletePetProduct(Long id);
    PetProduct getPetProductById(Long id);
}

