package com.pethome.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.PetProduct;
import com.pethome.mapper.PetProductMapper;
import com.pethome.service.PetProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PetProductServiceImpl implements PetProductService {

    @Autowired
    private PetProductMapper petProductMapper;

    @Override
    public IPage<PetProduct> getPetProductList(Page<PetProduct> page) {
        return petProductMapper.selectPage(page, null);
    }

    @Override
    public PetProduct createPetProduct(PetProduct petProduct) {
        petProductMapper.insert(petProduct);
        return petProduct;
    }

    @Override
    public PetProduct updatePetProduct(PetProduct petProduct) {
        petProductMapper.updateById(petProduct);
        return petProduct;
    }

    @Override
    public boolean deletePetProduct(Long id) {
        return petProductMapper.deleteById(id) > 0;
    }

    @Override
    public PetProduct getPetProductById(Long id) {
        return petProductMapper.selectById(id);
    }
}


