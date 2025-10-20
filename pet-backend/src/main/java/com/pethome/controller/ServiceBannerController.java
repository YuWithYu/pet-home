package com.pethome.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pethome.common.Result;
import com.pethome.entity.MedicalServiceBanner;
import com.pethome.service.MedicalServiceBannerService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/api/service-banners")
@Api(tags = "服务展示图管理")
public class ServiceBannerController {

    @Autowired
    private MedicalServiceBannerService medicalServiceBannerService;

    @GetMapping("/service-selection")
    @ApiOperation("获取选择服务下方的展示图")
    public Result<MedicalServiceBanner> getServiceSelectionBanner() {
        try {
            // 获取医疗服务展示图作为选择服务下方的展示图
            MedicalServiceBanner banner = medicalServiceBannerService.getBannerByPosition("medical-page-top");
            return Result.success(banner);
        } catch (Exception e) {
            return Result.error("获取服务展示图失败: " + e.getMessage());
        }
    }
}
