package com.pethome.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.common.Result;
import com.pethome.entity.PetAdoption;
import com.pethome.service.PetAdoptionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pet-adoption")
@Api(tags = "宠物领养管理")
public class PetAdoptionController {

    @Autowired
    private PetAdoptionService petAdoptionService;

    @GetMapping("/page")
    @ApiOperation("分页查询宠物领养")
    public Result<IPage<PetAdoption>> getPetAdoptionPage(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<PetAdoption> page = new Page<>(pageNo, pageSize);
        IPage<PetAdoption> result = petAdoptionService.getPetAdoptionList(page);
        return Result.success(result);
    }

    @PostMapping("/create")
    @ApiOperation("创建宠物领养")
    public Result<PetAdoption> createPetAdoption(@RequestBody PetAdoption petAdoption) {
        return Result.success(petAdoptionService.createPetAdoption(petAdoption));
    }

    @PutMapping("/update")
    @ApiOperation("更新宠物领养")
    public Result<PetAdoption> updatePetAdoption(@RequestBody PetAdoption petAdoption) {
        return Result.success(petAdoptionService.updatePetAdoption(petAdoption));
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除宠物领养")
    public Result<Boolean> deletePetAdoption(@PathVariable Long id) {
        return Result.success(petAdoptionService.deletePetAdoption(id));
    }

    @GetMapping("/{id}")
    @ApiOperation("获取宠物领养详情")
    public Result<PetAdoption> getPetAdoptionDetail(@PathVariable Long id) {
        return Result.success(petAdoptionService.getPetAdoptionById(id));
    }
}
