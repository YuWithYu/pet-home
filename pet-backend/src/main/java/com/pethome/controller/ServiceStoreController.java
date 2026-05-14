package com.pethome.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.common.Result;
import com.pethome.entity.ServiceStore;
import com.pethome.service.ServiceStoreService;
import com.pethome.util.FileUploadUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 服务门店控制器
 */
@RestController
@RequestMapping("/api/stores")
@Api(tags = "服务门店管理")
public class ServiceStoreController {

    @Autowired
    private ServiceStoreService serviceStoreService;
    
    @Autowired
    private FileUploadUtil fileUploadUtil;

    @GetMapping("/page")
    @ApiOperation("分页查询门店")
    public Result<IPage<ServiceStore>> getStorePage(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String serviceType,
            @RequestParam(required = false) String status) {
        Page<ServiceStore> page = new Page<>(pageNo, pageSize);
        IPage<ServiceStore> result = serviceStoreService.getStorePage(page, serviceType, status);
        return Result.success(result);
    }

    @GetMapping("/all")
    @ApiOperation("获取所有营业中的门店")
    public Result<List<ServiceStore>> getAllActiveStores() {
        List<ServiceStore> stores = serviceStoreService.getAllActiveStores();
        return Result.success(stores);
    }

    @GetMapping("/by-service/{serviceType}")
    @ApiOperation("根据服务类型获取门店列表")
    public Result<List<ServiceStore>> getStoresByService(@PathVariable String serviceType) {
        List<ServiceStore> stores = serviceStoreService.getStoresByService(serviceType);
        return Result.success(stores);
    }

    @GetMapping("/{id}")
    @ApiOperation("获取门店详情")
    public Result<ServiceStore> getStoreById(@PathVariable Long id) {
        ServiceStore store = serviceStoreService.getStoreById(id);
        if (store != null) {
            return Result.success(store);
        }
        return Result.error("门店不存在");
    }

    @GetMapping("/default")
    @ApiOperation("获取默认门店")
    public Result<ServiceStore> getDefaultStore() {
        ServiceStore store = serviceStoreService.getDefaultStore();
        if (store != null) {
            return Result.success(store);
        }
        return Result.error("未设置默认门店");
    }

    @PostMapping("/create")
    @ApiOperation("创建门店")
    public Result<ServiceStore> createStore(@RequestBody ServiceStore store) {
        try {
            ServiceStore created = serviceStoreService.createStore(store);
            return Result.success(created);
        } catch (Exception e) {
            return Result.error("创建门店失败: " + e.getMessage());
        }
    }

    /** 将完整 URL 转为相对路径存库，便于小程序用配置的域名拼接 */
    private String normalizeImageUrlToRelative(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) return imageUrl;
        if (!imageUrl.startsWith("http://") && !imageUrl.startsWith("https://")) return imageUrl;
        try {
            int pathStart = imageUrl.indexOf("/", imageUrl.indexOf("://") + 3);
            if (pathStart > 0) {
                String path = imageUrl.substring(pathStart);
                return path.startsWith("/") ? path : "/" + path;
            }
        } catch (Exception ignored) {}
        return imageUrl;
    }

    @PutMapping("/update")
    @ApiOperation("更新门店")
    public Result<ServiceStore> updateStore(@RequestBody ServiceStore store) {
        try {
            if (store.getImageUrl() != null && !store.getImageUrl().isEmpty()) {
                store.setImageUrl(normalizeImageUrlToRelative(store.getImageUrl()));
            }
            ServiceStore updated = serviceStoreService.updateStore(store);
            return Result.success(updated);
        } catch (Exception e) {
            return Result.error("更新门店失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除门店")
    public Result<Boolean> deleteStore(@PathVariable Long id) {
        try {
            boolean success = serviceStoreService.deleteStore(id);
            return Result.success(success);
        } catch (Exception e) {
            return Result.error("删除门店失败: " + e.getMessage());
        }
    }

    @PostMapping("/upload")
    @ApiOperation("上传门店图片")
    public Result<String> uploadStoreImage(@RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = fileUploadUtil.uploadImage(file, "files");
            return Result.success(imageUrl);
        } catch (Exception e) {
            return Result.error("图片上传失败: " + e.getMessage());
        }
    }
}

