package com.pethome.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.Banner;

public interface BannerService {
    IPage<Banner> getBannerList(Page<Banner> page);
    Banner createBanner(Banner banner);
    Banner updateBanner(Banner banner);
    boolean deleteBanner(Long id);
    Banner getBannerById(Long id);
    java.util.List<Banner> getAllBanners();
}
