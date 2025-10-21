package com.pethome.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.common.Result;
import com.pethome.entity.AdoptionService;
import com.pethome.service.AdoptionServiceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/adoption-services")
@Api(tags = "宠物领养服务管理")
public class AdoptionServiceController {

    @Autowired
    private AdoptionServiceService adoptionServiceService;

    @GetMapping("/page")
    @ApiOperation("分页查询领养服务")
    public Result<IPage<AdoptionService>> getAdoptionServicePage(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String status) {
        Page<AdoptionService> page = new Page<>(pageNo, pageSize);
        IPage<AdoptionService> result = adoptionServiceService.page(page);
        return Result.success(result);
    }

    @PostMapping("/create")
    @ApiOperation("创建领养服务")
    public Result<AdoptionService> createAdoptionService(@RequestBody AdoptionService service) {
        boolean success = adoptionServiceService.createAdoptionService(service);
        if (success) {
            return Result.success(service);
        } else {
            return Result.error("创建失败");
        }
    }

    @PutMapping("/update")
    @ApiOperation("更新领养服务")
    public Result<AdoptionService> updateAdoptionService(@RequestBody AdoptionService service) {
        boolean success = adoptionServiceService.updateAdoptionService(service);
        if (success) {
            return Result.success(service);
        } else {
            return Result.error("更新失败");
        }
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除领养服务")
    public Result<Boolean> deleteAdoptionService(@PathVariable Long id) {
        boolean success = adoptionServiceService.deleteAdoptionService(id);
        return Result.success(success);
    }

    @GetMapping("/{id}")
    @ApiOperation("获取领养服务详情")
    public Result<AdoptionService> getAdoptionServiceDetail(@PathVariable Long id) {
        AdoptionService service = adoptionServiceService.getAdoptionServiceById(id);
        if (service != null) {
            return Result.success(service);
        } else {
            return Result.error("服务不存在");
        }
    }

    @PutMapping("/{id}/status")
    @ApiOperation("更新领养服务状态")
    public Result<AdoptionService> updateAdoptionServiceStatus(@PathVariable Long id, @RequestParam String status) {
        boolean success = adoptionServiceService.updateAdoptionServiceStatus(id, status);
        if (success) {
            AdoptionService service = adoptionServiceService.getAdoptionServiceById(id);
            return Result.success(service);
        } else {
            return Result.error("状态更新失败");
        }
    }

    @PostMapping("/upload")
    @ApiOperation("上传领养服务图片")
    public Result<String> uploadAdoptionServiceImage(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return Result.error("请选择要上传的图片");
            }
            
            // 检查文件类型
            String contentType = file.getContentType();
            if (contentType == null || (!contentType.startsWith("image/"))) {
                return Result.error("只能上传图片文件");
            }
            
            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                : ".jpg";
            String filename = "adoption-service-" + System.currentTimeMillis() + extension;
            
            // 上传目录
            String uploadDir = "C:/Users/Yu/Desktop/pet-home/upload/adoption-service/";
            java.io.File dir = new java.io.File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            // 保存文件
            java.io.File targetFile = new java.io.File(dir, filename);
            file.transferTo(targetFile);
            
            // 返回图片URL
            String imageUrl = "/upload/adoption-service/" + filename;
            return Result.success(imageUrl);
        } catch (Exception e) {
            return Result.error("图片上传失败: " + e.getMessage());
        }
    }
}
