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
@RequestMapping("/api/boarding-services")
@Api(tags = "宠物寄养服务管理")
public class BoardingServiceController {

    @Autowired
    private LitterServiceService litterServiceService;

    @GetMapping("/page")
    @ApiOperation("分页查询寄养服务")
    public Result<IPage<LitterService>> getBoardingServicePage(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String status) {
        Page<LitterService> page = new Page<>(pageNo, pageSize);
        IPage<LitterService> result = litterServiceService.getLitterServiceList(page);
        return Result.success(result);
    }

    @PostMapping("/create")
    @ApiOperation("创建寄养服务")
    public Result<LitterService> createBoardingService(@RequestBody LitterService service) {
        return Result.success(litterServiceService.createLitterService(service));
    }

    @PutMapping("/update")
    @ApiOperation("更新寄养服务")
    public Result<LitterService> updateBoardingService(@RequestBody LitterService service) {
        return Result.success(litterServiceService.updateLitterService(service));
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除寄养服务")
    public Result<Boolean> deleteBoardingService(@PathVariable Long id) {
        return Result.success(litterServiceService.deleteLitterService(id));
    }

    @GetMapping("/{id}")
    @ApiOperation("获取寄养服务详情")
    public Result<LitterService> getBoardingServiceDetail(@PathVariable Long id) {
        return Result.success(litterServiceService.getLitterServiceById(id));
    }

    @PutMapping("/{id}/status")
    @ApiOperation("更新寄养服务状态")
    public Result<LitterService> updateBoardingServiceStatus(@PathVariable Long id, @RequestParam String status) {
        LitterService service = litterServiceService.getLitterServiceById(id);
        if (service != null) {
            service.setStatus(status);
            return Result.success(litterServiceService.updateLitterService(service));
        }
        return Result.error("服务不存在");
    }

    @PostMapping("/upload")
    @ApiOperation("上传寄养服务图片")
    public Result<String> uploadBoardingServiceImage(@RequestParam("file") MultipartFile file) {
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
            String filename = "boarding-service-" + System.currentTimeMillis() + extension;
            
            // 上传目录
            String uploadDir = "C:/Users/Yu/Desktop/pet-home/upload/boarding-service/";
            java.io.File dir = new java.io.File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            // 保存文件
            java.io.File targetFile = new java.io.File(dir, filename);
            file.transferTo(targetFile);
            
            // 返回图片URL
            String imageUrl = "/upload/boarding-service/" + filename;
            return Result.success(imageUrl);
        } catch (Exception e) {
            return Result.error("图片上传失败: " + e.getMessage());
        }
    }
}
