package com.pethome.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.GroomingServiceBanner;
import com.pethome.mapper.GroomingServiceBannerMapper;
import com.pethome.service.GroomingServiceBannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * GroomingServiceBanner 服务实现
 * 重构后继承自 {@link AbstractBannerService}，避免与 LitterServiceBannerServiceImpl 重复代码
 */
@Service
public class GroomingServiceBannerServiceImpl
        extends AbstractBannerService<GroomingServiceBanner>
        implements GroomingServiceBannerService {

    @Autowired
    public GroomingServiceBannerServiceImpl(GroomingServiceBannerMapper mapper) {
        super(mapper);
    }

    @Override
    protected GroomingServiceBanner newInstance() {
        return new GroomingServiceBanner();
    }

    // —— 以下方法委托给基类，保留具体类型签名以兼容 Controller 调用 ——

    @Override
    public IPage<GroomingServiceBanner> getGroomingServiceBannerList(Page<GroomingServiceBanner> page) {
        return getBannerList(page);
    }

    @Override
    public GroomingServiceBanner createGroomingServiceBanner(GroomingServiceBanner banner) {
        return createBanner(banner);
    }

    @Override
    public GroomingServiceBanner updateGroomingServiceBanner(GroomingServiceBanner banner) {
        return updateBanner(banner);
    }

    @Override
    public boolean deleteGroomingServiceBanner(Long id) {
        return deleteBanner(id);
    }

    @Override
    public GroomingServiceBanner getGroomingServiceBannerById(Long id) {
        return getBannerById(id);
    }
}
