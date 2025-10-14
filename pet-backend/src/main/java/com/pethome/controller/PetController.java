package com.pethome.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.common.Result;
import com.pethome.entity.Pet;
import com.pethome.service.PetService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pets")
@Api(tags = "宠物管理")
public class PetController {

    @Autowired
    private PetService petService;

    @GetMapping("/page")
    @ApiOperation("分页查询宠物")
    public Result<IPage<Pet>> getPetPage(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam Long userId) {
        Page<Pet> page = new Page<>(pageNo, pageSize);
        IPage<Pet> result = petService.getPetListByUserId(page, userId);
        return Result.success(result);
    }

    @PostMapping("/create")
    @ApiOperation("创建宠物")
    public Result<Pet> createPet(@RequestBody Pet pet) {
        return Result.success(petService.createPet(pet));
    }

    @PutMapping("/update")
    @ApiOperation("更新宠物")
    public Result<Pet> updatePet(@RequestBody Pet pet) {
        return Result.success(petService.updatePet(pet));
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除宠物")
    public Result<Boolean> deletePet(@PathVariable Long id) {
        return Result.success(petService.deletePet(id));
    }

    @GetMapping("/{id}")
    @ApiOperation("获取宠物详情")
    public Result<Pet> getPetDetail(@PathVariable Long id) {
        return Result.success(petService.getPetById(id));
    }
}
