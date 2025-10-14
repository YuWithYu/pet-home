package com.pethome.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.common.Result;
import com.pethome.entity.Address;
import com.pethome.service.AddressService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/addresses")
@Api(tags = "地址管理")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @GetMapping("/page")
    @ApiOperation("分页查询地址")
    public Result<IPage<Address>> getAddressPage(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam Long userId) {
        Page<Address> page = new Page<>(pageNo, pageSize);
        IPage<Address> result = addressService.getAddressListByUserId(page, userId);
        return Result.success(result);
    }

    @PostMapping("/create")
    @ApiOperation("创建地址")
    public Result<Address> createAddress(@RequestBody Address address) {
        return Result.success(addressService.createAddress(address));
    }

    @PutMapping("/update")
    @ApiOperation("更新地址")
    public Result<Address> updateAddress(@RequestBody Address address) {
        return Result.success(addressService.updateAddress(address));
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除地址")
    public Result<Boolean> deleteAddress(@PathVariable Long id) {
        return Result.success(addressService.deleteAddress(id));
    }

    @GetMapping("/{id}")
    @ApiOperation("获取地址详情")
    public Result<Address> getAddressDetail(@PathVariable Long id) {
        return Result.success(addressService.getAddressById(id));
    }

    @PutMapping("/set-default")
    @ApiOperation("设置默认地址")
    public Result<Address> setDefaultAddress(@RequestParam Long userId, @RequestParam Long addressId) {
        return Result.success(addressService.setDefaultAddress(userId, addressId));
    }
}


