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
    /** 前台/小程序：仅上架（status=active），按 sort_order */
    java.util.List<Banner> getAllBanners();
    /** 管理端：全部横幅（含下架），按 id 倒序 */
    java.util.List<Banner> getAllBannersForManagement();
}
