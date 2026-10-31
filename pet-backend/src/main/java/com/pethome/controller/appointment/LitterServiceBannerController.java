package com.pethome.controller.appointment;


import com.pethome.common.SafeCall;
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
import com.pethome.service.appointment.LitterServiceBannerService;
import com.pethome.util.FileUploadUtil;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/litter-banners")
@Tag(name = "铲屎服务展示图管理")
public class LitterServiceBannerController {

    private static final Logger log = LoggerFactory.getLogger(LitterServiceBannerController.class);

    @Autowired
    private LitterServiceBannerService litterServiceBannerService;
    
    @Autowired
    private FileUploadUtil fileUploadUtil;

    @GetMapping("/position/{position}")
    @Operation(summary = "根据位置获取展示图")
    public Result<LitterServiceBanner> getLitterServiceBannerByPosition(@PathVariable String position) {
        LitterServiceBanner banner = litterServiceBannerService.getBannerByPosition(position);
        return Result.success(banner);
    }

    @PostMapping("/upload")
    @Operation(summary = "上传铲屎服务展示图")
    public Result<String> uploadLitterServiceBanner(@RequestParam("file") MultipartFile file) {
        return com.pethome.common.SafeCall.of(() -> {
            // 使用FileUploadUtil上传文件，自动返回HTTPS URL
            String imageUrl;
            try {
                imageUrl = fileUploadUtil.uploadImage(file, "files");
            } catch (java.io.IOException e) {
                log.error("上传展示图失败", e);
                return Result.error("图片上传失败，请稍后重试");
            }
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
        }, "图片上传失败，请稍后重试", log);
    }

    @PostMapping("/create")
    @Operation(summary = "创建铲屎服务横幅")
    public Result<LitterServiceBanner> createLitterServiceBanner(@RequestBody LitterServiceBanner banner) {
        return com.pethome.common.SafeCall.of(() -> {
            banner.setCreatedAt(java.time.LocalDateTime.now());
            banner.setUpdatedAt(java.time.LocalDateTime.now());
            banner.setIsDeleted(false);
            LitterServiceBanner createdBanner = litterServiceBannerService.createLitterServiceBanner(banner);
            return Result.success(createdBanner);
        }, "创建展示图失败，请稍后重试", log);
    }

    @PutMapping("/update")
    @Operation(summary = "更新铲屎服务横幅")
    public Result<LitterServiceBanner> updateLitterServiceBanner(@RequestBody LitterServiceBanner banner) {
        return com.pethome.common.SafeCall.of(() -> {
            banner.setUpdatedAt(java.time.LocalDateTime.now());
            LitterServiceBanner updatedBanner = litterServiceBannerService.updateLitterServiceBanner(banner);
            return Result.success(updatedBanner);
        }, "更新展示图失败，请稍后重试", log);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除铲屎服务横幅")
    public Result<Boolean> deleteLitterServiceBanner(@PathVariable Long id) {
        return com.pethome.common.SafeCall.of(() -> {
            boolean success = litterServiceBannerService.deleteLitterServiceBanner(id);
            if (success) {
                return Result.success(true);
            } else {
                return Result.error("删除展示图失败");
            }
        }, "删除展示图失败，请稍后重试", log);
    }
}
