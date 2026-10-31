package com.pethome.controller.admin;


import com.pethome.common.SafeCall;
import com.pethome.common.Result;
import com.pethome.service.order.ProductService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理后台 - 商品热门推荐管理
 */
@RestController
@RequestMapping("/api/admin/products")
@Tag(name = "管理后台-商品热门推荐")
public class AdminProductController {

    private static final Logger log = LoggerFactory.getLogger(AdminProductController.class);

    @Autowired
    private ProductService productService;

    @PutMapping("/{id}/hot")
    @Operation(summary = "设置/取消商品热门推荐")
    public Result<Boolean> setProductHot(
            @PathVariable Long id,
            @RequestParam Boolean isHot) {
        return com.pethome.common.SafeCall.of(() -> {
            boolean success = productService.setProductHot(id, isHot);
            return success ? Result.success(true) : Result.error("商品不存在或操作失败");
        }, "操作失败，请稍后重试", log);
    }

    @PutMapping("/hot/batch")
    @Operation(summary = "批量设置/取消热门推荐")
    public Result<Boolean> batchSetHotProducts(@RequestBody Map<String, Object> body) {
        return com.pethome.common.SafeCall.of(() -> {
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
        }, "操作失败，请稍后重试", log);
    }
}
