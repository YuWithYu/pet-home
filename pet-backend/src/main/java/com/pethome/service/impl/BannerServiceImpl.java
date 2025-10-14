package com.pethome.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.Banner;
import com.pethome.mapper.BannerMapper;
import com.pethome.service.BannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BannerServiceImpl implements BannerService {

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
        return bannerMapper.selectList(null);
    }
}
