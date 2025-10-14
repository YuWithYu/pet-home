package com.pethome.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.common.Result;
import com.pethome.entity.Product;
import com.pethome.service.ProductService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/product")
@Api(tags = "商品管理")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/page")
    @ApiOperation("分页查询商品")
    public Result<IPage<Product>> getProductPage(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Product> page = new Page<>(pageNo, pageSize);
        IPage<Product> result = productService.getProductPage(page);
        return Result.success(result);
    }

    @PostMapping("/create")
    @ApiOperation("创建商品")
    public Result<Product> createProduct(@RequestBody Product product) {
        return Result.success(product);
    }

    @PutMapping("/update")
    @ApiOperation("更新商品")
    public Result<Product> updateProduct(@RequestBody Product product) {
        return Result.success(product);
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除商品")
    public Result<Boolean> deleteProduct(@PathVariable Long id) {
        return Result.success(true);
    }

    @GetMapping("/hot")
    @ApiOperation("获取热门商品")
    public Result<java.util.List<Product>> getHotProducts(@RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(productService.getHotProducts(limit));
    }

    @GetMapping("/recommend")
    @ApiOperation("获取推荐商品")
    public Result<java.util.List<Product>> getRecommendProducts(@RequestParam(defaultValue = "4") Integer limit) {
        return Result.success(productService.getRecommendProducts(limit));
    }

    @GetMapping("/favorites/count")
    @ApiOperation("获取用户商品收藏数量")
    public Result<Integer> getFavoriteCount(@RequestParam Long userId) {

        return Result.success(0);
    }

    @GetMapping("/{id}")
    @ApiOperation("获取商品详情")
    public Result<Product> getProductDetail(@PathVariable Long id) {
        return Result.success(new Product());
    }
}
