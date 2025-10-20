package com.pethome.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.common.Result;
import com.pethome.entity.ServiceConfig;
import com.pethome.service.ServiceConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/service-config")
@Api(tags = "服务配置管理")
public class ServiceConfigController {

    @Autowired
    private ServiceConfigService serviceConfigService;

    @GetMapping("/page")
    @ApiOperation("分页查询服务配置")
    public Result<IPage<ServiceConfig>> getServiceConfigPage(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<ServiceConfig> page = new Page<>(pageNo, pageSize);
        IPage<ServiceConfig> result = serviceConfigService.getServiceConfigPage(page);
        return Result.success(result);
    }

    @GetMapping("/all")
    @ApiOperation("获取所有服务配置")
    public Result<List<ServiceConfig>> getAllServiceConfigs() {
        List<ServiceConfig> list = serviceConfigService.getAllServiceConfigs();
        return Result.success(list);
    }

    @GetMapping("/{id}")
    @ApiOperation("获取服务配置详情")
    public Result<ServiceConfig> getServiceConfigById(@PathVariable Long id) {
        ServiceConfig serviceConfig = serviceConfigService.getServiceConfigById(id);
        if (serviceConfig != null) {
            return Result.success(serviceConfig);
        }
        return Result.error("服务配置不存在");
    }

    @GetMapping("/type/{serviceType}")
    @ApiOperation("根据服务类型获取配置")
    public Result<ServiceConfig> getServiceConfigByType(@PathVariable String serviceType) {
        ServiceConfig serviceConfig = serviceConfigService.getServiceConfigByType(serviceType);
        if (serviceConfig != null) {
            return Result.success(serviceConfig);
        }
        return Result.error("服务配置不存在");
    }

    @PostMapping("/create")
    @ApiOperation("创建服务配置")
    public Result<ServiceConfig> createServiceConfig(@RequestBody ServiceConfig serviceConfig) {
        try {
            ServiceConfig created = serviceConfigService.createServiceConfig(serviceConfig);
            return Result.success(created);
        } catch (Exception e) {
            return Result.error("创建服务配置失败: " + e.getMessage());
        }
    }

    @PutMapping("/update")
    @ApiOperation("更新服务配置")
    public Result<ServiceConfig> updateServiceConfig(@RequestBody ServiceConfig serviceConfig) {
        try {
            ServiceConfig updated = serviceConfigService.updateServiceConfig(serviceConfig);
            return Result.success(updated);
        } catch (Exception e) {
            return Result.error("更新服务配置失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除服务配置")
    public Result<Boolean> deleteServiceConfig(@PathVariable Long id) {
        try {
            boolean success = serviceConfigService.deleteServiceConfig(id);
            return Result.success(success);
        } catch (Exception e) {
            return Result.error("删除服务配置失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    @ApiOperation("更新服务状态")
    public Result<Boolean> updateServiceStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        try {
            boolean success = serviceConfigService.updateServiceStatus(id, status);
            return Result.success(success);
        } catch (Exception e) {
            return Result.error("更新状态失败: " + e.getMessage());
        }
    }
}

