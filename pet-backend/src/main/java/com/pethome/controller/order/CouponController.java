package com.pethome.controller.order;

import com.pethome.common.BusinessException;
import com.pethome.common.Result;
import com.pethome.entity.User;
import com.pethome.entity.UserCoupon;
import com.pethome.service.order.CouponService;
import com.pethome.service.order.UserCouponService;
import com.pethome.service.user.UserService;
import com.pethome.security.JwtUtil;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 优惠券用户端接口：领券中心 / 领取 / 我的卡包 / 下单可用券计算
 *
 * <p>安全约束：所有接口的 userId 一律从 token 解析，不信任前端传参。
 */
@Tag(name = "优惠券", description = "领券中心、我的卡包、下单选券")
@RestController
@RequestMapping("/api/coupons")
public class CouponController {

    private static final Logger log = LoggerFactory.getLogger(CouponController.class);

    @Autowired
    private CouponService couponService;

    @Autowired
    private UserCouponService userCouponService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/center")
    @Operation(summary = "领券中心：上架中且在领取窗口内的券（含当前用户可领状态）")
    public Result<List<Map<String, Object>>> center(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long userId = getUserIdFromToken(authHeader);
        return Result.success(couponService.listActiveForCenter(userId));
    }

    @PostMapping("/{couponId}/receive")
    @Operation(summary = "领取优惠券")
    public Result<Map<String, Object>> receive(
            @PathVariable Long couponId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long userId = requireUserId(authHeader);
        UserCoupon uc = userCouponService.receive(userId, couponId);
        Map<String, Object> data = Map.of(
                "id", uc.getId(),
                "validEndTime", uc.getValidEndTime());
        return Result.success("领取成功", data);
    }

    @GetMapping("/mine")
    @Operation(summary = "我的卡包（status: unused/used/expired，空=全部）")
    public Result<List<Map<String, Object>>> mine(
            @RequestParam(required = false) String status,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long userId = requireUserId(authHeader);
        return Result.success(userCouponService.myCoupons(userId, status));
    }

    @PostMapping("/usable")
    @Operation(summary = "计算下单可用券（传订单商品ID与后端计算的总金额，返回每张券可用性与优惠额）")
    public Result<List<Map<String, Object>>> usable(
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long userId = requireUserId(authHeader);
        List<Long> productIds = parseProductIds(body.get("productIds"));
        BigDecimal totalAmount = parseAmount(body.get("totalAmount"));
        return Result.success(userCouponService.listUsable(userId, productIds, totalAmount));
    }

    // ==================== 私有方法 ====================

    private Long requireUserId(String authHeader) {
        Long userId = getUserIdFromToken(authHeader);
        if (userId == null) {
            throw new BusinessException(401, "用户未登录");
        }
        return userId;
    }

    /** 从 Authorization 头解析当前登录用户 ID（Redis 缓存 → JWT subject 反查，与 SignInController 一致） */
    private Long getUserIdFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authHeader.substring(7);
        try {
            User cachedUser = userService.getUserByToken(token);
            if (cachedUser != null) {
                return cachedUser.getId();
            }
        } catch (Exception ignored) {
            // 继续走 JWT 解析
        }
        try {
            Claims claims = jwtUtil.parseToken(token);
            if (claims != null) {
                String subject = claims.getSubject();
                if (subject != null) {
                    User user = userService.getUserByUsername(subject);
                    if (user == null) {
                        user = userService.getUserByPhone(subject);
                    }
                    if (user == null && subject.matches("\\d+")) {
                        user = userService.getUserById(Long.parseLong(subject));
                    }
                    if (user != null) {
                        return user.getId();
                    }
                }
            }
        } catch (Exception e) {
            log.error("Token解析失败", e);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private List<Long> parseProductIds(Object raw) {
        if (!(raw instanceof List<?> list) || list.isEmpty()) {
            return List.of();
        }
        return ((List<Object>) list).stream()
                .map(o -> Long.parseLong(o.toString()))
                .toList();
    }

    private BigDecimal parseAmount(Object raw) {
        if (raw == null) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(raw.toString());
        } catch (NumberFormatException e) {
            throw new BusinessException(400, "金额格式错误");
        }
    }
}
