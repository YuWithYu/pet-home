package com.pethome.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.common.Result;
import com.pethome.entity.GroomingService;
import com.pethome.service.GroomingServiceService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@RequestMapping("/api/grooming-services")
@Api(tags = "洗护服务管理")
@Validated
public class GroomingServiceController {

    @Autowired
    private GroomingServiceService groomingServiceService;

    @GetMapping("/page")
    @ApiOperation("分页查询洗护服务")
    public Result<IPage<GroomingService>> getGroomingServicePage(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNo,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer pageSize,
            @ApiParam("服务分类") @RequestParam(required = false) String category,
            @ApiParam("状态") @RequestParam(required = false) String status,
            @ApiParam("服务名称") @RequestParam(required = false) String name) {

        Page<GroomingService> page = new Page<>(pageNo, pageSize);
        IPage<GroomingService> result = groomingServiceService.getGroomingServicePage(page, category, status, name);
        return Result.success(result);
    }
}