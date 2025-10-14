package com.pethome.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.common.Result;
import com.pethome.entity.PetToy;
import com.pethome.service.PetToyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pet-toys")
@Api(tags = "宠物玩具管理")
public class PetToyController {

    @Autowired
    private PetToyService petToyService;

    @GetMapping("/page")
    @ApiOperation("分页查询宠物玩具")
    public Result<IPage<PetToy>> getPetToyPage(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<PetToy> page = new Page<>(pageNo, pageSize);
        IPage<PetToy> result = petToyService.getPetToyList(page);
        return Result.success(result);
    }

    @PostMapping("/create")
    @ApiOperation("创建宠物玩具")
    public Result<PetToy> createPetToy(@RequestBody PetToy petToy) {
        return Result.success(petToyService.createPetToy(petToy));
    }

    @PutMapping("/update")
    @ApiOperation("更新宠物玩具")
    public Result<PetToy> updatePetToy(@RequestBody PetToy petToy) {
        return Result.success(petToyService.updatePetToy(petToy));
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除宠物玩具")
    public Result<Boolean> deletePetToy(@PathVariable Long id) {
        return Result.success(petToyService.deletePetToy(id));
    }

    @GetMapping("/{id}")
    @ApiOperation("获取宠物玩具详情")
    public Result<PetToy> getPetToyDetail(@PathVariable Long id) {
        return Result.success(petToyService.getPetToyById(id));
    }
}


