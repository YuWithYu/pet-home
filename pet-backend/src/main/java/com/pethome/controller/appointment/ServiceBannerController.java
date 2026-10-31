package com.pethome.controller.appointment;


import com.pethome.common.SafeCall;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pethome.common.Result;
import com.pethome.entity.GroomingServiceBanner;
import com.pethome.entity.LitterServiceBanner;
import com.pethome.entity.MedicalServiceBanner;
import com.pethome.service.appointment.GroomingServiceBannerService;
import com.pethome.service.appointment.LitterServiceBannerService;
import com.pethome.service.appointment.MedicalServiceBannerService;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api")
@Tag(name = "服务展示图管理")
public class ServiceBannerController {

    private static final Logger log = LoggerFactory.getLogger(ServiceBannerController.class);

    @Autowired
    private MedicalServiceBannerService medicalServiceBannerService;

    @Autowired
    private GroomingServiceBannerService groomingServiceBannerService;

    @Autowired
    private LitterServiceBannerService litterServiceBannerService;

    @Value("${host:http://localhost}")
    private String serverHost;

    @Value("${server.port:8080}")
    private String serverPort;

    /**
     * 将相对路径转换为完整的图片URL
     */
    private String convertToFullUrl(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return "";
        }
        
        // 如果已经是完整URL，直接返回
        if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
            return imagePath;
        }
        
        // 确保路径格式正确
        // 如果路径以 /upload/ 或 /static/ 开头，直接拼接
        if (imagePath.startsWith("/upload/") || imagePath.startsWith("/static/")) {
            return serverHost + ":" + serverPort + imagePath;
        }
        
        // 如果路径以 / 开头但不是 /upload/ 或 /static/，直接拼接（不添加 /upload 前缀）
        if (imagePath.startsWith("/")) {
            return serverHost + ":" + serverPort + imagePath;
        }
        
        // 其他情况（如 product/xxx.jpg），添加 /upload/ 前缀
        return serverHost + ":" + serverPort + "/upload/" + imagePath;
    }

    /** 展示图对外字段（字段名沿用旧接口契约） */
    private Map<String, Object> bannerData(Long id, String title, String description,
                                           String imageUrl, String position, String status) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("title", title);
        data.put("description", description);
        data.put("imageUrl", convertToFullUrl(imageUrl));
        data.put("position", position);
        data.put("status", status);
        return data;
    }

    @GetMapping("/grooming-banner")
    @Operation(summary = "获取洗护服务展示图")
    public Result<Map<String, Object>> getGroomingServiceBanner() {
        return SafeCall.of(() -> {
            // 获取洗护服务页面顶部的展示图
            GroomingServiceBanner banner = groomingServiceBannerService.getBannerByPosition("grooming-page-top");
            Map<String, Object> data = banner == null ? null : bannerData(banner.getId(), banner.getTitle(),
                    banner.getDescription(), banner.getImageUrl(), banner.getPosition(), banner.getStatus());
            return Result.success(data);
        }, "获取洗护服务展示图失败，请稍后重试", log);
    }

    @GetMapping("/medical-banner")
    @Operation(summary = "获取宠物医院展示图")
    public Result<Map<String, Object>> getMedicalServiceBanner() {
        return SafeCall.of(() -> {
            // 获取宠物医院页面顶部的展示图
            MedicalServiceBanner banner = medicalServiceBannerService.getBannerByPosition("medical-page-top");
            Map<String, Object> data = banner == null ? null : bannerData(banner.getId(), banner.getTitle(),
                    banner.getDescription(), banner.getImageUrl(), banner.getPosition(), banner.getStatus());
            return Result.success(data);
        }, "获取宠物医院展示图失败，请稍后重试", log);
    }

    @GetMapping("/litter-banner")
    @Operation(summary = "获取铲屎服务展示图")
    public Result<Map<String, Object>> getLitterServiceBanner() {
        return SafeCall.of(() -> {
            // 获取铲屎服务页面顶部的展示图
            LitterServiceBanner banner = litterServiceBannerService.getBannerByPosition("litter-page-top");
            Map<String, Object> data = banner == null ? null : bannerData(banner.getId(), banner.getTitle(),
                    banner.getDescription(), banner.getImageUrl(), banner.getPosition(), banner.getStatus());
            return Result.success(data);
        }, "获取铲屎服务展示图失败，请稍后重试", log);
    }

    @GetMapping("/service-banners/service-selection")
    @Operation(summary = "获取选择服务下方的展示图")
    public Result<MedicalServiceBanner> getServiceSelectionBanner() {
        return SafeCall.of(() -> {
            // 获取医疗服务展示图作为选择服务下方的展示图
            MedicalServiceBanner banner = medicalServiceBannerService.getBannerByPosition("medical-page-top");
            return Result.success(banner);
        }, "获取服务展示图失败，请稍后重试", log);
    }
}
