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

    @GetMapping
    @ApiOperation("获取医疗服务列表")
    public Result<java.util.List<MedicalService>> getMedicalServices() {
        try {
            Page<MedicalService> page = new Page<>(1, 100); // 获取前100条记录
            IPage<MedicalService> result = medicalServiceService.getMedicalServiceList(page);
            return Result.success(result.getRecords());
        } catch (Exception e) {
            return Result.error("获取医疗服务列表失败: " + e.getMessage());
        }
    }

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
        try {
            MedicalService result = medicalServiceService.createMedicalService(medicalService);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("创建医疗服务失败: " + e.getMessage());
        }
    }

    @PutMapping("/update")
    @ApiOperation("更新医疗服务")
    public Result<MedicalService> updateMedicalService(@RequestBody MedicalService medicalService) {
        try {
            MedicalService result = medicalServiceService.updateMedicalService(medicalService);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("更新医疗服务失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除医疗服务")
    public Result<Boolean> deleteMedicalService(@PathVariable Long id) {
        try {
            boolean result = medicalServiceService.deleteMedicalService(id);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("删除医疗服务失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @ApiOperation("获取医疗服务详情")
    public Result<MedicalService> getMedicalServiceDetail(@PathVariable Long id) {
        try {
            MedicalService result = medicalServiceService.getMedicalServiceById(id);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取医疗服务详情失败: " + e.getMessage());
        }
    }
}
