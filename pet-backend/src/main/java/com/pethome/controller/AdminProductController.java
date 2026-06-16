package com.pethome.controller;

import com.pethome.common.Result;
import com.pethome.service.ProductService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理后台 - 商品热门推荐管理
 */
@RestController
@RequestMapping("/api/admin/products")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
@Api(tags = "管理后台-商品热门推荐")
public class AdminProductController {

    @Autowired
    private ProductService productService;

    @PutMapping("/{id}/hot")
    @ApiOperation("设置/取消商品热门推荐")
    public Result<Boolean> setProductHot(
            @PathVariable Long id,
            @RequestParam Boolean isHot) {
        try {
            boolean success = productService.setProductHot(id, isHot);
            return success ? Result.success(true) : Result.error("商品不存在或操作失败");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("操作失败: " + e.getMessage());
        }
    }

    @PutMapping("/hot/batch")
    @ApiOperation("批量设置/取消热门推荐")
    public Result<Boolean> batchSetHotProducts(@RequestBody Map<String, Object> body) {
        try {
            @SuppressWarnings("unchecked")
            List<Number> ids = (List<Number>) body.get("productIds");
            if (ids == null || ids.isEmpty()) {
                return Result.error("请选择要操作的商品");
            }
            Boolean isHot = body.get("isHot") != null ? Boolean.valueOf(body.get("isHot").toString()) : false;
            List<Long> productIds = new java.util.ArrayList<>();
            for (Number n : ids) {
                productIds.add(n.longValue());
            }
            boolean success = productService.batchSetHotProducts(productIds, isHot);
            return success ? Result.success(true) : Result.error("批量操作失败");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("操作失败: " + e.getMessage());
        }
    }
}
