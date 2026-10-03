package com.pethome.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.GroomingServiceBanner;

public interface GroomingServiceBannerService {
    IPage<GroomingServiceBanner> getGroomingServiceBannerList(Page<GroomingServiceBanner> page);
    GroomingServiceBanner createGroomingServiceBanner(GroomingServiceBanner banner);
    GroomingServiceBanner updateGroomingServiceBanner(GroomingServiceBanner banner);
    boolean deleteGroomingServiceBanner(Long id);
    GroomingServiceBanner getBannerByPosition(String position);
    GroomingServiceBanner getGroomingServiceBannerById(Long id);
}


