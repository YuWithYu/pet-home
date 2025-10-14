package com.pethome.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.common.Result;
import com.pethome.entity.Cart;
import com.pethome.service.CartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@Api(tags = "购物车管理")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping("/page")
    @ApiOperation("分页查询购物车")
    public Result<IPage<Cart>> getCartPage(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam Long userId) {
        Page<Cart> page = new Page<>(pageNo, pageSize);
        IPage<Cart> result = cartService.getCartListByUserId(page, userId);
        return Result.success(result);
    }

    @PostMapping("/add")
    @ApiOperation("添加到购物车")
    public Result<Cart> addToCart(@RequestBody Cart cart) {
        return Result.success(cartService.addToCart(cart));
    }

    @PutMapping("/update")
    @ApiOperation("更新购物车商品")
    public Result<Cart> updateCartItem(@RequestBody Cart cart) {
        return Result.success(cartService.updateCartItem(cart));
    }

    @DeleteMapping("/{id}")
    @ApiOperation("从购物车删除商品")
    public Result<Boolean> removeFromCart(@PathVariable Long id) {
        return Result.success(cartService.removeFromCart(id));
    }

    @GetMapping("/count")
    @ApiOperation("获取购物车商品数量")
    public Result<Integer> getCartCount(@RequestParam Long userId) {
        try {
            // 这里应该从数据库查询用户购物车商品数量
            return Result.success(0);
        } catch (Exception e) {
            return Result.error("获取购物车数量失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @ApiOperation("获取购物车商品详情")
    public Result<Cart> getCartItemDetail(@PathVariable Long id) {
        return Result.success(cartService.getCartItemById(id));
    }
}
