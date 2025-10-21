package com.pethome.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pethome.entity.PetAdoption;
import com.pethome.mapper.PetAdoptionMapper;
import com.pethome.service.PetAdoptionService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PetAdoptionServiceImpl extends ServiceImpl<PetAdoptionMapper, PetAdoption> implements PetAdoptionService {

    @Override
    public List<PetAdoption> getAvailablePets() {
        QueryWrapper<PetAdoption> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", "available")
                   .orderByDesc("create_time");
        return this.list(queryWrapper);
    }

    @Override
    public PetAdoption getPetById(Long id) {
        return this.getById(id);
    }
}