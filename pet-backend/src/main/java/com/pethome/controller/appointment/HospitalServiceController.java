package com.pethome.controller.appointment;


import com.pethome.common.SafeCall;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.common.Result;
import com.pethome.entity.HospitalService;
import com.pethome.security.RequestIdentity;
import com.pethome.service.appointment.HospitalServiceService;
import com.pethome.util.FileUploadUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/hospital-services")
@Tag(name = "宠物医院服务管理")
public class HospitalServiceController {

    private static final Logger log = LoggerFactory.getLogger(HospitalServiceController.class);

    @Autowired
    private HospitalServiceService hospitalServiceService;

    @Autowired
    private FileUploadUtil fileUploadUtil;

    @GetMapping("/page")
    @Operation(summary = "分页查询宠物医院服务")
    public Result<IPage<HospitalService>> getHospitalServicePage(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String status) {

        Page<HospitalService> page = new Page<>(pageNo, pageSize);
        QueryWrapper<HospitalService> wrapper = new QueryWrapper<>();
        wrapper.eq("is_deleted", 0);
        if (StringUtils.hasText(name)) {
            wrapper.like("name", name.trim());
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq("status", status.trim());
        }
        wrapper.orderByAsc("sort_order").orderByDesc("created_at");

        IPage<HospitalService> result = hospitalServiceService.getHospitalServicePage(page, wrapper);
        return Result.success(result);
    }

    @GetMapping("/list")
    @Operation(summary = "获取宠物医院服务列表")
    public Result<List<HospitalService>> getHospitalServiceList() {
        List<HospitalService> list = hospitalServiceService.list(new QueryWrapper<HospitalService>()
                .eq("is_deleted", 0)
                .eq("status", "active")
                .orderByAsc("sort_order"));
        list.forEach(this::fillDerivedFields);
        return Result.success(list);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取宠物医院服务详情")
    public Result<HospitalService> getHospitalServiceById(@PathVariable Long id) {
        HospitalService service = hospitalServiceService.getHospitalServiceById(id);
        if (service != null) {
            fillDerivedFields(service);
            return Result.success(service);
        } else {
            return Result.error("服务不存在");
        }
    }

    @PostMapping("/create")
    @Operation(summary = "创建宠物医院服务")
    public Result<HospitalService> createHospitalService(@RequestBody HospitalService service,
                                                         HttpServletRequest request) {
        // 安全修复：医院服务与价格为平台级主数据，仅平台管理员可维护
        if (!RequestIdentity.isPlatformAdmin(request)) {
            return Result.error(403, "无权创建医院服务");
        }
        return com.pethome.common.SafeCall.of(() -> {
            if (service.getCreatedAt() == null) {
                service.setCreatedAt(LocalDateTime.now());
            }
            service.setUpdatedAt(LocalDateTime.now());
            HospitalService created = hospitalServiceService.createHospitalService(service);
            fillDerivedFields(created);
            return Result.success(created);
        }, "服务创建失败，请稍后重试", log);
    }

    @PutMapping("/update")
    @Operation(summary = "更新宠物医院服务")
    public Result<HospitalService> updateHospitalService(@RequestBody HospitalService service,
                                                         HttpServletRequest request) {
        if (!RequestIdentity.isPlatformAdmin(request)) {
            return Result.error(403, "无权修改医院服务");
        }
        return com.pethome.common.SafeCall.of(() -> {
            service.setUpdatedAt(LocalDateTime.now());
            HospitalService updated = hospitalServiceService.updateHospitalService(service);
            fillDerivedFields(updated);
            return Result.success(updated);
        }, "服务更新失败，请稍后重试", log);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除宠物医院服务")
    public Result<Boolean> deleteHospitalService(@PathVariable Long id, HttpServletRequest request) {
        if (!RequestIdentity.isPlatformAdmin(request)) {
            return Result.error(403, "无权删除医院服务");
        }
        return com.pethome.common.SafeCall.of(() -> {
            boolean success = hospitalServiceService.deleteHospitalService(id);
            if (success) {
                return Result.success("服务删除成功", true);
            } else {
                return Result.error("服务删除失败");
            }
        }, "服务删除失败，请稍后重试", log);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新宠物医院服务状态")
    public Result<HospitalService> updateHospitalServiceStatus(@PathVariable Long id, @RequestParam String status,
                                                               HttpServletRequest request) {
        if (!RequestIdentity.isPlatformAdmin(request)) {
            return Result.error(403, "无权修改医院服务状态");
        }
        return com.pethome.common.SafeCall.of(() -> {
            HospitalService service = hospitalServiceService.getHospitalServiceById(id);
            if (service == null) {
                return Result.error("服务不存在");
            }
            service.setStatus(status);
            service.setUpdatedAt(LocalDateTime.now());
            HospitalService updated = hospitalServiceService.updateHospitalService(service);
            fillDerivedFields(updated);
            return Result.success(updated);
        }, "状态更新失败，请稍后重试", log);
    }

    @PostMapping("/upload")
    @Operation(summary = "上传医院服务图片")
    public Result<String> uploadServiceImage(@RequestParam("file") MultipartFile file,
                                             @RequestParam(value = "id", required = false) Long id,
                                             HttpServletRequest request) {
        if (!RequestIdentity.isPlatformAdmin(request)) {
            return Result.error(403, "无权上传医院服务图片");
        }
        return com.pethome.common.SafeCall.of(() -> {
            String imageUrl = fileUploadUtil.uploadImage(file, "files");
            if (id != null) {
                HospitalService service = hospitalServiceService.getHospitalServiceById(id);
                if (service != null) {
                    service.setImageUrl(imageUrl);
                    service.setUpdatedAt(LocalDateTime.now());
                    hospitalServiceService.updateHospitalService(service);
                }
            }
            return Result.success(imageUrl);
        }, "图片上传失败，请稍后重试", log);
    }

    @PostMapping("/{id}/image")
    @Operation(summary = "更新医院服务图片")
    public Result<String> updateServiceImage(@PathVariable Long id, @RequestParam("file") MultipartFile file,
                                             HttpServletRequest request) {
        if (!RequestIdentity.isPlatformAdmin(request)) {
            return Result.error(403, "无权修改医院服务图片");
        }
        return com.pethome.common.SafeCall.of(() -> {
            String imageUrl = fileUploadUtil.uploadImage(file, "files");
            HospitalService service = hospitalServiceService.getHospitalServiceById(id);
            if (service == null) {
                return Result.error("服务不存在");
            }
            service.setImageUrl(imageUrl);
            service.setUpdatedAt(LocalDateTime.now());
            hospitalServiceService.updateHospitalService(service);
            return Result.success(imageUrl);
        }, "图片上传失败，请稍后重试", log);
    }

    private void fillDerivedFields(HospitalService service) {
        if (service == null) {
            return;
        }
        if (!StringUtils.hasText(service.getBgColor())) {
            service.setBgColor("#ffffff");
        }
    }
}
