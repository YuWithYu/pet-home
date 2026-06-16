package com.pethome.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.pethome.common.Result;
import com.pethome.entity.GroomingServiceBanner;
import com.pethome.service.GroomingServiceBannerService;
import com.pethome.util.FileUploadUtil;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/api/grooming-banners")
@Api(tags = "洗护服务展示图管理")
public class GroomingServiceBannerController {

    @Autowired
    private GroomingServiceBannerService groomingServiceBannerService;
    
    @Autowired
    private FileUploadUtil fileUploadUtil;

    @GetMapping("/position/{position}")
    @ApiOperation("根据位置获取展示图")
    public Result<GroomingServiceBanner> getGroomingServiceBannerByPosition(@PathVariable String position) {
        GroomingServiceBanner banner = groomingServiceBannerService.getBannerByPosition(position);
        return Result.success(banner);
    }

    @PostMapping("/upload")
    @ApiOperation("上传洗护服务展示图")
    public Result<String> uploadGroomingServiceBanner(@RequestParam("file") MultipartFile file) {
        try {
            // 使用FileUploadUtil上传文件，自动返回HTTPS URL
            String imageUrl = fileUploadUtil.uploadImage(file, "files");
            String originalFilename = file.getOriginalFilename();
            
            // 创建或更新展示图记录到数据库
            String position = "grooming-page-top";
            GroomingServiceBanner existingBanner = groomingServiceBannerService.getBannerByPosition(position);
            
            if (existingBanner != null) {
                // 更新现有记录
                existingBanner.setImageUrl(imageUrl);
                existingBanner.setTitle(originalFilename != null ? originalFilename : "洗护服务展示图");
                existingBanner.setUpdatedAt(java.time.LocalDateTime.now());
                groomingServiceBannerService.updateGroomingServiceBanner(existingBanner);
            } else {
                // 创建新记录
                GroomingServiceBanner newBanner = new GroomingServiceBanner();
                newBanner.setTitle(originalFilename != null ? originalFilename : "洗护服务展示图");
                newBanner.setDescription("洗护服务页面顶部展示图");
                newBanner.setImageUrl(imageUrl);
                newBanner.setPosition(position);
                newBanner.setStatus("active");
                newBanner.setSortOrder(1);
                newBanner.setCreatedAt(java.time.LocalDateTime.now());
                newBanner.setUpdatedAt(java.time.LocalDateTime.now());
                newBanner.setIsDeleted(false);
                groomingServiceBannerService.createGroomingServiceBanner(newBanner);
            }
            
            return Result.success(imageUrl);
        } catch (Exception e) {
            return Result.error("图片上传失败: " + e.getMessage());
        }
    }

    @PostMapping("/create")
    @ApiOperation("创建洗护服务横幅")
    public Result<GroomingServiceBanner> createGroomingServiceBanner(@RequestBody GroomingServiceBanner banner) {
        try {
            banner.setCreatedAt(java.time.LocalDateTime.now());
            banner.setUpdatedAt(java.time.LocalDateTime.now());
            banner.setIsDeleted(false);
            GroomingServiceBanner createdBanner = groomingServiceBannerService.createGroomingServiceBanner(banner);
            return Result.success(createdBanner);
        } catch (Exception e) {
            return Result.error("创建展示图失败: " + e.getMessage());
        }
    }

    @PutMapping("/update")
    @ApiOperation("更新洗护服务横幅")
    public Result<GroomingServiceBanner> updateGroomingServiceBanner(@RequestBody GroomingServiceBanner banner) {
        try {
            banner.setUpdatedAt(java.time.LocalDateTime.now());
            GroomingServiceBanner updatedBanner = groomingServiceBannerService.updateGroomingServiceBanner(banner);
            return Result.success(updatedBanner);
        } catch (Exception e) {
            return Result.error("更新展示图失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除洗护服务横幅")
    public Result<Boolean> deleteGroomingServiceBanner(@PathVariable Long id) {
        try {
            boolean success = groomingServiceBannerService.deleteGroomingServiceBanner(id);
            if (success) {
                return Result.success(true);
            } else {
                return Result.error("删除展示图失败");
            }
        } catch (Exception e) {
            return Result.error("删除展示图失败: " + e.getMessage());
        }
    }
}
