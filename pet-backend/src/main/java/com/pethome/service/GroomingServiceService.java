package com.pethome.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.pethome.entity.GroomingService;

public interface GroomingServiceService extends IService<GroomingService> {
    IPage<GroomingService> getGroomingServicePage(Page<GroomingService> page, String category, String status, String name);
}
