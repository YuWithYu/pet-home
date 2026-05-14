package com.pethome.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.Banner;
import com.pethome.mapper.BannerMapper;
import com.pethome.service.BannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class BannerServiceImpl implements BannerService {

    /** 前台展示：兼容历史数据（active / 1 / enabled / NULL） */
    private static final List<String> PUBLISHED_STATUS = Arrays.asList("active", "1", "enabled");

    @Autowired
    private BannerMapper bannerMapper;

    @Override
    public IPage<Banner> getBannerList(Page<Banner> page) {
        return bannerMapper.selectPage(page, null);
    }

    @Override
    public Banner createBanner(Banner banner) {
        bannerMapper.insert(banner);
        return banner;
    }

    @Override
    public Banner updateBanner(Banner banner) {
        bannerMapper.updateById(banner);
        return banner;
    }

    @Override
    public boolean deleteBanner(Long id) {
        return bannerMapper.deleteById(id) > 0;
    }

    @Override
    public Banner getBannerById(Long id) {
        return bannerMapper.selectById(id);
    }

    @Override
    public java.util.List<Banner> getAllBanners() {
        QueryWrapper<Banner> q = new QueryWrapper<>();
        q.and(w -> w.in("status", PUBLISHED_STATUS).or().isNull("status"))
                .orderByAsc("sort_order")
                .orderByDesc("id")
                .last("limit 100");
        return bannerMapper.selectList(q);
    }

    @Override
    public java.util.List<Banner> getAllBannersForManagement() {
        QueryWrapper<Banner> q = new QueryWrapper<>();
        q.orderByDesc("id");
        return bannerMapper.selectList(q);
    }
}
