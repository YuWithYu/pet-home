package com.pethome.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.GroomingServiceBanner;
import com.pethome.mapper.GroomingServiceBannerMapper;
import com.pethome.service.GroomingServiceBannerService;

@Service
public class GroomingServiceBannerServiceImpl implements GroomingServiceBannerService {

    @Autowired
    private GroomingServiceBannerMapper groomingServiceBannerMapper;

    @Override
    public IPage<GroomingServiceBanner> getGroomingServiceBannerList(Page<GroomingServiceBanner> page) {
        return groomingServiceBannerMapper.selectPage(page, null);
    }

    @Override
    public GroomingServiceBanner createGroomingServiceBanner(GroomingServiceBanner banner) {
        groomingServiceBannerMapper.insert(banner);
        return banner;
    }

    @Override
    public GroomingServiceBanner updateGroomingServiceBanner(GroomingServiceBanner banner) {
        groomingServiceBannerMapper.updateById(banner);
        return banner;
    }

    @Override
    public boolean deleteGroomingServiceBanner(Long id) {
        return groomingServiceBannerMapper.deleteById(id) > 0;
    }

    @Override
    public GroomingServiceBanner getBannerByPosition(String position) {
        QueryWrapper<GroomingServiceBanner> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("position", position)
                   .eq("status", "active")
                   .eq("is_deleted", false)
                   .orderByDesc("sort_order")
                   .last("limit 1");
        return groomingServiceBannerMapper.selectOne(queryWrapper);
    }

    @Override
    public GroomingServiceBanner getGroomingServiceBannerById(Long id) {
        return groomingServiceBannerMapper.selectById(id);
    }
}
