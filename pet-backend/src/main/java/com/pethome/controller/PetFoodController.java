package com.pethome.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.common.Result;
import com.pethome.entity.PetFood;
import com.pethome.service.PetFoodService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pet-food")
@Api(tags = "宠物食品管理")
public class PetFoodController {

    @Autowired
    private PetFoodService petFoodService;

    @GetMapping("/page")
    @ApiOperation("分页查询宠物食品")
    public Result<IPage<PetFood>> getPetFoodPage(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<PetFood> page = new Page<>(pageNo, pageSize);
        IPage<PetFood> result = petFoodService.getPetFoodList(page);
        return Result.success(result);
    }

    @PostMapping("/create")
    @ApiOperation("创建宠物食品")
    public Result<PetFood> createPetFood(@RequestBody PetFood petFood) {
        return Result.success(petFoodService.createPetFood(petFood));
    }

    @PutMapping("/update")
    @ApiOperation("更新宠物食品")
    public Result<PetFood> updatePetFood(@RequestBody PetFood petFood) {
        return Result.success(petFoodService.updatePetFood(petFood));
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除宠物食品")
    public Result<Boolean> deletePetFood(@PathVariable Long id) {
        return Result.success(petFoodService.deletePetFood(id));
    }

    @GetMapping("/{id}")
    @ApiOperation("获取宠物食品详情")
    public Result<PetFood> getPetFoodDetail(@PathVariable Long id) {
        return Result.success(petFoodService.getPetFoodById(id));
    }
}


