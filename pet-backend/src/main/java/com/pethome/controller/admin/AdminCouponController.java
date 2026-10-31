package com.pethome.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.common.Result;
import com.pethome.entity.Coupon;
import com.pethome.service.order.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 优惠券管理端接口（/api/admin/** 仅 admin 角色可访问，由 RoleInterceptor 统一拦截；
 * 业务异常由 GlobalExceptionHandler 统一映射 code+message）
 */
@Tag(name = "优惠券管理", description = "优惠券的创建、上下架、发放统计")
@RestController
@RequestMapping("/api/admin/coupons")
public class AdminCouponController {

    @Autowired
    private CouponService couponService;

    @GetMapping("/list")
    @Operation(summary = "分页查询优惠券")
    public Result<IPage<Coupon>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        return Result.success(couponService.page(new Page<>(page, size), status, keyword));
    }

    @GetMapping("/{id}")
    @Operation(summary = "优惠券详情")
    public Result<Coupon> detail(@PathVariable Long id) {
        Coupon coupon = couponService.getById(id);
        if (coupon == null) {
            return Result.error(404, "优惠券不存在");
        }
        return Result.success(coupon);
    }

    @PostMapping("/create")
    @Operation(summary = "创建优惠券（默认草稿态）")
    public Result<Coupon> create(@RequestBody Coupon coupon) {
        return Result.success("创建成功", couponService.create(coupon));
    }

    @PutMapping("/update")
    @Operation(summary = "更新优惠券（已发放的券禁止修改金额规则）")
    public Result<Coupon> update(@RequestBody Coupon coupon) {
        return Result.success("更新成功", couponService.update(coupon));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "上架/下架（status: active/offline）")
    public Result<Boolean> changeStatus(@PathVariable Long id, @RequestParam String status) {
        couponService.changeStatus(id, status);
        return Result.success("操作成功", true);
    }

    @GetMapping("/{id}/stats")
    @Operation(summary = "发放统计：领用量/核销量/剩余量")
    public Result<Map<String, Object>> stats(@PathVariable Long id) {
        return Result.success(couponService.stats(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除优惠券（仅零领取记录的券可删，已发放的请下架）")
    public Result<Boolean> delete(@PathVariable Long id) {
        couponService.deleteIfUntouched(id);
        return Result.success("已删除", true);
    }
}
