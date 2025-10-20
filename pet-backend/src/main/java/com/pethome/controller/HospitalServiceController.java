package com.pethome.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.HospitalService;
import com.pethome.service.HospitalServiceService;
import com.pethome.common.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/hospital-services")
@Api(tags = "宠物医院服务管理")
public class HospitalServiceController {

    @Autowired
    private HospitalServiceService hospitalServiceService;

    @GetMapping("/page")
    @ApiOperation("分页查询宠物医院服务")
    public Result<IPage<HospitalService>> getHospitalServicePage(
            @ApiParam("当前页") @RequestParam(defaultValue = "1") Integer current,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer size,
            @ApiParam("状态") @RequestParam(required = false) Integer status) {
        
        Page<HospitalService> page = new Page<>(current, size);
        IPage<HospitalService> result = hospitalServiceService.page(page);
        return Result.success(result);
    }

    @GetMapping("/list")
    @ApiOperation("获取宠物医院服务列表")
    public Result<List<HospitalService>> getHospitalServiceList(
            @ApiParam("状态") @RequestParam(required = false) Integer status) {
        
        List<HospitalService> list = hospitalServiceService.list();
        return Result.success(list);
    }

    @GetMapping("/{id}")
    @ApiOperation("根据ID获取宠物医院服务详情")
    public Result<HospitalService> getHospitalServiceById(@PathVariable Long id) {
        HospitalService service = hospitalServiceService.getHospitalServiceById(id);
        if (service != null) {
            return Result.success(service);
        } else {
            return Result.error("服务不存在");
        }
    }

    @PostMapping("/create")
    @ApiOperation("创建宠物医院服务")
    public Result<HospitalService> createHospitalService(@RequestBody HospitalService service) {
        try {
            boolean success = hospitalServiceService.save(service);
            if (success) {
                return Result.success(service, "服务创建成功");
            } else {
                return Result.error("服务创建失败");
            }
        } catch (Exception e) {
            return Result.error("服务创建失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @ApiOperation("更新宠物医院服务")
    public Result<HospitalService> updateHospitalService(@PathVariable Long id, @RequestBody HospitalService service) {
        try {
            service.setId(id);
            boolean success = hospitalServiceService.updateById(service);
            if (success) {
                return Result.success(service, "服务更新成功");
            } else {
                return Result.error("服务更新失败");
            }
        } catch (Exception e) {
            return Result.error("服务更新失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除宠物医院服务")
    public Result<Boolean> deleteHospitalService(@PathVariable Long id) {
        try {
            boolean success = hospitalServiceService.removeById(id);
            if (success) {
                return Result.success(true, "服务删除成功");
            } else {
                return Result.error("服务删除失败");
            }
        } catch (Exception e) {
            return Result.error("服务删除失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    @ApiOperation("更新宠物医院服务状态")
    public Result<Boolean> updateHospitalServiceStatus(@PathVariable Long id, @RequestParam Integer status) {
        try {
            HospitalService service = hospitalServiceService.getById(id);
            if (service != null) {
                service.setStatus(status);
                boolean success = hospitalServiceService.updateById(service);
                if (success) {
                    return Result.success(true, "状态更新成功");
                } else {
                    return Result.error("状态更新失败");
                }
            } else {
                return Result.error("服务不存在");
            }
        } catch (Exception e) {
            return Result.error("状态更新失败: " + e.getMessage());
        }
    }

    @PostMapping("/upload")
    @ApiOperation("上传服务图片")
    public Result<String> uploadServiceImage(@RequestParam("file") MultipartFile file, @RequestParam("id") Long id) {
        return updateServiceImage(id, file);
    }

    @PutMapping("/{id}/image")
    @ApiOperation("更新服务图片")
    public Result<String> updateServiceImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return Result.error("请选择要上传的图片");
            }

            String contentType = file.getContentType();
            if (contentType == null || (!contentType.startsWith("image/"))) {
                return Result.error("只能上传图片文件");
            }

            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".jpg";
            String filename = "hospital-service-" + id + "-" + System.currentTimeMillis() + extension;

            String uploadDir = "C:/Users/Yu/Desktop/pet-home/upload/hospital-service/";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File targetFile = new File(dir, filename);
            file.transferTo(targetFile);

            String imageUrl = "/upload/hospital-service/" + filename;

            HospitalService service = hospitalServiceService.getHospitalServiceById(id);
            if (service != null) {
                service.setImageUrl(imageUrl);
                hospitalServiceService.updateHospitalService(service);
                return Result.success(imageUrl);
            } else {
                return Result.error("服务不存在");
            }
        } catch (IOException e) {
            return Result.error("图片上传失败: " + e.getMessage());
        }
    }
}
