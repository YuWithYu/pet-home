package com.pethome.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.ServiceStore;
import com.pethome.mapper.ServiceStoreMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 服务门店服务类
 */
@Service
public class ServiceStoreService {

    @Autowired
    private ServiceStoreMapper serviceStoreMapper;

    /**
     * 分页查询门店
     */
    public IPage<ServiceStore> getStorePage(Page<ServiceStore> page, String serviceType, String status) {
        QueryWrapper<ServiceStore> wrapper = new QueryWrapper<>();
        wrapper.eq("is_deleted", 0);
        
        // 按服务类型筛选
        if (serviceType != null && !serviceType.isEmpty()) {
            wrapper.like("services", serviceType);
        }
        
        // 按状态筛选
        if (status != null && !status.isEmpty()) {
            wrapper.eq("status", status);
        }
        
        wrapper.orderByDesc("is_default");
        wrapper.orderByAsc("sort_order");
        wrapper.orderByDesc("created_at");
        
        return serviceStoreMapper.selectPage(page, wrapper);
    }

    /**
     * 获取所有营业中的门店
     */
    public List<ServiceStore> getAllActiveStores() {
        QueryWrapper<ServiceStore> wrapper = new QueryWrapper<>();
        wrapper.eq("is_deleted", 0);
        wrapper.eq("status", "active");
        wrapper.orderByDesc("is_default");
        wrapper.orderByAsc("sort_order");
        return serviceStoreMapper.selectList(wrapper);
    }

    /**
     * 根据服务类型获取门店列表
     */
    public List<ServiceStore> getStoresByService(String serviceType) {
        QueryWrapper<ServiceStore> wrapper = new QueryWrapper<>();
        wrapper.eq("is_deleted", 0);
        wrapper.eq("status", "active");
        wrapper.like("services", serviceType);
        wrapper.orderByDesc("is_default");
        wrapper.orderByAsc("sort_order");
        return serviceStoreMapper.selectList(wrapper);
    }

    /**
     * 获取门店详情
     */
    public ServiceStore getStoreById(Long id) {
        QueryWrapper<ServiceStore> wrapper = new QueryWrapper<>();
        wrapper.eq("id", id);
        wrapper.eq("is_deleted", 0);
        return serviceStoreMapper.selectOne(wrapper);
    }

    /**
     * 创建门店
     */
    public ServiceStore createStore(ServiceStore store) {
        store.setCreatedAt(LocalDateTime.now());
        store.setUpdatedAt(LocalDateTime.now());
        store.setIsDeleted(false);
        if (store.getStatus() == null) {
            store.setStatus("active");
        }
        serviceStoreMapper.insert(store);
        return store;
    }

    /**
     * 更新门店
     */
    public ServiceStore updateStore(ServiceStore store) {
        store.setUpdatedAt(LocalDateTime.now());
        serviceStoreMapper.updateById(store);
        return store;
    }

    /**
     * 删除门店（逻辑删除）
     */
    public boolean deleteStore(Long id) {
        ServiceStore store = new ServiceStore();
        store.setId(id);
        store.setIsDeleted(true);
        store.setUpdatedAt(LocalDateTime.now());
        return serviceStoreMapper.updateById(store) > 0;
    }

    /**
     * 获取默认门店
     */
    public ServiceStore getDefaultStore() {
        QueryWrapper<ServiceStore> wrapper = new QueryWrapper<>();
        wrapper.eq("is_deleted", 0);
        wrapper.eq("status", "active");
        wrapper.eq("is_default", 1);
        wrapper.last("LIMIT 1");
        return serviceStoreMapper.selectOne(wrapper);
    }
}

