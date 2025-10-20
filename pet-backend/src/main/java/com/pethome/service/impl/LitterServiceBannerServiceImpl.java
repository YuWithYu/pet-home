package com.pethome.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.LitterServiceBanner;
import com.pethome.mapper.LitterServiceBannerMapper;
import com.pethome.service.LitterServiceBannerService;

@Service
public class LitterServiceBannerServiceImpl implements LitterServiceBannerService {

    @Autowired
    private LitterServiceBannerMapper litterServiceBannerMapper;

    @Override
    public IPage<LitterServiceBanner> getLitterServiceBannerList(Page<LitterServiceBanner> page) {
        return litterServiceBannerMapper.selectPage(page, null);
    }

    @Override
    public LitterServiceBanner createLitterServiceBanner(LitterServiceBanner banner) {
        litterServiceBannerMapper.insert(banner);
        return banner;
    }

    @Override
    public LitterServiceBanner updateLitterServiceBanner(LitterServiceBanner banner) {
        litterServiceBannerMapper.updateById(banner);
        return banner;
    }

    @Override
    public boolean deleteLitterServiceBanner(Long id) {
        return litterServiceBannerMapper.deleteById(id) > 0;
    }

    @Override
    public LitterServiceBanner getBannerByPosition(String position) {
        QueryWrapper<LitterServiceBanner> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("position", position)
                   .eq("status", "active")
                   .eq("is_deleted", false)
                   .orderByDesc("sort_order")
                   .last("limit 1");
        return litterServiceBannerMapper.selectOne(queryWrapper);
    }

    @Override
    public LitterServiceBanner getLitterServiceBannerById(Long id) {
        return litterServiceBannerMapper.selectById(id);
    }
}
