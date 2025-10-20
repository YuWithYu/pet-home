package com.pethome.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.ServiceConfig;
import com.pethome.mapper.ServiceConfigMapper;
import com.pethome.service.ServiceConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ServiceConfigServiceImpl implements ServiceConfigService {

    @Autowired
    private ServiceConfigMapper serviceConfigMapper;

    @Override
    public IPage<ServiceConfig> getServiceConfigPage(Page<ServiceConfig> page) {
        return serviceConfigMapper.selectPage(page, null);
    }

    @Override
    public List<ServiceConfig> getAllServiceConfigs() {
        QueryWrapper<ServiceConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByAsc("sort_order");
        return serviceConfigMapper.selectList(queryWrapper);
    }

    @Override
    public ServiceConfig getServiceConfigById(Long id) {
        return serviceConfigMapper.selectById(id);
    }

    @Override
    public ServiceConfig getServiceConfigByType(String serviceType) {
        QueryWrapper<ServiceConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("service_type", serviceType);
        return serviceConfigMapper.selectOne(queryWrapper);
    }

    @Override
    public ServiceConfig createServiceConfig(ServiceConfig serviceConfig) {
        serviceConfig.setCreateTime(LocalDateTime.now());
        serviceConfig.setUpdateTime(LocalDateTime.now());
        
        if (serviceConfig.getStatus() == null) {
            serviceConfig.setStatus(1); // 默认启用
        }
        
        serviceConfigMapper.insert(serviceConfig);
        return serviceConfig;
    }

    @Override
    public ServiceConfig updateServiceConfig(ServiceConfig serviceConfig) {
        serviceConfig.setUpdateTime(LocalDateTime.now());
        serviceConfigMapper.updateById(serviceConfig);
        return serviceConfig;
    }

    @Override
    public boolean deleteServiceConfig(Long id) {
        return serviceConfigMapper.deleteById(id) > 0;
    }

    @Override
    public boolean updateServiceStatus(Long id, Integer status) {
        ServiceConfig serviceConfig = serviceConfigMapper.selectById(id);
        if (serviceConfig != null) {
            serviceConfig.setStatus(status);
            serviceConfig.setUpdateTime(LocalDateTime.now());
            return serviceConfigMapper.updateById(serviceConfig) > 0;
        }
        return false;
    }
}

