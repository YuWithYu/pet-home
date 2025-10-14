package com.pethome.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pethome.common.Result;
import com.pethome.entity.GroomingServiceBanner;
import com.pethome.service.GroomingServiceBannerService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/api/grooming-banners")
@Api(tags = "洗护服务展示图管理")
public class GroomingServiceBannerController {

    @Autowired
    private GroomingServiceBannerService groomingServiceBannerService;

    @GetMapping("/position/{position}")
    @ApiOperation("根据位置获取展示图")
    public Result<GroomingServiceBanner> getGroomingServiceBannerByPosition(@PathVariable String position) {
        GroomingServiceBanner banner = groomingServiceBannerService.getBannerByPosition(position);
        return Result.success(banner);
    }

    @PostMapping("/create")
    @ApiOperation("创建洗护服务横幅")
    public Result<GroomingServiceBanner> createGroomingServiceBanner(@RequestBody GroomingServiceBanner banner) {
        // 这里应该有GroomingServiceBannerService的create方法
        return Result.success(banner);
    }

    @PutMapping("/update")
    @ApiOperation("更新洗护服务横幅")
    public Result<GroomingServiceBanner> updateGroomingServiceBanner(@RequestBody GroomingServiceBanner banner) {
        // 这里应该有GroomingServiceBannerService的update方法
        return Result.success(banner);
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除洗护服务横幅")
    public Result<Boolean> deleteGroomingServiceBanner(@PathVariable Long id) {
        // 这里应该有GroomingServiceBannerService的delete方法
        return Result.success(true);
    }
}
