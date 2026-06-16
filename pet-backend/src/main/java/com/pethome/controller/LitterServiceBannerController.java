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
import com.pethome.entity.LitterServiceBanner;
import com.pethome.service.LitterServiceBannerService;
import com.pethome.util.FileUploadUtil;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/api/litter-banners")
@Api(tags = "铲屎服务展示图管理")
public class LitterServiceBannerController {

    @Autowired
    private LitterServiceBannerService litterServiceBannerService;
    
    @Autowired
    private FileUploadUtil fileUploadUtil;

    @GetMapping("/position/{position}")
    @ApiOperation("根据位置获取展示图")
    public Result<LitterServiceBanner> getLitterServiceBannerByPosition(@PathVariable String position) {
        LitterServiceBanner banner = litterServiceBannerService.getBannerByPosition(position);
        return Result.success(banner);
    }

    @PostMapping("/upload")
    @ApiOperation("上传铲屎服务展示图")
    public Result<String> uploadLitterServiceBanner(@RequestParam("file") MultipartFile file) {
        try {
            // 使用FileUploadUtil上传文件，自动返回HTTPS URL
            String imageUrl = fileUploadUtil.uploadImage(file, "files");
            String originalFilename = file.getOriginalFilename();
            
            // 创建或更新展示图记录到数据库
            String position = "litter-page-top";
            LitterServiceBanner existingBanner = litterServiceBannerService.getBannerByPosition(position);
            
            if (existingBanner != null) {
                // 更新现有记录
                existingBanner.setImageUrl(imageUrl);
                existingBanner.setTitle(originalFilename != null ? originalFilename : "铲屎服务展示图");
                existingBanner.setUpdatedAt(java.time.LocalDateTime.now());
                litterServiceBannerService.updateLitterServiceBanner(existingBanner);
            } else {
                // 创建新记录
                LitterServiceBanner newBanner = new LitterServiceBanner();
                newBanner.setTitle(originalFilename != null ? originalFilename : "铲屎服务展示图");
                newBanner.setDescription("铲屎服务页面顶部展示图");
                newBanner.setImageUrl(imageUrl);
                newBanner.setPosition(position);
                newBanner.setStatus("active");
                newBanner.setSortOrder(1);
                newBanner.setCreatedAt(java.time.LocalDateTime.now());
                newBanner.setUpdatedAt(java.time.LocalDateTime.now());
                newBanner.setIsDeleted(false);
                litterServiceBannerService.createLitterServiceBanner(newBanner);
            }
            
            return Result.success(imageUrl);
        } catch (Exception e) {
            return Result.error("图片上传失败: " + e.getMessage());
        }
    }

    @PostMapping("/create")
    @ApiOperation("创建铲屎服务横幅")
    public Result<LitterServiceBanner> createLitterServiceBanner(@RequestBody LitterServiceBanner banner) {
        try {
            banner.setCreatedAt(java.time.LocalDateTime.now());
            banner.setUpdatedAt(java.time.LocalDateTime.now());
            banner.setIsDeleted(false);
            LitterServiceBanner createdBanner = litterServiceBannerService.createLitterServiceBanner(banner);
            return Result.success(createdBanner);
        } catch (Exception e) {
            return Result.error("创建展示图失败: " + e.getMessage());
        }
    }

    @PutMapping("/update")
    @ApiOperation("更新铲屎服务横幅")
    public Result<LitterServiceBanner> updateLitterServiceBanner(@RequestBody LitterServiceBanner banner) {
        try {
            banner.setUpdatedAt(java.time.LocalDateTime.now());
            LitterServiceBanner updatedBanner = litterServiceBannerService.updateLitterServiceBanner(banner);
            return Result.success(updatedBanner);
        } catch (Exception e) {
            return Result.error("更新展示图失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除铲屎服务横幅")
    public Result<Boolean> deleteLitterServiceBanner(@PathVariable Long id) {
        try {
            boolean success = litterServiceBannerService.deleteLitterServiceBanner(id);
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
