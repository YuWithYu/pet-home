package com.pethome.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.pethome.entity.GroomingService;

public interface GroomingServiceService extends IService<GroomingService> {
    IPage<GroomingService> getGroomingServicePage(Page<GroomingService> page, QueryWrapper<GroomingService> queryWrapper);

    GroomingService createGroomingService(GroomingService groomingService);

    GroomingService updateGroomingService(GroomingService groomingService);

    boolean deleteGroomingService(Long id);

    GroomingService getGroomingServiceById(Long id);
}
