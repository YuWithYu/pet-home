package com.pethome.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.common.Result;
import com.pethome.entity.PetVaccine;
import com.pethome.service.PetVaccineService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pet-vaccines")
@Api(tags = "宠物疫苗管理")
public class PetVaccineController {

    @Autowired
    private PetVaccineService petVaccineService;

    @GetMapping("/page")
    @ApiOperation("分页查询宠物疫苗")
    public Result<IPage<PetVaccine>> getPetVaccinePage(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<PetVaccine> page = new Page<>(pageNo, pageSize);
        IPage<PetVaccine> result = petVaccineService.getPetVaccineList(page);
        return Result.success(result);
    }

    @PostMapping("/create")
    @ApiOperation("创建宠物疫苗记录")
    public Result<PetVaccine> createPetVaccine(@RequestBody PetVaccine petVaccine) {
        return Result.success(petVaccineService.createPetVaccine(petVaccine));
    }

    @PutMapping("/update")
    @ApiOperation("更新宠物疫苗记录")
    public Result<PetVaccine> updatePetVaccine(@RequestBody PetVaccine petVaccine) {
        return Result.success(petVaccineService.updatePetVaccine(petVaccine));
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除宠物疫苗记录")
    public Result<Boolean> deletePetVaccine(@PathVariable Long id) {
        return Result.success(petVaccineService.deletePetVaccine(id));
    }

    @GetMapping("/{id}")
    @ApiOperation("获取宠物疫苗记录详情")
    public Result<PetVaccine> getPetVaccineDetail(@PathVariable Long id) {
        return Result.success(petVaccineService.getPetVaccineById(id));
    }
}


