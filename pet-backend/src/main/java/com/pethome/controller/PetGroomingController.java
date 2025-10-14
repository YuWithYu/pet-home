package com.pethome.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.common.Result;
import com.pethome.entity.PetGrooming;
import com.pethome.service.PetGroomingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pet-grooming")
@Api(tags = "宠物美容用品管理")
public class PetGroomingController {

    @Autowired
    private PetGroomingService petGroomingService;

    @GetMapping("/page")
    @ApiOperation("分页查询宠物美容用品")
    public Result<IPage<PetGrooming>> getPetGroomingPage(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<PetGrooming> page = new Page<>(pageNo, pageSize);
        IPage<PetGrooming> result = petGroomingService.getPetGroomingList(page);
        return Result.success(result);
    }

    @PostMapping("/create")
    @ApiOperation("创建宠物美容用品")
    public Result<PetGrooming> createPetGrooming(@RequestBody PetGrooming petGrooming) {
        return Result.success(petGroomingService.createPetGrooming(petGrooming));
    }

    @PutMapping("/update")
    @ApiOperation("更新宠物美容用品")
    public Result<PetGrooming> updatePetGrooming(@RequestBody PetGrooming petGrooming) {
        return Result.success(petGroomingService.updatePetGrooming(petGrooming));
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除宠物美容用品")
    public Result<Boolean> deletePetGrooming(@PathVariable Long id) {
        return Result.success(petGroomingService.deletePetGrooming(id));
    }

    @GetMapping("/{id}")
    @ApiOperation("获取宠物美容用品详情")
    public Result<PetGrooming> getPetGroomingDetail(@PathVariable Long id) {
        return Result.success(petGroomingService.getPetGroomingById(id));
    }
}


