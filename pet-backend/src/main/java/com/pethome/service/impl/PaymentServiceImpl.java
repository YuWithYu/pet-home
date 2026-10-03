package com.pethome.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.pethome.common.BusinessException;
import com.pethome.entity.DoorCleaningAppointment;
import com.pethome.entity.GroomingAppointment;
import com.pethome.entity.HospitalAppointment;
import com.pethome.entity.Payment;
import com.pethome.mapper.DoorCleaningAppointmentMapper;
import com.pethome.mapper.GroomingAppointmentMapper;
import com.pethome.mapper.HospitalAppointmentMapper;
import com.pethome.mapper.PaymentMapper;
import com.pethome.service.PaymentService;
import com.pethome.service.WechatPayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 统一支付单服务实现（预约支付）
 *
 * 安全设计：
 * 1. 归属校验：预约 userId 必须等于 token 解析的登录用户
 * 2. 金额以预约表 DB 值为准，签名前不信任任何前端金额
 * 3. 幂等建单：同一预约存在未支付支付单时直接复用（payNo 不变），
 *    避免用户重复进确认页堆积待支付单
 * 4. 支付单号唯一性：PAY + 时间戳 + 随机数，DB 唯一索引兜底
 */
@Service
public class PaymentServiceImpl implements PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private static final String PAY_NO_PREFIX = "PAY";
    private static final DateTimeFormatter PAY_NO_TIME = DateTimeFormatter.ofPattern("yyMMddHHmmss");

    @Autowired
    private PaymentMapper paymentMapper;

    @Autowired
    private WechatPayService wechatPayService;

    @Autowired(required = false)
    private GroomingAppointmentMapper groomingAppointmentMapper;

    @Autowired(required = false)
    private HospitalAppointmentMapper hospitalAppointmentMapper;

    @Autowired(required = false)
    private DoorCleaningAppointmentMapper doorCleaningAppointmentMapper;

    @Autowired(required = false)
    private com.pethome.service.UserService userService;

    /**
     * 模拟支付通道开关（本地联调无商户凭证时演示完整支付流程）
     * 默认关闭；生产环境不设 WECHAT_PAY_MOCK_ENABLED 即走真实微信支付
     */
    @org.springframework.beans.factory.annotation.Value("${wechat.pay.mock-enabled:false}")
    private boolean mockEnabled;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, String> createAppointmentPayParams(Long userId, String serviceType, Long bizId) {
        AppointmentRef apt = loadAppointment(serviceType, bizId);
        if (apt.userId == null || !apt.userId.equals(userId)) {
            log.warn("预约支付越权尝试: token userId={}, 预约归属 userId={}, serviceType={}, bizId={}",
                    userId, apt.userId, serviceType, bizId);
            throw new BusinessException(403, "无权支付此预约");
        }
        if ("cancelled".equals(apt.status) || "no_show".equals(apt.status)) {
            throw new BusinessException(400, "预约已取消，无法支付");
        }
        BigDecimal amount = apt.price == null ? BigDecimal.ZERO : apt.price;
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(400, "预约金额异常，无法支付");
        }

        // 幂等建单：复用同预约未支付的待支付单
        Payment payment = findReusablePendingPayment(Payment.BIZ_APPOINTMENT, bizId);
        if (payment == null) {
            payment = insertNewPayment(userId, Payment.BIZ_APPOINTMENT, bizId, serviceType, amount);
        }

        // mock 通道：不调微信统一下单（无商户凭证），返回 mock 标记由前端改走确认接口
        if (mockEnabled) {
            Map<String, String> data = new HashMap<>();
            data.put("mock", "true");
            data.put("payNo", payment.getPayNo());
            return data;
        }

        // JSAPI 支付必须携带支付者 openid（预约表不落 openid，从用户表解析）
        com.pethome.entity.User user = userService != null ? userService.getUserById(userId) : null;
        if (user == null || user.getOpenid() == null || user.getOpenid().trim().isEmpty()) {
            throw new BusinessException(400, "请使用微信登录后支付");
        }

        Map<String, String> payParams = wechatPayService.createJsapiPayParams(
                payment.getPayNo(), toFen(amount), user.getOpenid().trim(), "宠物之家-服务预约");
        payParams.put("payNo", payment.getPayNo());
        return payParams;
    }

    @Override
    public Map<String, Object> queryAppointmentPayResult(Long userId, String serviceType, Long bizId) {
        AppointmentRef apt = loadAppointment(serviceType, bizId);
        if (apt.userId == null || !apt.userId.equals(userId)) {
            log.warn("预约支付查询越权尝试: token userId={}, 预约归属 userId={}, serviceType={}, bizId={}",
                    userId, apt.userId, serviceType, bizId);
            throw new BusinessException(403, "无权查询此预约");
        }

        Map<String, Object> data = new HashMap<>();
        Payment payment = findLatestPayment(Payment.BIZ_APPOINTMENT, bizId);
        if (payment == null) {
            data.put("paid", false);
            data.put("payNo", null);
            return data;
        }
        data.put("payNo", payment.getPayNo());
        if (payment.getStatus() != null && payment.getStatus() == Payment.STATUS_PAID) {
            data.put("paid", true);
            return data;
        }

        // mock 通道：不查微信侧（无凭证），DB 状态即最终状态
        if (mockEnabled) {
            data.put("paid", false);
            data.put("tradeState", "NOTPAY");
            return data;
        }

        // DB 未支付：查微信侧状态补偿（回调延迟/未达）
        Map<String, String> wxResult = wechatPayService.queryOrder(payment.getPayNo());
        String tradeState = wxResult.get("trade_state");
        if ("SUCCESS".equals(tradeState)) {
            boolean marked = markPaymentPaid(payment.getPayNo(), wxResult.get("transaction_id"));
            if (marked) {
                log.info("查单补偿标记预约支付单已支付: payNo={}, bizId={}", payment.getPayNo(), bizId);
            }
            data.put("paid", true);
        } else {
            data.put("paid", false);
        }
        data.put("tradeState", tradeState);
        return data;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> mockConfirmAppointmentPay(Long userId, String serviceType, Long bizId) {
        AppointmentRef apt = loadAppointment(serviceType, bizId);
        if (apt.userId == null || !apt.userId.equals(userId)) {
            log.warn("模拟支付越权尝试: token userId={}, 预约归属 userId={}, serviceType={}, bizId={}",
                    userId, apt.userId, serviceType, bizId);
            throw new BusinessException(403, "无权支付此预约");
        }

        Payment payment = findLatestPayment(Payment.BIZ_APPOINTMENT, bizId);
        if (payment == null) {
            throw new BusinessException(400, "支付单不存在，请先发起支付");
        }

        // 复用微信回调同款状态机入口（条件更新，幂等）
        boolean marked = markPaymentPaid(payment.getPayNo(),
                "MOCK" + System.currentTimeMillis());
        if (marked) {
            log.info("模拟支付标记支付单已支付: payNo={}, bizId={}, userId={}",
                    payment.getPayNo(), bizId, userId);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("paid", true);
        data.put("payNo", payment.getPayNo());
        return data;
    }

    @Override
    public boolean markPaymentPaid(String payNo, String transactionId) {
        Payment payment = getByPayNo(payNo);
        if (payment == null) {
            return false;
        }
        // 条件更新：仅待支付生效，并发（回调与查单补偿同时到达）只成功一次
        int affected = paymentMapper.markPaid(payment.getId(), transactionId);
        if (affected > 0) {
            log.info("支付单标记已支付: payNo={}, transactionId={}", payNo, transactionId);
            return true;
        }
        return payment.getStatus() != null && payment.getStatus() == Payment.STATUS_PAID;
    }

    @Override
    public Payment getByPayNo(String payNo) {
        if (payNo == null || payNo.trim().isEmpty()) {
            return null;
        }
        QueryWrapper<Payment> qw = new QueryWrapper<>();
        qw.eq("pay_no", payNo.trim()).last("LIMIT 1");
        return paymentMapper.selectOne(qw);
    }

    /** 预约信息载体（三种预约表字段语义一致：userId/price/status） */
    private static class AppointmentRef {
        Long userId;
        BigDecimal price;
        String status;
    }

    private AppointmentRef loadAppointment(String serviceType, Long bizId) {
        if (bizId == null) {
            throw new BusinessException(400, "预约ID不能为空");
        }
        AppointmentRef ref = new AppointmentRef();
        switch (serviceType == null ? "" : serviceType) {
            case "grooming": {
                GroomingAppointment a = groomingAppointmentMapper != null
                        ? groomingAppointmentMapper.selectById(bizId) : null;
                if (a == null) throw new BusinessException(400, "预约不存在");
                ref.userId = a.getUserId();
                ref.price = a.getPrice();
                ref.status = a.getStatus();
                break;
            }
            case "hospital": {
                HospitalAppointment a = hospitalAppointmentMapper != null
                        ? hospitalAppointmentMapper.selectById(bizId) : null;
                if (a == null) throw new BusinessException(400, "预约不存在");
                ref.userId = a.getUserId();
                ref.price = a.getPrice();
                ref.status = a.getStatus();
                break;
            }
            case "door-cleaning":
            case "door_cleaning": {
                DoorCleaningAppointment a = doorCleaningAppointmentMapper != null
                        ? doorCleaningAppointmentMapper.selectById(bizId) : null;
                if (a == null) throw new BusinessException(400, "预约不存在");
                ref.userId = a.getUserId();
                ref.price = a.getPrice();
                ref.status = a.getStatus();
                break;
            }
            default:
                throw new BusinessException(400, "不支持的服务类型: " + serviceType);
        }
        return ref;
    }

    private Payment findReusablePendingPayment(String bizType, Long bizId) {
        QueryWrapper<Payment> qw = new QueryWrapper<>();
        qw.eq("biz_type", bizType).eq("biz_id", bizId).eq("status", Payment.STATUS_PENDING)
                .orderByDesc("id").last("LIMIT 1");
        return paymentMapper.selectOne(qw);
    }

    private Payment findLatestPayment(String bizType, Long bizId) {
        QueryWrapper<Payment> qw = new QueryWrapper<>();
        qw.eq("biz_type", bizType).eq("biz_id", bizId)
                .orderByDesc("id").last("LIMIT 1");
        return paymentMapper.selectOne(qw);
    }

    private Payment insertNewPayment(Long userId, String bizType, Long bizId, String serviceType, BigDecimal amount) {
        Payment payment = new Payment();
        payment.setPayNo(generatePayNo());
        payment.setBizType(bizType);
        payment.setBizId(bizId);
        payment.setServiceType(serviceType);
        payment.setUserId(userId);
        payment.setAmount(amount);
        payment.setStatus(Payment.STATUS_PENDING);
        payment.setCreateTime(LocalDateTime.now());
        payment.setUpdateTime(LocalDateTime.now());
        paymentMapper.insert(payment);
        log.info("创建支付单: payNo={}, bizType={}, bizId={}, userId={}, amount={}",
                payment.getPayNo(), bizType, bizId, userId, amount);
        return payment;
    }

    private String generatePayNo() {
        return PAY_NO_PREFIX + LocalDateTime.now().format(PAY_NO_TIME)
                + String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
    }

    private long toFen(BigDecimal yuan) {
        return yuan.multiply(new BigDecimal("100")).longValue();
    }
}
