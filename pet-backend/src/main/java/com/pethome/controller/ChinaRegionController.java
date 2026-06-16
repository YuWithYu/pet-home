package com.pethome.controller;

import com.pethome.common.Result;
import com.pethome.entity.ChinaRegion;
import com.pethome.service.ChinaRegionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/regions")
@Api(tags = "地区管理")
public class ChinaRegionController {
    
    @Autowired
    private ChinaRegionService chinaRegionService;
    
    @GetMapping("/provinces")
    @ApiOperation("获取所有省份")
    public Result<List<ChinaRegion>> getProvinces() {
        List<ChinaRegion> provinces = chinaRegionService.getProvinces();
        return Result.success(provinces);
    }
    
    @GetMapping("/cities")
    @ApiOperation("根据省份代码获取城市")
    public Result<List<ChinaRegion>> getCities(@RequestParam String parentCode) {
        List<ChinaRegion> cities = chinaRegionService.getCitiesByParentCode(parentCode);
        return Result.success(cities);
    }
    
    @GetMapping("/districts")
    @ApiOperation("根据城市代码获取区县")
    public Result<List<ChinaRegion>> getDistricts(@RequestParam String parentCode) {
        List<ChinaRegion> districts = chinaRegionService.getDistrictsByParentCode(parentCode);
        return Result.success(districts);
    }
    
    @GetMapping("/children")
    @ApiOperation("根据父级代码获取子级地区")
    public Result<List<ChinaRegion>> getChildren(@RequestParam String parentCode) {
        List<ChinaRegion> children = chinaRegionService.getByParentCode(parentCode);
        return Result.success(children);
    }
    
    @GetMapping("/{code}")
    @ApiOperation("根据代码获取地区信息")
    public Result<ChinaRegion> getByCode(@PathVariable String code) {
        ChinaRegion region = chinaRegionService.getByCode(code);
        return Result.success(region);
    }
}
