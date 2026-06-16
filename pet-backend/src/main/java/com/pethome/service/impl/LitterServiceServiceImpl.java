package com.pethome.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.LitterService;
import com.pethome.mapper.LitterServiceMapper;
import com.pethome.service.LitterServiceService;

@Service
public class LitterServiceServiceImpl implements LitterServiceService {

    @Autowired
    private LitterServiceMapper litterServiceMapper;

    @Override
    public IPage<LitterService> getLitterServiceList(Page<LitterService> page) {
        return litterServiceMapper.selectPage(page, null);
    }
    
    @Override
    public IPage<LitterService> getLitterServiceList(Page<LitterService> page, QueryWrapper<LitterService> queryWrapper) {
        return litterServiceMapper.selectPage(page, queryWrapper);
    }

    @Override
    public LitterService createLitterService(LitterService litterService) {
        litterServiceMapper.insert(litterService);
        return litterService;
    }

    @Override
    public LitterService updateLitterService(LitterService litterService) {
        System.out.println("=== 更新铲屎服务 ===");
        System.out.println("服务ID: " + litterService.getId());
        System.out.println("introduction: " + litterService.getIntroduction());
        System.out.println("instructions: " + litterService.getInstructions());
        System.out.println("instructions类型: " + (litterService.getInstructions() != null ? litterService.getInstructions().getClass().getName() : "null"));
        
        litterServiceMapper.updateById(litterService);
        
        // 重新查询以确保数据正确
        LitterService updated = litterServiceMapper.selectById(litterService.getId());
        System.out.println("更新后查询结果:");
        System.out.println("introduction: " + updated.getIntroduction());
        System.out.println("instructions: " + updated.getInstructions());
        
        return updated;
    }

    @Override
    public boolean deleteLitterService(Long id) {
        return litterServiceMapper.deleteById(id) > 0;
    }

    @Override
    public LitterService getLitterServiceById(Long id) {
        LitterService service = litterServiceMapper.selectById(id);
        if (service != null) {
            System.out.println("=== 根据ID获取服务 ===");
            System.out.println("服务ID: " + service.getId());
            System.out.println("introduction: " + service.getIntroduction());
            System.out.println("instructions: " + service.getInstructions());
            System.out.println("instructions类型: " + (service.getInstructions() != null ? service.getInstructions().getClass().getName() : "null"));
        }
        return service;
    }
}