package com.pethome.controller;

import com.pethome.entity.PetAdoption;
import com.pethome.service.PetAdoptionService;
import com.pethome.common.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pet-adoption")
@Api(tags = "待领养宠物管理")
public class PetAdoptionController {

    @Autowired
    private PetAdoptionService petAdoptionService;

    @GetMapping("/list")
    @ApiOperation("获取可领养宠物列表")
    public Result<List<PetAdoption>> getAvailablePets() {
        try {
            List<PetAdoption> pets = petAdoptionService.getAvailablePets();
            return Result.success(pets);
        } catch (Exception e) {
            return Result.error("获取宠物列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @ApiOperation("获取宠物详情")
    public Result<PetAdoption> getPetDetail(@PathVariable Long id) {
        try {
            PetAdoption pet = petAdoptionService.getPetById(id);
            if (pet != null) {
                return Result.success(pet);
            } else {
                return Result.error("宠物不存在");
            }
        } catch (Exception e) {
            return Result.error("获取宠物详情失败: " + e.getMessage());
        }
    }
}