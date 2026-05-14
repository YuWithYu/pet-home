package com.pethome.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.common.Result;
import com.pethome.entity.HospitalService;
import com.pethome.service.HospitalServiceService;
import com.pethome.util.FileUploadUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/hospital-services")
@Api(tags = "宠物医院服务管理")
public class HospitalServiceController {

    @Autowired
    private HospitalServiceService hospitalServiceService;

    @Autowired
    private FileUploadUtil fileUploadUtil;

    @GetMapping("/page")
    @ApiOperation("分页查询宠物医院服务")
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
    @ApiOperation("获取宠物医院服务列表")
    public Result<List<HospitalService>> getHospitalServiceList() {
        List<HospitalService> list = hospitalServiceService.list(new QueryWrapper<HospitalService>()
                .eq("is_deleted", 0)
                .eq("status", "active")
                .orderByAsc("sort_order"));
        list.forEach(this::fillDerivedFields);
        return Result.success(list);
    }

    @GetMapping("/{id}")
    @ApiOperation("根据ID获取宠物医院服务详情")
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
    @ApiOperation("创建宠物医院服务")
    public Result<HospitalService> createHospitalService(@RequestBody HospitalService service) {
        try {
            if (service.getCreatedAt() == null) {
                service.setCreatedAt(LocalDateTime.now());
            }
            service.setUpdatedAt(LocalDateTime.now());
            HospitalService created = hospitalServiceService.createHospitalService(service);
            fillDerivedFields(created);
            return Result.success(created);
        } catch (Exception e) {
            return Result.error("服务创建失败: " + e.getMessage());
        }
    }

    @PutMapping("/update")
    @ApiOperation("更新宠物医院服务")
    public Result<HospitalService> updateHospitalService(@RequestBody HospitalService service) {
        try {
            service.setUpdatedAt(LocalDateTime.now());
            HospitalService updated = hospitalServiceService.updateHospitalService(service);
            fillDerivedFields(updated);
            return Result.success(updated);
        } catch (Exception e) {
            return Result.error("服务更新失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除宠物医院服务")
    public Result<Boolean> deleteHospitalService(@PathVariable Long id) {
        try {
            boolean success = hospitalServiceService.deleteHospitalService(id);
            if (success) {
                return Result.success("服务删除成功", true);
            } else {
                return Result.error("服务删除失败");
            }
        } catch (Exception e) {
            return Result.error("服务删除失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    @ApiOperation("更新宠物医院服务状态")
    public Result<HospitalService> updateHospitalServiceStatus(@PathVariable Long id, @RequestParam String status) {
        try {
            HospitalService service = hospitalServiceService.getHospitalServiceById(id);
            if (service == null) {
                return Result.error("服务不存在");
            }
            service.setStatus(status);
            service.setUpdatedAt(LocalDateTime.now());
            HospitalService updated = hospitalServiceService.updateHospitalService(service);
            fillDerivedFields(updated);
            return Result.success(updated);
        } catch (Exception e) {
            return Result.error("状态更新失败: " + e.getMessage());
        }
    }

    @PostMapping("/upload")
    @ApiOperation("上传医院服务图片")
    public Result<String> uploadServiceImage(@RequestParam("file") MultipartFile file,
                                             @RequestParam(value = "id", required = false) Long id) {
        try {
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
        } catch (Exception e) {
            return Result.error("图片上传失败: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/image")
    @ApiOperation("更新医院服务图片")
    public Result<String> updateServiceImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = fileUploadUtil.uploadImage(file, "files");
            HospitalService service = hospitalServiceService.getHospitalServiceById(id);
            if (service == null) {
                return Result.error("服务不存在");
            }
            service.setImageUrl(imageUrl);
            service.setUpdatedAt(LocalDateTime.now());
            hospitalServiceService.updateHospitalService(service);
            return Result.success(imageUrl);
        } catch (Exception e) {
            return Result.error("图片上传失败: " + e.getMessage());
        }
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
