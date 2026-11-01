package com.pethome.controller.order;


import com.pethome.common.SafeCall;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.common.Result;
import com.pethome.entity.Cart;
import com.pethome.service.order.CartService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/cart")
@Tag(name = "购物车管理")
public class CartController {

    private static final Logger log = LoggerFactory.getLogger(CartController.class);

    @Autowired
    private CartService cartService;

    /**
     * 从登录态解析当前用户 ID（RoleInterceptor 对 Redis/JWT 两种 token 均已写入 userId attribute）。
     * 安全修复：不再从 query/body 接收 userId，避免越权查看/操作他人购物车。
     */
    private Long currentUserId(HttpServletRequest request) {
        Object attr = request.getAttribute("userId");
        return attr instanceof Number ? ((Number) attr).longValue() : null;
    }

    /** 管理端（admin/store_admin）上下文，可代客操作购物车；staff（店员）不参与购物车业务 */
    private boolean isAdminContext(HttpServletRequest request) {
        Object role = request.getAttribute("role");
        return "admin".equals(role) || "store_admin".equals(role);
    }

    /** 校验目标 userId 与登录身份一致；管理端放行，其余视为越权 */
    private boolean canAccess(HttpServletRequest request, Long targetUserId) {
        if (isAdminContext(request)) {
            return true;
        }
        Long uid = currentUserId(request);
        return uid != null && uid.equals(targetUserId);
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询购物车")
    public Result<IPage<Cart>> getCartPage(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam Long userId,
            HttpServletRequest request) {
        // 安全修复：归属校验——仅允许查询自己的购物车（管理端除外）
        if (!canAccess(request, userId)) {
            log.warn("购物车分页越权尝试: targetUserId={}, currentUserId={}", userId, currentUserId(request));
            return Result.error(403, "无权查看他人购物车");
        }
        Page<Cart> page = new Page<>(pageNo, pageSize);
        IPage<Cart> result = cartService.getCartListByUserId(page, userId);
        return Result.success(result);
    }

    @PostMapping("/add")
    @Operation(summary = "添加到购物车")
    public Result<Cart> addToCart(@RequestBody Cart cart, HttpServletRequest request) {
        // 安全修复：userId 一律以登录态为准，忽略前端传入值
        Long userId = currentUserId(request);
        if (userId == null) {
            return Result.error(401, "用户未登录或token无效");
        }
        cart.setUserId(userId);
        return Result.success(cartService.addToCart(cart));
    }

    @PutMapping("/update")
    @Operation(summary = "更新购物车商品")
    public Result<Cart> updateCartItem(@RequestBody Cart cart, HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return Result.error(401, "用户未登录或token无效");
        }
        // 安全修复：更新前校验该项归属当前用户
        if (cart.getId() != null) {
            Cart existing = cartService.getCartItemById(cart.getId());
            if (existing == null) {
                return Result.error(404, "购物车商品不存在");
            }
            if (!userId.equals(existing.getUserId())) {
                log.warn("购物车更新越权尝试: cartId={}, ownerId={}, currentUserId={}", cart.getId(), existing.getUserId(), userId);
                return Result.error(403, "无权操作他人购物车");
            }
        }
        cart.setUserId(userId);
        return Result.success(cartService.updateCartItem(cart));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "从购物车删除商品")
    public Result<Boolean> removeFromCart(@PathVariable Long id, HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return Result.error(401, "用户未登录或token无效");
        }
        // 安全修复：删除前校验该项归属当前用户，防止遍历 id 删除他人购物车
        Cart existing = cartService.getCartItemById(id);
        if (existing == null) {
            return Result.success(false);
        }
        if (!userId.equals(existing.getUserId())) {
            log.warn("购物车删除越权尝试: cartId={}, ownerId={}, currentUserId={}", id, existing.getUserId(), userId);
            return Result.error(403, "无权操作他人购物车");
        }
        return Result.success(cartService.removeFromCart(id));
    }

    @GetMapping("/list")
    @Operation(summary = "获取购物车列表（前端接口）")
    public Result<java.util.List<Cart>> getCartList(HttpServletRequest request) {
        // 安全修复：userId 只取登录态，不再接收 query 参数
        Long userId = currentUserId(request);
        if (userId == null) {
            return Result.error(401, "用户未登录或token无效");
        }
        java.util.List<Cart> cartItems = cartService.getCartListWithProductInfo(userId);

        // 不在此处拼接完整图片 URL，保持相对路径（如 /upload/xxx）返回，由前端 util.getImageUrl 按当前环境 base 拼接，避免真机/小程序中 localhost 图不显示

        return Result.success(cartItems);
    }

    @PostMapping("/delete")
    @Operation(summary = "删除购物车商品（支持批量删除）")
    public Result<Boolean> deleteCartItem(@RequestBody java.util.Map<String, Object> params, HttpServletRequest request) {
        return com.pethome.common.SafeCall.of(() -> {
            Long userId = currentUserId(request);
            if (userId == null) {
                return Result.error(401, "用户未登录或token无效");
            }
            Object idsObj = params.get("ids");
            if (idsObj == null) {
                return Result.error("ids参数不能为空");
            }

            // 支持单个ID或ID数组
            if (idsObj instanceof Number) {
                Long id = ((Number) idsObj).longValue();
                return Result.success(removeOwnedCartItem(id, userId));
            } else if (idsObj instanceof java.util.List) {
                @SuppressWarnings("unchecked")
                java.util.List<Number> ids = (java.util.List<Number>) idsObj;
                for (Number idNum : ids) {
                    // 安全修复：批量删除同样逐项校验归属
                    if (!removeOwnedCartItem(idNum.longValue(), userId)) {
                        return Result.error(403, "无权操作他人购物车");
                    }
                }
                return Result.success(true);
            } else {
                return Result.error("ids参数格式错误");
            }
        }, "删除失败，请稍后重试", log);
    }

    /** 删除指定购物车项，校验归属后删除；返回是否删除成功 */
    private boolean removeOwnedCartItem(Long id, Long userId) {
        Cart existing = cartService.getCartItemById(id);
        if (existing == null) {
            return true; // 已不存在视为成功
        }
        if (!userId.equals(existing.getUserId())) {
            log.warn("购物车批量删除越权尝试: cartId={}, ownerId={}, currentUserId={}", id, existing.getUserId(), userId);
            return false;
        }
        return cartService.removeFromCart(id);
    }

    @PostMapping("/update/quantity")
    @Operation(summary = "更新购物车商品数量")
    public Result<Cart> updateQuantity(@RequestBody java.util.Map<String, Object> params, HttpServletRequest request) {
        return com.pethome.common.SafeCall.of(() -> {
            Long userId = currentUserId(request);
            if (userId == null) {
                return Result.error(401, "用户未登录或token无效");
            }
            Object idObj = params.get("id");
            Object quantityObj = params.get("quantity");

            if (idObj == null || quantityObj == null) {
                return Result.error("id和quantity参数不能为空");
            }

            Long id = ((Number) idObj).longValue();
            Integer quantity = ((Number) quantityObj).intValue();

            // 查询购物车项
            Cart cart = cartService.getCartItemById(id);
            if (cart == null) {
                return Result.error("购物车商品不存在");
            }
            // 安全修复：归属校验
            if (!userId.equals(cart.getUserId())) {
                log.warn("购物车数量更新越权尝试: cartId={}, ownerId={}, currentUserId={}", id, cart.getUserId(), userId);
                return Result.error(403, "无权操作他人购物车");
            }

            cart.setQuantity(quantity);
            Cart updatedCart = cartService.updateCartItem(cart);
            return Result.success(updatedCart);
        }, "更新数量失败，请稍后重试", log);
    }

    @PostMapping("/clear")
    @Operation(summary = "清空购物车")
    public Result<Boolean> clearCart(HttpServletRequest request) {
        return com.pethome.common.SafeCall.of(() -> {
            // 安全修复：userId 只取登录态，不再接收 body 传入值
            Long userId = currentUserId(request);
            if (userId == null) {
                return Result.error(401, "用户未登录或token无效");
            }
            boolean success = cartService.clearUserCart(userId);
            return Result.success(success);
        }, "清空购物车失败，请稍后重试", log);
    }

    @GetMapping("/count")
    @Operation(summary = "获取购物车商品数量")
    public Result<Integer> getCartCount(@RequestParam Long userId, HttpServletRequest request) {
        // 安全修复：归属校验——仅允许查询自己的购物车数量（管理端除外）
        if (!canAccess(request, userId)) {
            log.warn("购物车数量越权尝试: targetUserId={}, currentUserId={}", userId, currentUserId(request));
            return Result.error(403, "无权查看他人购物车");
        }
        return com.pethome.common.SafeCall.of(() -> {
            Integer count = cartService.getCartCount(userId);
            return Result.success(count);
        }, "获取购物车数量失败，请稍后重试", log);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取购物车商品详情")
    public Result<Cart> getCartItemDetail(@PathVariable Long id, HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return Result.error(401, "用户未登录或token无效");
        }
        Cart cart = cartService.getCartItemById(id);
        if (cart == null) {
            return Result.success(null);
        }
        // 安全修复：归属校验
        if (!userId.equals(cart.getUserId()) && !isAdminContext(request)) {
            log.warn("购物车详情越权尝试: cartId={}, ownerId={}, currentUserId={}", id, cart.getUserId(), userId);
            return Result.error(403, "无权查看他人购物车");
        }
        return Result.success(cart);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "获取用户购物车列表")
    public Result<java.util.List<Cart>> getUserCartItems(@PathVariable Long userId, HttpServletRequest request) {
        // 安全修复：归属校验
        if (!canAccess(request, userId)) {
            log.warn("购物车列表越权尝试: targetUserId={}, currentUserId={}", userId, currentUserId(request));
            return Result.error(403, "无权查看他人购物车");
        }
        java.util.List<Cart> cartItems = cartService.getCartListWithProductInfo(userId);
        return Result.success(cartItems);
    }

    @DeleteMapping("/clear/{userId}")
    @Operation(summary = "清空用户购物车（兼容旧接口）")
    public Result<Boolean> clearCartByPath(@PathVariable Long userId, HttpServletRequest request) {
        // 安全修复：归属校验——仅允许清空自己的购物车（管理端除外）
        if (!canAccess(request, userId)) {
            log.warn("购物车清空越权尝试: targetUserId={}, currentUserId={}", userId, currentUserId(request));
            return Result.error(403, "无权操作他人购物车");
        }
        boolean success = cartService.clearUserCart(userId);
        return Result.success(success);
    }

    @PostMapping("/batch-add")
    @Operation(summary = "批量添加到购物车")
    public Result<java.util.List<Cart>> batchAddToCart(@RequestBody java.util.List<Cart> cartItems, HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return Result.error(401, "用户未登录或token无效");
        }
        // 安全修复：每项 userId 一律以登录态为准
        for (Cart item : cartItems) {
            item.setUserId(userId);
        }
        java.util.List<Cart> result = cartService.batchAddToCart(cartItems);
        return Result.success(result);
    }
}
