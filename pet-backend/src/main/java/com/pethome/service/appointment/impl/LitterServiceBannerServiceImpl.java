package com.pethome.service.appointment.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.LitterServiceBanner;
import com.pethome.mapper.LitterServiceBannerMapper;
import com.pethome.service.appointment.LitterServiceBannerService;
import com.pethome.service.common.impl.AbstractBannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * LitterServiceBanner 服务实现
 * 重构后继承自 {@link AbstractBannerService}，避免与 GroomingServiceBannerServiceImpl 重复代码
 */
@Service
public class LitterServiceBannerServiceImpl
        extends AbstractBannerService<LitterServiceBanner>
        implements LitterServiceBannerService {

    @Autowired
    public LitterServiceBannerServiceImpl(LitterServiceBannerMapper mapper) {
        super(mapper);
    }

    @Override
    protected LitterServiceBanner newInstance() {
        return new LitterServiceBanner();
    }

    // —— 以下方法委托给基类，保留具体类型签名以兼容 Controller 调用 ——

    @Override
    public IPage<LitterServiceBanner> getLitterServiceBannerList(Page<LitterServiceBanner> page) {
        return getBannerList(page);
    }

    @Override
    public LitterServiceBanner createLitterServiceBanner(LitterServiceBanner banner) {
        return createBanner(banner);
    }

    @Override
    public LitterServiceBanner updateLitterServiceBanner(LitterServiceBanner banner) {
        return updateBanner(banner);
    }

    @Override
    public boolean deleteLitterServiceBanner(Long id) {
        return deleteBanner(id);
    }

    @Override
    public LitterServiceBanner getLitterServiceBannerById(Long id) {
        return getBannerById(id);
    }
}
