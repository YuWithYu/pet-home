package com.pethome.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.LitterServiceBanner;

public interface LitterServiceBannerService {
    IPage<LitterServiceBanner> getLitterServiceBannerList(Page<LitterServiceBanner> page);
    LitterServiceBanner createLitterServiceBanner(LitterServiceBanner banner);
    LitterServiceBanner updateLitterServiceBanner(LitterServiceBanner banner);
    boolean deleteLitterServiceBanner(Long id);
    LitterServiceBanner getBannerByPosition(String position);
    LitterServiceBanner getLitterServiceBannerById(Long id);
}
