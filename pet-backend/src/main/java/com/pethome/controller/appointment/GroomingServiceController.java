package com.pethome.controller.appointment;


import com.pethome.common.SafeCall;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.common.Result;
import com.pethome.entity.GroomingService;
import com.pethome.service.appointment.GroomingServiceService;
import com.pethome.util.FileUploadUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/grooming-services")
@Tag(name = "洗护服务管理")
public class GroomingServiceController {

    private static final Logger log = LoggerFactory.getLogger(GroomingServiceController.class);

    @Autowired
    private GroomingServiceService groomingServiceService;

    @Autowired
    private FileUploadUtil fileUploadUtil;

    @GetMapping("/page")
    @Operation(summary = "分页查询洗护服务")
    public Result<IPage<GroomingService>> getGroomingServicePage(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNo,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "服务名称") @RequestParam(required = false) String name,
            @Parameter(description = "状态") @RequestParam(required = false) String status,
            @Parameter(description = "服务分类") @RequestParam(required = false) String category) {

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
    @Operation(summary = "获取启用的洗护服务列表")
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
    @Operation(summary = "根据ID获取洗护服务")
    public Result<GroomingService> getGroomingServiceById(@PathVariable Long id) {
        GroomingService service = groomingServiceService.getGroomingServiceById(id);
        if (service != null) {
            return Result.success(service);
        }
        return Result.error("服务不存在");
    }

    @PostMapping("/create")
    @Operation(summary = "创建洗护服务")
    public Result<GroomingService> createGroomingService(@RequestBody GroomingService service) {
        return com.pethome.common.SafeCall.of(() -> {
            GroomingService created = groomingServiceService.createGroomingService(service);
            return Result.success(created);
        }, "服务创建失败，请稍后重试", log);
    }

    @PutMapping("/update")
    @Operation(summary = "更新洗护服务")
    public Result<GroomingService> updateGroomingService(@RequestBody GroomingService service) {
        if (service.getId() == null) {
            return Result.error("服务ID不能为空");
        }
        return com.pethome.common.SafeCall.of(() -> {
            GroomingService updated = groomingServiceService.updateGroomingService(service);
            return Result.success(updated);
        }, "服务更新失败，请稍后重试", log);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除洗护服务")
    public Result<Boolean> deleteGroomingService(@PathVariable Long id) {
        return com.pethome.common.SafeCall.of(() -> {
            boolean success = groomingServiceService.deleteGroomingService(id);
            if (success) {
                return Result.success("删除成功", true);
            }
            return Result.error("删除失败");
        }, "删除失败，请稍后重试", log);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新洗护服务状态")
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
    @Operation(summary = "上传洗护服务图片")
    public Result<String> uploadServiceImage(@RequestParam("file") MultipartFile file,
                                             @RequestParam(value = "id", required = false) Long id) {
        return com.pethome.common.SafeCall.of(() -> {
            String imageUrl = fileUploadUtil.uploadImage(file, "files");
            if (id != null) {
                GroomingService service = groomingServiceService.getGroomingServiceById(id);
                if (service != null) {
                    service.setImageUrl(imageUrl);
                    groomingServiceService.updateGroomingService(service);
                }
            }
            return Result.success(imageUrl);
        }, "图片上传失败，请稍后重试", log);
    }

    @PostMapping("/{id}/image")
    @Operation(summary = "更新洗护服务图片")
    public Result<String> updateServiceImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        return com.pethome.common.SafeCall.of(() -> {
            String imageUrl = fileUploadUtil.uploadImage(file, "files");
            GroomingService service = groomingServiceService.getGroomingServiceById(id);
            if (service == null) {
                return Result.error("服务不存在");
            }
            service.setImageUrl(imageUrl);
            groomingServiceService.updateGroomingService(service);
            return Result.success(imageUrl);
        }, "图片上传失败，请稍后重试", log);
    }
}