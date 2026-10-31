package com.pethome.controller.appointment;


import com.pethome.common.SafeCall;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.common.Result;
import com.pethome.entity.ServiceConfig;
import com.pethome.service.appointment.ServiceConfigService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/service-config")
@Tag(name = "服务配置管理")
public class ServiceConfigController {

    private static final Logger log = LoggerFactory.getLogger(ServiceConfigController.class);

    @Autowired
    private ServiceConfigService serviceConfigService;

    @GetMapping("/page")
    @Operation(summary = "分页查询服务配置")
    public Result<IPage<ServiceConfig>> getServiceConfigPage(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<ServiceConfig> page = new Page<>(pageNo, pageSize);
        IPage<ServiceConfig> result = serviceConfigService.getServiceConfigPage(page);
        return Result.success(result);
    }

    @GetMapping("/all")
    @Operation(summary = "获取所有服务配置")
    public Result<List<ServiceConfig>> getAllServiceConfigs() {
        List<ServiceConfig> list = serviceConfigService.getAllServiceConfigs();
        return Result.success(list);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取服务配置详情")
    public Result<ServiceConfig> getServiceConfigById(@PathVariable Long id) {
        ServiceConfig serviceConfig = serviceConfigService.getServiceConfigById(id);
        if (serviceConfig != null) {
            return Result.success(serviceConfig);
        }
        return Result.error("服务配置不存在");
    }

    @GetMapping("/type/{serviceType}")
    @Operation(summary = "根据服务类型获取配置")
    public Result<ServiceConfig> getServiceConfigByType(@PathVariable String serviceType) {
        ServiceConfig serviceConfig = serviceConfigService.getServiceConfigByType(serviceType);
        if (serviceConfig != null) {
            return Result.success(serviceConfig);
        }
        return Result.error("服务配置不存在");
    }

    @PostMapping("/create")
    @Operation(summary = "创建服务配置")
    public Result<ServiceConfig> createServiceConfig(@RequestBody ServiceConfig serviceConfig) {
        return com.pethome.common.SafeCall.of(() -> {
            ServiceConfig created = serviceConfigService.createServiceConfig(serviceConfig);
            return Result.success(created);
        }, "创建服务配置失败，请稍后重试", log);
    }

    @PutMapping("/update")
    @Operation(summary = "更新服务配置")
    public Result<ServiceConfig> updateServiceConfig(@RequestBody ServiceConfig serviceConfig) {
        return com.pethome.common.SafeCall.of(() -> {
            ServiceConfig updated = serviceConfigService.updateServiceConfig(serviceConfig);
            return Result.success(updated);
        }, "更新服务配置失败，请稍后重试", log);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除服务配置")
    public Result<Boolean> deleteServiceConfig(@PathVariable Long id) {
        return com.pethome.common.SafeCall.of(() -> {
            boolean success = serviceConfigService.deleteServiceConfig(id);
            return Result.success(success);
        }, "删除服务配置失败，请稍后重试", log);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新服务状态")
    public Result<Boolean> updateServiceStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        return com.pethome.common.SafeCall.of(() -> {
            boolean success = serviceConfigService.updateServiceStatus(id, status);
            return Result.success(success);
        }, "更新状态失败，请稍后重试", log);
    }
}

