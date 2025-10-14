package com.pethome.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.common.Result;
import com.pethome.entity.MedicalService;
import com.pethome.service.MedicalServiceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/medical-services")
@Api(tags = "医疗服务管理")
public class MedicalServiceController {

    @Autowired
    private MedicalServiceService medicalServiceService;

    @GetMapping("/page")
    @ApiOperation("分页查询医疗服务")
    public Result<IPage<MedicalService>> getMedicalServicePage(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<MedicalService> page = new Page<>(pageNo, pageSize);
        IPage<MedicalService> result = medicalServiceService.getMedicalServiceList(page);
        return Result.success(result);
    }

    @PostMapping("/create")
    @ApiOperation("创建医疗服务")
    public Result<MedicalService> createMedicalService(@RequestBody MedicalService medicalService) {
        return Result.success(medicalServiceService.createMedicalService(medicalService));
    }

    @PutMapping("/update")
    @ApiOperation("更新医疗服务")
    public Result<MedicalService> updateMedicalService(@RequestBody MedicalService medicalService) {
        return Result.success(medicalServiceService.updateMedicalService(medicalService));
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除医疗服务")
    public Result<Boolean> deleteMedicalService(@PathVariable Long id) {
        return Result.success(medicalServiceService.deleteMedicalService(id));
    }

    @GetMapping("/{id}")
    @ApiOperation("获取医疗服务详情")
    public Result<MedicalService> getMedicalServiceDetail(@PathVariable Long id) {
        return Result.success(medicalServiceService.getMedicalServiceById(id));
    }
}
