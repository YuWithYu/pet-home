package com.pethome.service.common.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * Banner 服务泛型抽象基类
 *
 * 设计目的：GroomingServiceBannerServiceImpl 与 LitterServiceBannerServiceImpl 的 6 个方法
 * 逻辑完全相同，仅实体类型不同。抽到基类后子类只需声明 Mapper 与实体类型，避免重复代码。
 *
 * 使用方式：
 * <pre>
 *   class GroomingServiceBannerServiceImpl
 *       extends AbstractBannerService&lt;GroomingServiceBanner&gt;
 *       implements GroomingServiceBannerService {
 *
 *       public GroomingServiceBannerServiceImpl(GroomingServiceBannerMapper mapper) {
 *           super(mapper);
 *       }
 *   }
 * </pre>
 *
 * @param <T> Banner 实体类型，需包含 position/status/is_deleted/sort_order 等字段
 */
public abstract class AbstractBannerService<T> {

    private final BaseMapper<T> mapper;

    protected AbstractBannerService(BaseMapper<T> mapper) {
        this.mapper = mapper;
    }

    protected abstract T newInstance();

    /**
     * 在实例上设置 position/status，子类可重写以适配字段差异
     */
    protected void applyPositionQuery(QueryWrapper<T> qw, String position) {
        qw.eq("position", position)
          .eq("status", "active")
          .eq("is_deleted", false)
          .orderByDesc("sort_order")
          .last("limit 1");
    }

    public IPage<T> getBannerList(Page<T> page) {
        return mapper.selectPage(page, null);
    }

    public T createBanner(T banner) {
        mapper.insert(banner);
        return banner;
    }

    public T updateBanner(T banner) {
        mapper.updateById(banner);
        return banner;
    }

    public boolean deleteBanner(Long id) {
        return mapper.deleteById(id) > 0;
    }

    public T getBannerByPosition(String position) {
        QueryWrapper<T> qw = new QueryWrapper<>();
        applyPositionQuery(qw, position);
        return mapper.selectOne(qw);
    }

    public T getBannerById(Long id) {
        return mapper.selectById(id);
    }
}
