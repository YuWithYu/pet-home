package com.pethome.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.common.Result;
import com.pethome.entity.LitterService;
import com.pethome.service.LitterServiceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/litter-services")
@Api(tags = "铲屎服务管理")
public class LitterServiceController {

    @Autowired
    private LitterServiceService litterServiceService;

    @GetMapping("/page")
    @ApiOperation("分页查询铲屎服务")
    public Result<IPage<LitterService>> getLitterServicePage(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<LitterService> page = new Page<>(pageNo, pageSize);
        IPage<LitterService> result = litterServiceService.getLitterServiceList(page);
        return Result.success(result);
    }

    @PostMapping("/create")
    @ApiOperation("创建铲屎服务")
    public Result<LitterService> createLitterService(@RequestBody LitterService litterService) {
        return Result.success(litterServiceService.createLitterService(litterService));
    }

    @PutMapping("/update")
    @ApiOperation("更新铲屎服务")
    public Result<LitterService> updateLitterService(@RequestBody LitterService litterService) {
        return Result.success(litterServiceService.updateLitterService(litterService));
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除铲屎服务")
    public Result<Boolean> deleteLitterService(@PathVariable Long id) {
        return Result.success(litterServiceService.deleteLitterService(id));
    }

    @GetMapping("/{id}")
    @ApiOperation("获取铲屎服务详情")
    public Result<LitterService> getLitterServiceDetail(@PathVariable Long id) {
        return Result.success(litterServiceService.getLitterServiceById(id));
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
            String filename = "litter-service-" + id + "-" + System.currentTimeMillis() + extension;
            
            // 上传目录
            String uploadDir = "C:/Users/Yu/Desktop/pet-home/upload/grooming-service/";
            java.io.File dir = new java.io.File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            // 保存文件
            java.io.File targetFile = new java.io.File(dir, filename);
            file.transferTo(targetFile);
            
            // 返回图片URL
            String imageUrl = "/upload/grooming-service/" + filename;
            
            // 更新服务记录
            LitterService service = litterServiceService.getLitterServiceById(id);
            if (service != null) {
                service.setImageUrl(imageUrl);
                litterServiceService.updateLitterService(service);
                return Result.success(imageUrl);
            } else {
                return Result.error("服务不存在");
            }
        } catch (Exception e) {
            return Result.error("图片上传失败: " + e.getMessage());
        }
    }
}
