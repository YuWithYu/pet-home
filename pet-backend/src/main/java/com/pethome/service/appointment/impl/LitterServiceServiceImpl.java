package com.pethome.service.appointment.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.LitterService;
import com.pethome.mapper.LitterServiceMapper;
import com.pethome.service.appointment.LitterServiceService;

@Service
public class LitterServiceServiceImpl implements LitterServiceService {

    private static final Logger log = LoggerFactory.getLogger(LitterServiceServiceImpl.class);

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
        litterServiceMapper.updateById(litterService);

        // 重新查询以确保数据正确
        LitterService updated = litterServiceMapper.selectById(litterService.getId());

        return updated;
    }

    @Override
    public boolean deleteLitterService(Long id) {
        return litterServiceMapper.deleteById(id) > 0;
    }

    @Override
    public LitterService getLitterServiceById(Long id) {
        LitterService service = litterServiceMapper.selectById(id);
        return service;
    }
}