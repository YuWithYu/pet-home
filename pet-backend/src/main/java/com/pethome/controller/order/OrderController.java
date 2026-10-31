package com.pethome.controller.order;


import com.pethome.common.SafeCall;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pethome.common.BusinessException;
import com.pethome.common.Result;
import com.pethome.entity.Admin;
import com.pethome.entity.StoreWarehouse;
import com.pethome.service.order.LogisticsService;
import com.pethome.service.order.OrderCommandAppService;
import com.pethome.service.order.OrderLogisticsAppService;
import com.pethome.service.order.OrderQueryAppService;
import com.pethome.service.order.OrderRefundAppService;
import com.pethome.service.order.OrderWarehouseAppService;
import com.pethome.security.RequestIdentity;

import javax.servlet.http.HttpServletRequest;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

/**
 * 订单控制器
 */
@RestController
@RequestMapping("/api/orders")
@Tag(name = "订单管理")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderQueryAppService orderQueryAppService;

    @Autowired
    private OrderCommandAppService orderCommandAppService;

    @Autowired
    private OrderLogisticsAppService orderLogisticsAppService;

    @Autowired
    private LogisticsService logisticsService;

    @Autowired
    private OrderRefundAppService orderRefundAppService;
    
    @Autowired(required = false)
    private com.pethome.security.AdminContext adminContext;

    @Autowired(required = false)
    private com.pethome.security.UserTokenResolver userTokenResolver;

    @Autowired
    private OrderWarehouseAppService orderWarehouseAppService;

    /**
     * 从 token 中解析当前用户 ID（小程序/APP 端用，保证订单按用户隔离）
     * 实现已抽取至 UserTokenResolver（与 PaymentController 等共用，避免多处维护同一解析链）
     */
    private Long getUserIdFromToken(String authHeader) {
        if (userTokenResolver != null) {
            return userTokenResolver.getUserIdFromBearer(authHeader);
        }
        return null;
    }

    /**
     * 统一补全 Bearer 前缀，避免 AdminContext / UserTokenResolver 因缺少前缀解析失败。
     */
    private String toBearerHeader(String token) {
        if (token == null || token.trim().isEmpty()) {
            return null;
        }
        String t = token.trim();
        return t.startsWith("Bearer ") ? t : ("Bearer " + t);
    }

    /**
     * 获取铲屎服务预约订单列表（兼容原有前端路径）
     */
    @GetMapping("/litter-appointments")
    @Operation(summary = "获取铲屎服务预约订单列表")
    public Result<Map<String, Object>> getLitterAppointments(@RequestParam(required = false) String status,
                                                             HttpServletRequest request) {
        return SafeCall.of(() -> Result.success(orderQueryAppService.getLitterAppointments(
                status,
                RequestIdentity.isAdminOrStaff(request),
                RequestIdentity.userId(request))),
                "获取铲屎服务预约列表失败，请稍后重试", log);
    }

    /**
     * 获取商品订单列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取商品订单列表")
    public Result<Map<String, Object>> getOrderList(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String userIdStr,
            @RequestParam(value = "userId", required = false) String userIdFromQuery,
            @RequestParam(required = false) String orderCategory,
            @RequestParam(value = "currentPage", required = false) Integer currentPage,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestHeader(value = "Authorization", required = false) String token) {
        return SafeCall.of(() -> {
            String bearerToken = toBearerHeader(token);
            boolean platformAdmin = adminContext != null && bearerToken != null
                    && adminContext.isPlatformAdmin(bearerToken);
            Long currentStoreId = adminContext != null && bearerToken != null
                    ? adminContext.getCurrentStoreId(bearerToken) : null;
            Long userIdFromToken = bearerToken != null ? getUserIdFromToken(bearerToken) : null;
            String userIdParam = (userIdStr != null && !userIdStr.isEmpty()) ? userIdStr : userIdFromQuery;
            return Result.success(orderQueryAppService.getOrderList(status, userIdParam, orderCategory,
                    currentPage, pageSize, platformAdmin, userIdFromToken, currentStoreId));
        }, "获取商品订单列表失败，请稍后重试", log);
    }

    /**
     * 获取商品订单详情
     */
    @GetMapping("/{orderId}")
    @Operation(summary = "获取商品订单详情")
    public Result<Map<String, Object>> getOrderDetail(
            @PathVariable String orderId,
            @RequestHeader(value = "Authorization", required = false) String token) {
        return SafeCall.of(() -> {
            String bearerToken = toBearerHeader(token);
            boolean platformAdmin = adminContext != null && bearerToken != null
                    && adminContext.isPlatformAdmin(bearerToken);
            Long currentStoreId = adminContext != null && bearerToken != null
                    ? adminContext.getCurrentStoreId(bearerToken) : null;
            Long userIdFromToken = getUserIdFromToken(bearerToken);
            return Result.success(orderQueryAppService.getOrderDetail(
                    orderId, platformAdmin, currentStoreId, userIdFromToken));
        }, "获取商品订单详情失败，请稍后重试", log);
    }

    /**
     * 创建商品订单
     */
    @PostMapping("/create")
    @Operation(summary = "创建商品订单")
    public Result<Map<String, Object>> createOrder(@RequestBody Map<String, Object> orderData,
                                                    @RequestHeader(value = "Authorization", required = false) String token) {
        return SafeCall.of(() -> {
            Long currentUserId = getUserIdFromToken(toBearerHeader(token));
            if (currentUserId == null) {
                throw new BusinessException(401, "用户未登录");
            }
            return Result.success(orderCommandAppService.createOrder(currentUserId, orderData));
        }, "创建订单失败，请稍后重试", log);
    }

    /**
     * 更新商品订单状态
     */
    @PutMapping("/{orderId}/status")
    @Operation(summary = "更新商品订单状态")
    public Result<Boolean> updateOrderStatus(
            @PathVariable String orderId,
            @RequestParam String status,
            @RequestHeader(value = "Authorization", required = false) String token) {
        return SafeCall.of(() -> {
            String bearerToken = toBearerHeader(token);
            boolean platformAdmin = adminContext != null && bearerToken != null
                    && adminContext.isPlatformAdmin(bearerToken);
            Long currentStoreId = adminContext != null && bearerToken != null
                    ? adminContext.getCurrentStoreId(bearerToken) : null;
            Long userIdFromToken = getUserIdFromToken(bearerToken);
            return Result.success(orderCommandAppService.updateOrderStatus(
                    orderId, status, platformAdmin, currentStoreId, userIdFromToken));
        }, "更新订单状态失败，请稍后重试", log);
    }

    /**
     * 获取订单物流信息
     */
    @GetMapping("/{orderId}/logistics")
    @Operation(summary = "获取订单物流信息")
    public Result<Map<String, Object>> getOrderLogistics(
            @PathVariable String orderId,
            @RequestHeader(value = "Authorization", required = false) String token,
            HttpServletRequest request) {
        return SafeCall.of(() -> {
            Long currentStoreId = adminContext != null && token != null
                    ? adminContext.getCurrentStoreId(token) : null;
            return Result.success(orderLogisticsAppService.getOrderLogistics(
                    orderId, RequestIdentity.isAdminOrStaff(request),
                    RequestIdentity.userId(request), currentStoreId));
        }, "获取物流信息失败，请稍后重试", log);
    }

    /**
     * 获取当前店铺的发货仓地址列表（管理员用，用于发货时选择）
     * 店铺管理员：用 token 中的 storeId；平台管理员：可传 storeId 参数（当前订单的店铺ID）以查看/刷新该店铺的发货仓列表
     */
    @GetMapping("/warehouses")
    @Operation(summary = "获取当前店铺的发货仓列表")
    public Result<List<StoreWarehouse>> listWarehouses(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam(required = false) Long storeId,
            HttpServletRequest request) {
        // 安全修复：发货仓地址属经营数据，禁止未登录/C 端用户按 storeId 枚举
        if (!RequestIdentity.isAdminOrStaff(request)) {
            return Result.error(403, "无权查看发货仓");
        }
        Long currentStoreId = adminContext != null && token != null ? adminContext.getCurrentStoreId(token) : null;
        if (currentStoreId == null && storeId != null) {
            currentStoreId = storeId;
        }
        boolean isPlatform = adminContext != null && token != null && adminContext.isPlatformAdmin(token);
        return Result.success(orderWarehouseAppService.listWarehouses(currentStoreId, isPlatform));
    }

    /**
     * 添加发货仓地址（管理员用）
     * 店铺管理员：使用 token 中的 storeId；平台管理员：可传 body.storeId（如当前订单的店铺），须先打开订单详情以带出 storeId
     */
    @PostMapping("/warehouses")
    @Operation(summary = "添加发货仓地址")
    public Result<StoreWarehouse> addWarehouse(
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "Authorization", required = false) String token,
            HttpServletRequest request) {
        // 安全修复：发货仓维护仅限管理员
        if (!RequestIdentity.isAdmin(request)) {
            return Result.error(403, "无权维护发货仓");
        }
        Long currentStoreId = adminContext != null && token != null ? adminContext.getCurrentStoreId(token) : null;
        boolean isPlatform = adminContext != null && token != null && adminContext.isPlatformAdmin(token);
        return Result.success(orderWarehouseAppService.addWarehouse(body, currentStoreId, isPlatform));
    }

    @DeleteMapping("/warehouses/{id}")
    @Operation(summary = "删除发货仓地址")
    public Result<Boolean> deleteWarehouse(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String token,
            HttpServletRequest request) {
        // 安全修复：发货仓维护仅限管理员
        if (!RequestIdentity.isAdmin(request)) {
            return Result.error(403, "无权维护发货仓");
        }
        Long currentStoreId = adminContext != null && token != null ? adminContext.getCurrentStoreId(token) : null;
        boolean isPlatform = adminContext != null && token != null && adminContext.isPlatformAdmin(token);
        orderWarehouseAppService.deleteWarehouse(id, currentStoreId, isPlatform);
        return Result.success(true);
    }

    /**
     * 更新订单物流信息（管理员用）
     */
    @PutMapping("/{orderId}/logistics")
    @Operation(summary = "更新订单物流信息")
    public Result<Boolean> updateOrderLogistics(
            @PathVariable String orderId,
            @RequestBody Map<String, Object> logisticsData,
            @RequestHeader(value = "Authorization", required = false) String token,
            HttpServletRequest request) {
        return SafeCall.of(() -> {
            Long currentStoreId = adminContext != null && token != null
                    ? adminContext.getCurrentStoreId(token) : null;
            return Result.success(orderLogisticsAppService.updateOrderLogistics(
                    orderId, logisticsData, RequestIdentity.isAdminOrStaff(request), currentStoreId));
        }, "更新物流信息失败，请稍后重试", log);
    }

    /**
     * 获取支持的物流公司列表
     */
    @GetMapping("/logistics/companies")
    @Operation(summary = "获取支持的物流公司列表")
    public Result<List<Map<String, String>>> getLogisticsCompanies() {
        return com.pethome.common.SafeCall.of(() -> {
            List<Map<String, String>> companies = logisticsService.getSupportedCompanies();
            return Result.success(companies);
        }, "获取物流公司列表失败，请稍后重试", log);
    }

    /**
     * 申请退款
     */
    @PostMapping("/{orderId}/refund")
    @Operation(summary = "申请退款")
    public Result<Map<String, Object>> requestRefund(
            @PathVariable String orderId,
            @RequestBody Map<String, Object> refundData,
            @RequestHeader(value = "Authorization", required = false) String token) {
        // 安全修复：原实现 token 为空时直接跳过校验——匿名请求可替任意订单发起退款
        if (token == null || token.trim().isEmpty()) {
            throw new BusinessException(401, "未登录或token无效");
        }
        Long tokenUserId = getUserIdFromToken(token.startsWith("Bearer ") ? token : ("Bearer " + token));
        if (tokenUserId == null) {
            throw new BusinessException(401, "未登录或token无效");
        }
        return Result.success(orderRefundAppService.requestRefund(orderId, refundData, tokenUserId));
    }

    /**
     * 用户取消退款申请（仅 pending 可取消）
     */
    @PutMapping("/{orderId}/refund/cancel")
    @Operation(summary = "取消退款申请")
    public Result<Boolean> cancelRefund(
            @PathVariable String orderId,
            @RequestHeader(value = "Authorization", required = false) String token) {
        Long tokenUserId = getUserIdFromToken(token);
        return Result.success(orderRefundAppService.cancelRefund(orderId, tokenUserId));
    }

    /**
     * 获取退款申请列表
     */
    @GetMapping("/refunds")
    @Operation(summary = "获取退款申请列表")
    public Result<Map<String, Object>> getRefundList(
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            @RequestHeader(value = "Authorization", required = false) String token,
            HttpServletRequest request) {
        // 安全修复：退款列表属管理端数据，普通用户/匿名禁止访问
        if (!RequestIdentity.isAdminOrStaff(request)) {
            return Result.error(403, "无权查看退款列表");
        }
        Long currentStoreId = adminContext != null && token != null ? adminContext.getCurrentStoreId(token) : null;
        log.debug("退款列表 - 当前店铺ID: {}", currentStoreId);
        int p = page != null && page > 0 ? page : 1;
        int sz = size != null && size > 0 ? size : 10;
        return Result.success(orderRefundAppService.getRefundList(p, sz, status, currentStoreId));
    }

    /**
     * 批准退款申请
     */
    @PutMapping("/refunds/{orderId}/approve")
    @Operation(summary = "批准退款申请")
    public Result<Map<String, Object>> approveRefund(
            @PathVariable String orderId,
            @RequestHeader(value = "Authorization", required = false) String token,
            HttpServletRequest request) {
        // 安全修复：退款审批属管理员权限，staff 与普通用户禁止
        if (!RequestIdentity.isAdmin(request)) {
            return Result.error(403, "无权审批退款");
        }
        Admin currentAdmin = adminContext != null && token != null ? adminContext.getCurrentAdmin(token) : null;
        String role = currentAdmin != null && currentAdmin.getRole() != null ? currentAdmin.getRole().toLowerCase() : "";
        Long currentStoreId = adminContext != null && token != null ? adminContext.getCurrentStoreId(token) : null;
        return Result.success(orderRefundAppService.approveRefund(orderId, currentStoreId, role));
    }

    /**
     * 拒绝退款申请
     */
    @PutMapping("/refunds/{orderId}/reject")
    @Operation(summary = "拒绝退款申请")
    public Result<Map<String, Object>> rejectRefund(
            @PathVariable String orderId,
            @RequestBody(required = false) Map<String, Object> body,
            @RequestHeader(value = "Authorization", required = false) String token,
            HttpServletRequest request) {
        // 安全修复：退款审批属管理员权限，staff 与普通用户禁止
        if (!RequestIdentity.isAdmin(request)) {
            return Result.error(403, "无权审批退款");
        }
        Admin currentAdmin = adminContext != null && token != null ? adminContext.getCurrentAdmin(token) : null;
        String role = currentAdmin != null && currentAdmin.getRole() != null ? currentAdmin.getRole().toLowerCase() : "";
        Long currentStoreId = adminContext != null && token != null ? adminContext.getCurrentStoreId(token) : null;
        return Result.success(orderRefundAppService.rejectRefund(orderId, body, currentStoreId, role));
    }
    
}
