package com.pethome.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.common.Result;
import com.pethome.entity.PetBoarding;
import com.pethome.service.PetBoardingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pet-boarding")
@Api(tags = "宠物寄养管理")
public class PetBoardingController {

    @Autowired
    private PetBoardingService petBoardingService;

    @GetMapping("/page")
    @ApiOperation("分页查询寄养订单")
    public Result<IPage<PetBoarding>> getPetBoardingPage(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<PetBoarding> page = new Page<>(pageNo, pageSize);
        IPage<PetBoarding> result = petBoardingService.getPetBoardingList(page);
        return Result.success(result);
    }

    @PostMapping("/create")
    @ApiOperation("创建寄养订单")
    public Result<PetBoarding> createPetBoarding(@RequestBody PetBoarding petBoarding) {
        return Result.success(petBoardingService.createPetBoarding(petBoarding));
    }

    @PutMapping("/{id}/status")
    @ApiOperation("更新寄养状态")
    public Result<PetBoarding> updatePetBoardingStatus(@PathVariable Long id, @RequestParam String status) {
        return Result.success(petBoardingService.updatePetBoardingStatus(id, status));
    }

    @GetMapping("/{id}")
    @ApiOperation("获取寄养详情")
    public Result<PetBoarding> getPetBoardingDetail(@PathVariable Long id) {
        return Result.success(petBoardingService.getPetBoardingById(id));
    }
}
