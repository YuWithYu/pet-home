package com.pethome.controller.appointment;


import com.pethome.common.SafeCall;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.common.Result;
import com.pethome.entity.ServiceConfig;
import com.pethome.security.RequestIdentity;
import com.pethome.service.appointment.ServiceConfigService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
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
    public Result<ServiceConfig> createServiceConfig(@RequestBody ServiceConfig serviceConfig,
                                                     HttpServletRequest request) {
        // 安全修复：服务配置为平台级主数据，仅平台管理员可维护（此前任意登录用户可写）
        if (!RequestIdentity.isPlatformAdmin(request)) {
            return Result.error(403, "无权创建服务配置");
        }
        return com.pethome.common.SafeCall.of(() -> {
            ServiceConfig created = serviceConfigService.createServiceConfig(serviceConfig);
            return Result.success(created);
        }, "创建服务配置失败，请稍后重试", log);
    }

    @PutMapping("/update")
    @Operation(summary = "更新服务配置")
    public Result<ServiceConfig> updateServiceConfig(@RequestBody ServiceConfig serviceConfig,
                                                     HttpServletRequest request) {
        if (!RequestIdentity.isPlatformAdmin(request)) {
            return Result.error(403, "无权修改服务配置");
        }
        return com.pethome.common.SafeCall.of(() -> {
            ServiceConfig updated = serviceConfigService.updateServiceConfig(serviceConfig);
            return Result.success(updated);
        }, "更新服务配置失败，请稍后重试", log);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除服务配置")
    public Result<Boolean> deleteServiceConfig(@PathVariable Long id, HttpServletRequest request) {
        if (!RequestIdentity.isPlatformAdmin(request)) {
            return Result.error(403, "无权删除服务配置");
        }
        return com.pethome.common.SafeCall.of(() -> {
            boolean success = serviceConfigService.deleteServiceConfig(id);
            return Result.success(success);
        }, "删除服务配置失败，请稍后重试", log);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新服务状态")
    public Result<Boolean> updateServiceStatus(
            @PathVariable Long id,
            @RequestParam Integer status,
            HttpServletRequest request) {
        if (!RequestIdentity.isPlatformAdmin(request)) {
            return Result.error(403, "无权修改服务状态");
        }
        return com.pethome.common.SafeCall.of(() -> {
            boolean success = serviceConfigService.updateServiceStatus(id, status);
            return Result.success(success);
        }, "更新状态失败，请稍后重试", log);
    }
}

