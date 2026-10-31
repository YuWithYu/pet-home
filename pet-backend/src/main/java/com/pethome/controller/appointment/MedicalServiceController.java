package com.pethome.controller.appointment;


import com.pethome.common.SafeCall;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.common.Result;
import com.pethome.entity.MedicalService;
import com.pethome.service.appointment.MedicalServiceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/medical-services")
@Tag(name = "医疗服务管理")
public class MedicalServiceController {

    private static final Logger log = LoggerFactory.getLogger(MedicalServiceController.class);

    @Autowired
    private MedicalServiceService medicalServiceService;

    @GetMapping
    @Operation(summary = "获取医疗服务列表")
    public Result<java.util.List<MedicalService>> getMedicalServices() {
        return com.pethome.common.SafeCall.of(() -> {
            Page<MedicalService> page = new Page<>(1, 100); // 获取前100条记录
            IPage<MedicalService> result = medicalServiceService.getMedicalServiceList(page);
            return Result.success(result.getRecords());
        }, "获取医疗服务列表失败，请稍后重试", log);
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询医疗服务")
    public Result<IPage<MedicalService>> getMedicalServicePage(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<MedicalService> page = new Page<>(pageNo, pageSize);
        IPage<MedicalService> result = medicalServiceService.getMedicalServiceList(page);
        return Result.success(result);
    }

    @PostMapping("/create")
    @Operation(summary = "创建医疗服务")
    public Result<MedicalService> createMedicalService(@RequestBody MedicalService medicalService) {
        return com.pethome.common.SafeCall.of(() -> {
            MedicalService result = medicalServiceService.createMedicalService(medicalService);
            return Result.success(result);
        }, "创建医疗服务失败，请稍后重试", log);
    }

    @PutMapping("/update")
    @Operation(summary = "更新医疗服务")
    public Result<MedicalService> updateMedicalService(@RequestBody MedicalService medicalService) {
        return com.pethome.common.SafeCall.of(() -> {
            MedicalService result = medicalServiceService.updateMedicalService(medicalService);
            return Result.success(result);
        }, "更新医疗服务失败，请稍后重试", log);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除医疗服务")
    public Result<Boolean> deleteMedicalService(@PathVariable Long id) {
        return com.pethome.common.SafeCall.of(() -> {
            boolean result = medicalServiceService.deleteMedicalService(id);
            return Result.success(result);
        }, "删除医疗服务失败，请稍后重试", log);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取医疗服务详情")
    public Result<MedicalService> getMedicalServiceDetail(@PathVariable Long id) {
        return com.pethome.common.SafeCall.of(() -> {
            MedicalService result = medicalServiceService.getMedicalServiceById(id);
            return Result.success(result);
        }, "获取医疗服务详情失败，请稍后重试", log);
    }
}
