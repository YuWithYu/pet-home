package com.pethome.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.common.Result;
import com.pethome.entity.PetCheckup;
import com.pethome.service.PetCheckupService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pet-checkups")
@Api(tags = "宠物体检管理")
public class PetCheckupController {

    @Autowired
    private PetCheckupService petCheckupService;

    @GetMapping("/page")
    @ApiOperation("分页查询宠物体检")
    public Result<IPage<PetCheckup>> getPetCheckupPage(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<PetCheckup> page = new Page<>(pageNo, pageSize);
        IPage<PetCheckup> result = petCheckupService.getPetCheckupList(page);
        return Result.success(result);
    }

    @PostMapping("/create")
    @ApiOperation("创建宠物体检记录")
    public Result<PetCheckup> createPetCheckup(@RequestBody PetCheckup petCheckup) {
        return Result.success(petCheckupService.createPetCheckup(petCheckup));
    }

    @PutMapping("/update")
    @ApiOperation("更新宠物体检记录")
    public Result<PetCheckup> updatePetCheckup(@RequestBody PetCheckup petCheckup) {
        return Result.success(petCheckupService.updatePetCheckup(petCheckup));
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除宠物体检记录")
    public Result<Boolean> deletePetCheckup(@PathVariable Long id) {
        return Result.success(petCheckupService.deletePetCheckup(id));
    }

    @GetMapping("/{id}")
    @ApiOperation("获取宠物体检记录详情")
    public Result<PetCheckup> getPetCheckupDetail(@PathVariable Long id) {
        return Result.success(petCheckupService.getPetCheckupById(id));
    }
}


