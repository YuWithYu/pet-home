package com.pethome.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.common.Result;
import com.pethome.entity.Consultation;
import com.pethome.service.ConsultationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/consultations")
@Api(tags = "问诊咨询管理")
public class ConsultationController {

    @Autowired
    private ConsultationService consultationService;

    @GetMapping("/page")
    @ApiOperation("分页查询问诊咨询")
    public Result<IPage<Consultation>> getConsultationPage(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Consultation> page = new Page<>(pageNo, pageSize);
        IPage<Consultation> result = consultationService.getConsultationList(page);
        return Result.success(result);
    }

    @PostMapping("/create")
    @ApiOperation("创建问诊咨询")
    public Result<Consultation> createConsultation(@RequestBody Consultation consultation) {
        return Result.success(consultationService.createConsultation(consultation));
    }

    @PutMapping("/update")
    @ApiOperation("更新问诊咨询")
    public Result<Consultation> updateConsultation(@RequestBody Consultation consultation) {
        return Result.success(consultationService.updateConsultation(consultation));
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除问诊咨询")
    public Result<Boolean> deleteConsultation(@PathVariable Long id) {
        return Result.success(consultationService.deleteConsultation(id));
    }

    @GetMapping("/{id}")
    @ApiOperation("获取问诊咨询详情")
    public Result<Consultation> getConsultationDetail(@PathVariable Long id) {
        return Result.success(consultationService.getConsultationById(id));
    }
}


