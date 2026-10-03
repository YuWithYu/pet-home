package com.pethome.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.LitterService;

public interface LitterServiceService {
    IPage<LitterService> getLitterServiceList(Page<LitterService> page);
    IPage<LitterService> getLitterServiceList(Page<LitterService> page, QueryWrapper<LitterService> queryWrapper);
    LitterService createLitterService(LitterService litterService);
    LitterService updateLitterService(LitterService litterService);
    boolean deleteLitterService(Long id);
    LitterService getLitterServiceById(Long id);
}