package com.pethome.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.common.Result;
import com.pethome.entity.Doctor;
import com.pethome.service.DoctorService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/doctors")
@Api(tags = "医师管理")
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    @GetMapping("/page")
    @ApiOperation("分页查询医师")
    public Result<IPage<Doctor>> getDoctorPage(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Doctor> page = new Page<>(pageNo, pageSize);
        IPage<Doctor> result = doctorService.getDoctorList(page);
        return Result.success(result);
    }

    @PostMapping("/create")
    @ApiOperation("创建医师")
    public Result<Doctor> createDoctor(@RequestBody Doctor doctor) {
        return Result.success(doctorService.createDoctor(doctor));
    }

    @PutMapping("/update")
    @ApiOperation("更新医师")
    public Result<Doctor> updateDoctor(@RequestBody Doctor doctor) {
        return Result.success(doctorService.updateDoctor(doctor));
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除医师")
    public Result<Boolean> deleteDoctor(@PathVariable Long id) {
        return Result.success(doctorService.deleteDoctor(id));
    }

    @GetMapping("/{id}")
    @ApiOperation("获取医师详情")
    public Result<Doctor> getDoctorDetail(@PathVariable Long id) {
        return Result.success(doctorService.getDoctorById(id));
    }
}
