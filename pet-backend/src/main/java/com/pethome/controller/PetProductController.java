package com.pethome.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.common.Result;
import com.pethome.entity.PetProduct;
import com.pethome.service.PetProductService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pet-products")
@Api(tags = "宠物用品管理")
public class PetProductController {

    @Autowired
    private PetProductService petProductService;

    @GetMapping("/page")
    @ApiOperation("分页查询宠物用品")
    public Result<IPage<PetProduct>> getPetProductPage(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<PetProduct> page = new Page<>(pageNo, pageSize);
        IPage<PetProduct> result = petProductService.getPetProductList(page);
        return Result.success(result);
    }

    @PostMapping("/create")
    @ApiOperation("创建宠物用品")
    public Result<PetProduct> createPetProduct(@RequestBody PetProduct petProduct) {
        return Result.success(petProductService.createPetProduct(petProduct));
    }

    @PutMapping("/update")
    @ApiOperation("更新宠物用品")
    public Result<PetProduct> updatePetProduct(@RequestBody PetProduct petProduct) {
        return Result.success(petProductService.updatePetProduct(petProduct));
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除宠物用品")
    public Result<Boolean> deletePetProduct(@PathVariable Long id) {
        return Result.success(petProductService.deletePetProduct(id));
    }

    @GetMapping("/{id}")
    @ApiOperation("获取宠物用品详情")
    public Result<PetProduct> getPetProductDetail(@PathVariable Long id) {
        return Result.success(petProductService.getPetProductById(id));
    }
}


