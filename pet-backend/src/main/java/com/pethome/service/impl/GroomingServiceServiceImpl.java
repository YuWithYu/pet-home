package com.pethome.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pethome.entity.GroomingService;
import com.pethome.mapper.GroomingServiceMapper;
import com.pethome.service.GroomingServiceService;

@Service
public class GroomingServiceServiceImpl extends ServiceImpl<GroomingServiceMapper, GroomingService> implements GroomingServiceService {

    @Autowired
    private GroomingServiceMapper groomingServiceMapper;

    @Override
    public IPage<GroomingService> getGroomingServicePage(Page<GroomingService> page, String category, String status, String name) {
        QueryWrapper<GroomingService> queryWrapper = new QueryWrapper<>();

        if (category != null && !category.isEmpty()) {
            queryWrapper.eq("category", category);
        }

        if (status != null && !status.isEmpty()) {
            queryWrapper.eq("status", status);
        }

        if (name != null && !name.isEmpty()) {
            queryWrapper.like("name", name);
        }

        queryWrapper.eq("is_deleted", false)
                   .orderByAsc("sort_order")
                   .orderByDesc("created_at");

        return groomingServiceMapper.selectPage(page, queryWrapper);
    }
}
