package com.pethome.controller;

import com.pethome.common.Result;
import com.pethome.entity.MedicalServiceBanner;
import com.pethome.service.MedicalServiceBannerService;
import com.pethome.util.FileUploadUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

/**
 * 宠物医院服务展示图管理
 * 使用 MedicalServiceBanner（医院专用），与上门铲屎、宠物洗护展示图区分
 */
@RestController
@RequestMapping("/api/hospital-banners")
@Api(tags = "宠物医院服务展示图管理")
public class HospitalServiceBannerController {

    private static final String DEFAULT_POSITION = "hospital-page-top";

    @Autowired
    private MedicalServiceBannerService medicalServiceBannerService;

    @Autowired
    private FileUploadUtil fileUploadUtil;

    @GetMapping("/position/{position}")
    @ApiOperation("根据位置获取医院服务展示图")
    public Result<MedicalServiceBanner> getBannerByPosition(@PathVariable String position) {
        MedicalServiceBanner banner = medicalServiceBannerService.getBannerByPosition(position);
        // 兼容旧数据：若请求 hospital-page-top 无记录，则用 medical-page-top
        if (banner == null && "hospital-page-top".equals(position)) {
            banner = medicalServiceBannerService.getBannerByPosition("medical-page-top");
        }
        return Result.success(banner);
    }

    @PostMapping("/upload")
    @ApiOperation("上传医院服务展示图")
    public Result<String> uploadBanner(@RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = fileUploadUtil.uploadImage(file, "files");
            String filename = file.getOriginalFilename();

            MedicalServiceBanner banner = medicalServiceBannerService.getBannerByPosition(DEFAULT_POSITION);
            if (banner == null) {
                banner = new MedicalServiceBanner();
                banner.setPosition(DEFAULT_POSITION);
                banner.setCreatedAt(LocalDateTime.now());
                banner.setUpdatedAt(LocalDateTime.now());
                banner.setIsDeleted(false);
            }

            banner.setTitle(filename != null ? filename : "宠物医院服务展示图");
            banner.setDescription("宠物医院服务页面顶部展示图");
            banner.setImageUrl(imageUrl);
            banner.setStatus("active");
            banner.setSortOrder(1);
            banner.setUpdatedAt(LocalDateTime.now());

            if (banner.getId() == null) {
                medicalServiceBannerService.createMedicalServiceBanner(banner);
            } else {
                medicalServiceBannerService.updateMedicalServiceBanner(banner);
            }

            return Result.success(imageUrl);
        } catch (Exception e) {
            return Result.error("图片上传失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除医院服务展示图")
    public Result<Boolean> deleteBanner(@PathVariable Long id) {
        try {
            boolean success = medicalServiceBannerService.deleteMedicalServiceBanner(id);
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
