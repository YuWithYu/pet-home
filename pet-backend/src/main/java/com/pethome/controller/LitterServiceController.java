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
}
