package com.pethome.controller;

import com.pethome.common.Result;
import com.pethome.entity.Order;
import com.pethome.entity.User;
import com.pethome.service.OrderService;
import com.pethome.service.UserService;
import com.pethome.service.WechatPayService;
import com.pethome.util.UserTokenResolver;
import com.pethome.util.WechatPayV2Util;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 微信支付接口（统一下单 / 查单 / 回调）
 *
 * 安全设计：
 * 1. 验签：微信支付 V2 MD5 签名校验（参数按 key 字典序拼接 + &key=API_KEY 后 MD5 大写），
 *    密钥未配置时 fail-closed 拒绝处理，防止伪造回调把订单标记为已支付
 * 2. 幂等：用订单号做 Redis SETNX，已处理则直接返回 SUCCESS；校验失败路径主动释放锁，
 *    不阻塞微信后续重试
 * 3. 金额校验：回调金额必须等于数据库应付金额
 * 4. 状态机：仅允许 待付款(0) → 已付款(待发货)(1)
 * 5. 归属校验：/prepay、/query 从 token 解析用户并校验订单归属（不信任前端传入的 userId）
 */
@Tag(name = "支付")
@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    /** 幂等锁 key 前缀，value=1，TTL 24 小时 */
    private static final String PAY_NOTIFY_LOCK_PREFIX = "pay:notify:lock:";
    private static final long PAY_NOTIFY_LOCK_TTL_HOURS = 24L;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private WechatPayService wechatPayService;

    @Autowired
    private com.pethome.service.PaymentService paymentService;

    @Autowired
    private UserTokenResolver userTokenResolver;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired(required = false)
    private com.pethome.service.AppointmentDelayService appointmentDelayService;

    /**
     * 微信支付 API V2 密钥（用于签名校验）
     * 生产环境必须通过环境变量注入
     */
    @Value("${wechat.pay.api-key:}")
    private String apiKey;

    /**
     * 模拟支付通道开关：默认关闭（生产 fail-closed），本地联调通过
     * WECHAT_PAY_MOCK_ENABLED=true 开启，跳过微信收银台直接驱动状态机
     */
    @Value("${wechat.pay.mock-enabled:false}")
    private boolean mockEnabled;

    /**
     * 发起支付：JSAPI 统一下单，返回小程序 uni.requestPayment 所需参数
     * 商品订单：{ "orderNo": "ORD..." } 或 { "orderId": 123 }
     * 服务预约：{ "bizType": "APPOINTMENT", "serviceType": "grooming|hospital|door-cleaning", "bizId": 1 }
     */
    @Operation(summary = "发起微信支付（统一下单）")
    @PostMapping("/prepay")
    public Result<Map<String, Object>> prepay(
            @RequestBody Map<String, String> body,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            Long currentUserId = userTokenResolver.resolveUserId(token);
            if (currentUserId == null) {
                return Result.error(401, "未登录或登录已过期");
            }

            // 支付单模式：服务预约（预约状态机与支付解耦，统一落 payment 表）
            if (body != null && "APPOINTMENT".equals(body.get("bizType"))) {
                String serviceType = body.get("serviceType");
                String bizIdStr = body.get("bizId");
                if (serviceType == null || bizIdStr == null || !bizIdStr.trim().matches("\\d+")) {
                    return Result.error(400, "缺少服务类型或预约ID");
                }
                Map<String, String> payParams = paymentService.createAppointmentPayParams(
                        currentUserId, serviceType.trim(), Long.parseLong(bizIdStr.trim()));
                Map<String, Object> data = new HashMap<>(payParams);
                return Result.success(data);
            }

            Order order = resolveOrder(body);
            if (order == null) {
                return Result.error(400, "订单不存在");
            }
            if (order.getUserId() == null || !currentUserId.equals(order.getUserId().longValue())) {
                log.warn("prepay 越权尝试：token userId={} 但订单归属 userId={}, orderId={}",
                        currentUserId, order.getUserId(), order.getId());
                return Result.error(403, "无权支付此订单");
            }
            if (order.getStatus() == null || order.getStatus() != 0) {
                return Result.error(400, "订单状态不允许支付");
            }
            BigDecimal payable = order.getTotalAmount() == null ? BigDecimal.ZERO : order.getTotalAmount();
            if (payable.compareTo(BigDecimal.ZERO) <= 0) {
                return Result.error(400, "订单金额异常，无法支付");
            }
            long totalFeeFen = payable.multiply(new BigDecimal("100")).longValue();

            // mock 通道：跳过 openid 校验与微信统一下单（本地无商户凭证时演示完整流程）
            if (mockEnabled) {
                Map<String, Object> data = new HashMap<>();
                data.put("mock", true);
                data.put("orderNo", order.getOrderNo());
                data.put("totalFee", totalFeeFen);
                return Result.success(data);
            }

            User user = userService.getUserById(currentUserId);
            if (user == null || user.getOpenid() == null || user.getOpenid().trim().isEmpty()) {
                return Result.error(400, "请使用微信登录后支付");
            }

            Map<String, String> payParams = wechatPayService.createJsapiPayParams(
                    order.getOrderNo(), totalFeeFen, user.getOpenid().trim(), "宠物之家-商品订单");

            Map<String, Object> data = new HashMap<>(payParams);
            data.put("orderNo", order.getOrderNo());
            data.put("totalFee", totalFeeFen);
            return Result.success(data);
        } catch (com.pethome.common.BusinessException e) {
            return Result.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("统一下单失败", e);
            return Result.error(500, "发起支付失败，请稍后重试");
        }
    }

    /**
     * 查询支付结果：DB 已支付直接返回；否则查微信侧状态，
     * 微信已扣款但回调未达时补偿更新状态（状态机保证幂等）
     * 商品订单：{ "orderNo": "ORD..." } 或 { "orderId": 123 }
     * 服务预约：{ "bizType": "APPOINTMENT", "serviceType": "...", "bizId": 1 }
     */
    @Operation(summary = "查询支付结果（含补偿状态同步）")
    @PostMapping("/query")
    public Result<Map<String, Object>> query(
            @RequestBody Map<String, String> body,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            Long currentUserId = userTokenResolver.resolveUserId(token);
            if (currentUserId == null) {
                return Result.error(401, "未登录或登录已过期");
            }

            // 支付单模式：服务预约
            if (body != null && "APPOINTMENT".equals(body.get("bizType"))) {
                String serviceType = body.get("serviceType");
                String bizIdStr = body.get("bizId");
                if (serviceType == null || bizIdStr == null || !bizIdStr.trim().matches("\\d+")) {
                    return Result.error(400, "缺少服务类型或预约ID");
                }
                Map<String, Object> data = paymentService.queryAppointmentPayResult(
                        currentUserId, serviceType.trim(), Long.parseLong(bizIdStr.trim()));
                return Result.success(data);
            }

            Order order = resolveOrder(body);
            if (order == null) {
                return Result.error(400, "订单不存在");
            }
            if (order.getUserId() == null || !currentUserId.equals(order.getUserId().longValue())) {
                log.warn("query 越权尝试：token userId={} 但订单归属 userId={}, orderId={}",
                        currentUserId, order.getUserId(), order.getId());
                return Result.error(403, "无权查询此订单");
            }

            Map<String, Object> data = new HashMap<>();
            boolean paid = (order.getStatus() != null && order.getStatus() >= 1)
                    || (order.getPaymentStatus() != null && order.getPaymentStatus() == 1);

            if (!paid) {
                if (mockEnabled) {
                    // mock 通道：不查微信侧（无凭证），已支付状态由 mock/confirm 同步
                    data.put("tradeState", "NOTPAY");
                } else {
                    // DB 未支付：向微信查单（回调延迟/未达时由查单结果驱动状态）
                    Map<String, String> wxResult = wechatPayService.queryOrder(order.getOrderNo());
                    String tradeState = wxResult.get("trade_state");
                    if ("SUCCESS".equals(tradeState)) {
                        // 补偿更新：仅 0→1 生效（状态机校验），已由回调处理时为幂等空操作
                        try {
                            orderService.updateOrderStatus(order.getId(), 1);
                            paid = true;
                            log.info("查单补偿更新订单为已支付: orderNo={}", order.getOrderNo());
                        } catch (Exception e) {
                            // 并发下回调先到导致状态机拒绝属正常；真实失败时保持未支付，等待回调/下次查询
                            log.warn("查单补偿更新订单状态失败（可能已被回调处理）: orderNo={}, err={}",
                                    order.getOrderNo(), e.getMessage());
                        }
                    }
                    data.put("tradeState", tradeState);
                }
            }

            data.put("paid", paid);
            data.put("orderNo", order.getOrderNo());
            return Result.success(data);
        } catch (com.pethome.common.BusinessException e) {
            return Result.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("查询支付结果失败", e);
            return Result.error(500, "查询支付结果失败，请稍后重试");
        }
    }

    /**
     * 模拟支付确认（仅本地联调，mock 通道开启时可用）
     * 复用与微信回调一致的状态机：
     * - 商品订单：0→1 条件更新 + 清除超时取消 key（与 notify 第 8/9 步同款）
     * - 服务预约：markPaymentPaid 条件更新（与 handlePaymentNotify 同款）
     * 生产环境（mock-enabled=false）一律 403，杜绝伪造支付状态
     */
    @Operation(summary = "模拟支付确认（仅联调）")
    @PostMapping("/mock/confirm")
    public Result<Map<String, Object>> mockConfirm(
            @RequestBody Map<String, String> body,
            @RequestHeader(value = "Authorization", required = false) String token) {
        if (!mockEnabled) {
            log.warn("模拟支付确认被拒绝（mock 通道未开启）");
            return Result.error(403, "模拟支付未开启");
        }
        try {
            Long currentUserId = userTokenResolver.resolveUserId(token);
            if (currentUserId == null) {
                return Result.error(401, "未登录或登录已过期");
            }

            // 服务预约支付单：复用回调同款 markPaymentPaid 状态机
            if (body != null && "APPOINTMENT".equals(body.get("bizType"))) {
                String serviceType = body.get("serviceType");
                String bizIdStr = body.get("bizId");
                if (serviceType == null || bizIdStr == null || !bizIdStr.trim().matches("\\d+")) {
                    return Result.error(400, "缺少服务类型或预约ID");
                }
                Map<String, Object> data = paymentService.mockConfirmAppointmentPay(
                        currentUserId, serviceType.trim(), Long.parseLong(bizIdStr.trim()));
                return Result.success(data);
            }

            // 商品订单：复用回调同款 0→1 状态机 + 超时取消 key 清理
            Order order = resolveOrder(body);
            if (order == null) {
                return Result.error(400, "订单不存在");
            }
            if (order.getUserId() == null || !currentUserId.equals(order.getUserId().longValue())) {
                log.warn("模拟支付越权尝试: token userId={} 但订单归属 userId={}, orderId={}",
                        currentUserId, order.getUserId(), order.getId());
                return Result.error(403, "无权支付此订单");
            }
            if (order.getStatus() != null && order.getStatus() >= 1) {
                // 幂等：已支付直接返回成功
                Map<String, Object> data = new HashMap<>();
                data.put("paid", true);
                data.put("orderNo", order.getOrderNo());
                return Result.success(data);
            }
            if (order.getStatus() == null || order.getStatus() != 0) {
                return Result.error(400, "订单状态不允许支付");
            }

            orderService.updateOrderStatus(order.getId(), 1);
            if (appointmentDelayService != null) {
                appointmentDelayService.cancelOrderCancelKey(order.getId());
            }
            log.info("模拟支付标记订单已支付: orderNo={}, orderId={}", order.getOrderNo(), order.getId());

            Map<String, Object> data = new HashMap<>();
            data.put("paid", true);
            data.put("orderNo", order.getOrderNo());
            return Result.success(data);
        } catch (com.pethome.common.BusinessException e) {
            return Result.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("模拟支付确认失败", e);
            return Result.error(500, "模拟支付确认失败，请稍后重试");
        }
    }

    /**
     * 微信支付回调入口
     * 微信会以 XML 形式 POST 到本接口
     */
    @Operation(summary = "微信支付回调")
    @PostMapping(value = "/notify", produces = "application/xml")
    public String payNotify(@RequestBody String xmlBody) {
        String lockKey = null;
        try {
            // 0. 验签前置条件：密钥未配置时无法验签，fail-closed 拒绝处理
            if (apiKey == null || apiKey.trim().isEmpty()) {
                log.error("微信支付 API 密钥未配置，拒绝处理回调（fail-closed）");
                return buildWechatResponse("FAIL", "SIGN_KEY_NOT_CONFIGURED");
            }

            // 1. 解析 XML（含 CDATA 剥离）
            Map<String, String> params = WechatPayV2Util.parseXml(xmlBody);
            String outTradeNo = params.get("out_trade_no");
            String resultCode = params.get("result_code");

            if (outTradeNo == null || outTradeNo.isEmpty()) {
                log.warn("支付回调缺少 out_trade_no");
                return buildWechatResponse("FAIL", "PARAM_ERROR");
            }

            log.info("收到支付回调: orderNo={}, resultCode={}", outTradeNo, resultCode);

            // 2. 验签：不通过一律拒绝（防止伪造回调标记订单已支付）
            String sign = params.get("sign");
            if (sign == null || sign.isEmpty() || !sign.equals(WechatPayV2Util.generateSign(params, apiKey.trim()))) {
                log.warn("支付回调验签失败: orderNo={}", outTradeNo);
                return buildWechatResponse("FAIL", "SIGN_ERROR");
            }

            // 2.5 路由：PAY 前缀为统一支付单（服务预约），其余为商品订单直连链路
            if (outTradeNo.startsWith("PAY")) {
                return handlePaymentNotify(params, outTradeNo);
            }

            // 3. 幂等：已处理过则直接返回 SUCCESS，避免重复处理
            lockKey = PAY_NOTIFY_LOCK_PREFIX + outTradeNo;
            Boolean acquired = redisTemplate.opsForValue()
                    .setIfAbsent(lockKey, "1", PAY_NOTIFY_LOCK_TTL_HOURS, TimeUnit.HOURS);
            if (Boolean.FALSE.equals(acquired)) {
                log.info("支付回调已处理过（幂等命中）: orderNo={}", outTradeNo);
                return buildWechatResponse("SUCCESS", "OK");
            }

            // 4. 业务结果非 SUCCESS 视为支付失败，不修改订单状态（释放锁允许后续有效回调）
            if (!"SUCCESS".equals(resultCode)) {
                log.warn("支付回调业务失败: orderNo={}, resultCode={}", outTradeNo, resultCode);
                return failAndRelease(lockKey, "BIZ_FAIL");
            }

            // 5. 查询订单（不存在时释放锁，避免阻塞微信重试）
            Order order = orderService.getOrderByOrderNo(outTradeNo);
            if (order == null) {
                log.warn("支付回调订单不存在: orderNo={}", outTradeNo);
                return failAndRelease(lockKey, "ORDER_NOT_EXIST");
            }

            // 6. 状态机：仅允许 待付款(0) → 已付款（非待付款视为已处理，直接成功应答）
            if (order.getStatus() != null && order.getStatus() != 0) {
                log.info("订单状态非待付款，忽略回调: orderNo={}, status={}", outTradeNo, order.getStatus());
                return buildWechatResponse("SUCCESS", "OK");
            }

            // 7. 金额校验：微信 total_fee 单位为分，需与数据库应付金额（元）换算
            String totalFeeStr = params.get("total_fee");
            if (totalFeeStr == null) {
                log.warn("支付回调缺少 total_fee: orderNo={}", outTradeNo);
                return failAndRelease(lockKey, "AMOUNT_MISSING");
            }
            long notifyFeeFen;
            try {
                notifyFeeFen = Long.parseLong(totalFeeStr);
            } catch (NumberFormatException e) {
                log.warn("支付回调 total_fee 格式错误: orderNo={}, value={}", outTradeNo, totalFeeStr);
                return failAndRelease(lockKey, "AMOUNT_FORMAT_ERROR");
            }
            BigDecimal expectedYuan = order.getTotalAmount() == null ? BigDecimal.ZERO : order.getTotalAmount();
            long expectedFeeFen = expectedYuan.multiply(new BigDecimal("100")).longValue();
            if (notifyFeeFen != expectedFeeFen) {
                log.error("支付回调金额不匹配: orderNo={}, expected={}, actual={}",
                        outTradeNo, expectedFeeFen, notifyFeeFen);
                return failAndRelease(lockKey, "AMOUNT_MISMATCH");
            }

            // 8. 更新订单状态为已付款（待发货=1）
            orderService.updateOrderStatus(order.getId(), 1);

            // 9. 清除订单超时取消 key：防止支付成功后 key 过期误触发超时取消
            if (appointmentDelayService != null) {
                appointmentDelayService.cancelOrderCancelKey(order.getId());
            }
            log.info("支付回调处理成功: orderNo={}, orderId={}", outTradeNo, order.getId());

            return buildWechatResponse("SUCCESS", "OK");
        } catch (Exception e) {
            log.error("支付回调处理异常", e);
            // 异常路径同样释放锁，保证微信重试可再次进入
            if (lockKey != null) {
                return failAndRelease(lockKey, "INTERNAL_ERROR");
            }
            return buildWechatResponse("FAIL", "INTERNAL_ERROR");
        }
    }

    /**
     * 统一支付单回调处理（服务预约）：
     * 幂等锁 → 业务结果校验 → 金额校验（对照 payment.amount）→ 条件更新标记已支付。
     * 预约状态机不受支付影响（pending → 管理员 confirmed 独立流转）。
     */
    private String handlePaymentNotify(Map<String, String> params, String payNo) {
        String lockKey = null;
        try {
            lockKey = PAY_NOTIFY_LOCK_PREFIX + payNo;
            Boolean acquired = redisTemplate.opsForValue()
                    .setIfAbsent(lockKey, "1", PAY_NOTIFY_LOCK_TTL_HOURS, TimeUnit.HOURS);
            if (Boolean.FALSE.equals(acquired)) {
                log.info("支付单回调已处理过（幂等命中）: payNo={}", payNo);
                return buildWechatResponse("SUCCESS", "OK");
            }

            if (!"SUCCESS".equals(params.get("result_code"))) {
                log.warn("支付单回调业务失败: payNo={}, resultCode={}", payNo, params.get("result_code"));
                return failAndRelease(lockKey, "BIZ_FAIL");
            }

            com.pethome.entity.Payment payment = paymentService.getByPayNo(payNo);
            if (payment == null) {
                log.warn("支付单回调单据不存在: payNo={}", payNo);
                return failAndRelease(lockKey, "PAYMENT_NOT_EXIST");
            }

            // 金额校验：微信 total_fee（分）必须等于 payment.amount（元）换算值
            String totalFeeStr = params.get("total_fee");
            long notifyFeeFen;
            try {
                notifyFeeFen = totalFeeStr != null ? Long.parseLong(totalFeeStr) : -1L;
            } catch (NumberFormatException e) {
                notifyFeeFen = -1L;
            }
            java.math.BigDecimal expected = payment.getAmount() == null
                    ? java.math.BigDecimal.ZERO : payment.getAmount();
            long expectedFeeFen = expected.multiply(new java.math.BigDecimal("100")).longValue();
            if (notifyFeeFen != expectedFeeFen) {
                log.error("支付单回调金额不匹配: payNo={}, expected={}, actual={}", payNo, expectedFeeFen, notifyFeeFen);
                return failAndRelease(lockKey, "AMOUNT_MISMATCH");
            }

            boolean marked = paymentService.markPaymentPaid(payNo, params.get("transaction_id"));
            log.info("支付单回调处理完成: payNo={}, marked={}, transactionId={}",
                    payNo, marked, params.get("transaction_id"));
            return buildWechatResponse("SUCCESS", "OK");
        } catch (Exception e) {
            log.error("支付单回调处理异常: payNo={}", payNo, e);
            if (lockKey != null) {
                return failAndRelease(lockKey, "INTERNAL_ERROR");
            }
            return buildWechatResponse("FAIL", "INTERNAL_ERROR");
        }
    }

    /** 解析请求体中的订单（orderNo 优先；orderNo 查不到时兼容按数字 ID 查） */
    private Order resolveOrder(Map<String, String> body) {
        if (body == null) {
            return null;
        }
        String orderNo = body.get("orderNo");
        if (orderNo != null && !orderNo.trim().isEmpty()) {
            String ref = orderNo.trim();
            Order order = orderService.getOrderByOrderNo(ref);
            if (order != null) {
                return order;
            }
            if (ref.matches("\\d+")) {
                return orderService.getOrderById(Long.parseLong(ref));
            }
            return null;
        }
        String orderId = body.get("orderId");
        if (orderId != null && orderId.matches("\\d+")) {
            return orderService.getOrderById(Long.parseLong(orderId));
        }
        return null;
    }

    /** 校验失败路径：先释放幂等锁再返回 FAIL，避免吞掉微信后续重试 */
    private String failAndRelease(String lockKey, String msg) {
        try {
            redisTemplate.delete(lockKey);
        } catch (Exception e) {
            log.warn("释放支付回调幂等锁失败: key={}", lockKey, e);
        }
        return buildWechatResponse("FAIL", msg);
    }

    /**
     * 构造微信支付回调响应（XML 格式）
     */
    private String buildWechatResponse(String code, String msg) {
        return "<xml>"
                + "<return_code><![CDATA[" + code + "]]></return_code>"
                + "<return_msg><![CDATA[" + msg + "]]></return_msg>"
                + "</xml>";
    }
}
