package com.pethome.controller.common;

import com.pethome.common.Result;
import com.pethome.entity.ChinaRegion;
import com.pethome.service.common.ChinaRegionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/regions")
@Tag(name = "地区管理")
public class ChinaRegionController {
    
    @Autowired
    private ChinaRegionService chinaRegionService;
    
    @GetMapping("/provinces")
    @Operation(summary = "获取所有省份")
    public Result<List<ChinaRegion>> getProvinces() {
        List<ChinaRegion> provinces = chinaRegionService.getProvinces();
        return Result.success(provinces);
    }
    
    @GetMapping("/cities")
    @Operation(summary = "根据省份代码获取城市")
    public Result<List<ChinaRegion>> getCities(@RequestParam String parentCode) {
        List<ChinaRegion> cities = chinaRegionService.getCitiesByParentCode(parentCode);
        return Result.success(cities);
    }
    
    @GetMapping("/districts")
    @Operation(summary = "根据城市代码获取区县")
    public Result<List<ChinaRegion>> getDistricts(@RequestParam String parentCode) {
        List<ChinaRegion> districts = chinaRegionService.getDistrictsByParentCode(parentCode);
        return Result.success(districts);
    }
    
    @GetMapping("/children")
    @Operation(summary = "根据父级代码获取子级地区")
    public Result<List<ChinaRegion>> getChildren(@RequestParam String parentCode) {
        List<ChinaRegion> children = chinaRegionService.getByParentCode(parentCode);
        return Result.success(children);
    }
    
    @GetMapping("/{code}")
    @Operation(summary = "根据代码获取地区信息")
    public Result<ChinaRegion> getByCode(@PathVariable String code) {
        ChinaRegion region = chinaRegionService.getByCode(code);
        return Result.success(region);
    }
}
