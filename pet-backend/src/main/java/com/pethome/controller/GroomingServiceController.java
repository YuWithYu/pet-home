package com.pethome.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.common.Result;
import com.pethome.entity.GroomingService;
import com.pethome.service.GroomingServiceService;
import com.pethome.util.FileUploadUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/grooming-services")
@Api(tags = "洗护服务管理")
public class GroomingServiceController {

    @Autowired
    private GroomingServiceService groomingServiceService;

    @Autowired
    private FileUploadUtil fileUploadUtil;

    @GetMapping("/page")
    @ApiOperation("分页查询洗护服务")
    public Result<IPage<GroomingService>> getGroomingServicePage(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNo,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer pageSize,
            @ApiParam("服务名称") @RequestParam(required = false) String name,
            @ApiParam("状态") @RequestParam(required = false) String status,
            @ApiParam("服务分类") @RequestParam(required = false) String category) {

        Page<GroomingService> page = new Page<>(pageNo, pageSize);
        QueryWrapper<GroomingService> wrapper = new QueryWrapper<>();
        wrapper.eq("is_deleted", 0);

        if (StringUtils.hasText(name)) {
            wrapper.like("name", name.trim());
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq("status", status.trim());
        }
        if (StringUtils.hasText(category)) {
            wrapper.eq("category", category.trim());
        }

        wrapper.orderByAsc("sort_order").orderByDesc("created_at");
        IPage<GroomingService> result = groomingServiceService.getGroomingServicePage(page, wrapper);
        return Result.success(result);
    }

    @GetMapping("/list")
    @ApiOperation("获取启用的洗护服务列表")
    public Result<List<GroomingService>> getActiveGroomingServices() {
        QueryWrapper<GroomingService> wrapper = new QueryWrapper<>();
        wrapper.eq("is_deleted", 0)
                .eq("status", "active")
                .orderByAsc("sort_order")
                .orderByDesc("created_at");
        List<GroomingService> services = groomingServiceService.list(wrapper);
        for (int i = 0; i < services.size(); i++) {
            GroomingService enriched = groomingServiceService.getGroomingServiceById(services.get(i).getId());
            if (enriched != null) {
                services.set(i, enriched);
            }
        }
        return Result.success(services);
    }

    @GetMapping("/{id}")
    @ApiOperation("根据ID获取洗护服务")
    public Result<GroomingService> getGroomingServiceById(@PathVariable Long id) {
        GroomingService service = groomingServiceService.getGroomingServiceById(id);
        if (service != null) {
            return Result.success(service);
        }
        return Result.error("服务不存在");
    }

    @PostMapping("/create")
    @ApiOperation("创建洗护服务")
    public Result<GroomingService> createGroomingService(@RequestBody GroomingService service) {
        try {
            GroomingService created = groomingServiceService.createGroomingService(service);
            return Result.success(created);
        } catch (Exception e) {
            return Result.error("服务创建失败: " + e.getMessage());
        }
    }

    @PutMapping("/update")
    @ApiOperation("更新洗护服务")
    public Result<GroomingService> updateGroomingService(@RequestBody GroomingService service) {
        if (service.getId() == null) {
            return Result.error("服务ID不能为空");
        }
        try {
            GroomingService updated = groomingServiceService.updateGroomingService(service);
            return Result.success(updated);
        } catch (Exception e) {
            return Result.error("服务更新失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除洗护服务")
    public Result<Boolean> deleteGroomingService(@PathVariable Long id) {
        try {
            boolean success = groomingServiceService.deleteGroomingService(id);
            if (success) {
                return Result.success("删除成功", true);
            }
            return Result.error("删除失败");
        } catch (Exception e) {
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    @ApiOperation("更新洗护服务状态")
    public Result<String> updateServiceStatus(@PathVariable Long id, @RequestParam String status) {
        GroomingService service = groomingServiceService.getGroomingServiceById(id);
        if (service == null) {
            return Result.error("服务不存在");
        }
        service.setStatus(status);
        groomingServiceService.updateGroomingService(service);
        return Result.success("状态更新成功");
    }

    @PostMapping("/upload")
    @ApiOperation("上传洗护服务图片")
    public Result<String> uploadServiceImage(@RequestParam("file") MultipartFile file,
                                             @RequestParam(value = "id", required = false) Long id) {
        try {
            String imageUrl = fileUploadUtil.uploadImage(file, "files");
            if (id != null) {
                GroomingService service = groomingServiceService.getGroomingServiceById(id);
                if (service != null) {
                    service.setImageUrl(imageUrl);
                    groomingServiceService.updateGroomingService(service);
                }
            }
            return Result.success(imageUrl);
        } catch (Exception e) {
            return Result.error("图片上传失败: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/image")
    @ApiOperation("更新洗护服务图片")
    public Result<String> updateServiceImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = fileUploadUtil.uploadImage(file, "files");
            GroomingService service = groomingServiceService.getGroomingServiceById(id);
            if (service == null) {
                return Result.error("服务不存在");
            }
            service.setImageUrl(imageUrl);
            groomingServiceService.updateGroomingService(service);
            return Result.success(imageUrl);
        } catch (Exception e) {
            return Result.error("图片上传失败: " + e.getMessage());
        }
    }
}