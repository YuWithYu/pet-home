package com.pethome.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.common.Result;
import com.pethome.entity.PetHealth;
import com.pethome.service.PetHealthService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pet-health")
@Api(tags = "宠物保健品管理")
public class PetHealthController {

    @Autowired
    private PetHealthService petHealthService;

    @GetMapping("/page")
    @ApiOperation("分页查询宠物保健品")
    public Result<IPage<PetHealth>> getPetHealthPage(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<PetHealth> page = new Page<>(pageNo, pageSize);
        IPage<PetHealth> result = petHealthService.getPetHealthList(page);
        return Result.success(result);
    }

    @PostMapping("/create")
    @ApiOperation("创建宠物保健品")
    public Result<PetHealth> createPetHealth(@RequestBody PetHealth petHealth) {
        return Result.success(petHealthService.createPetHealth(petHealth));
    }

    @PutMapping("/update")
    @ApiOperation("更新宠物保健品")
    public Result<PetHealth> updatePetHealth(@RequestBody PetHealth petHealth) {
        return Result.success(petHealthService.updatePetHealth(petHealth));
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除宠物保健品")
    public Result<Boolean> deletePetHealth(@PathVariable Long id) {
        return Result.success(petHealthService.deletePetHealth(id));
    }

    @GetMapping("/{id}")
    @ApiOperation("获取宠物保健品详情")
    public Result<PetHealth> getPetHealthDetail(@PathVariable Long id) {
        return Result.success(petHealthService.getPetHealthById(id));
    }
}


