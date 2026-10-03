package com.pethome.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.ServiceConfig;
import java.util.List;

public interface ServiceConfigService {
    IPage<ServiceConfig> getServiceConfigPage(Page<ServiceConfig> page);
    List<ServiceConfig> getAllServiceConfigs();
    ServiceConfig getServiceConfigById(Long id);
    ServiceConfig getServiceConfigByType(String serviceType);
    ServiceConfig createServiceConfig(ServiceConfig serviceConfig);
    ServiceConfig updateServiceConfig(ServiceConfig serviceConfig);
    boolean deleteServiceConfig(Long id);
    boolean updateServiceStatus(Long id, Integer status);
}

