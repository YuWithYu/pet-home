package com.pethome.service.order;

import com.pethome.entity.Payment;

import java.util.Map;

/**
 * 统一支付单服务（预约支付主链路）
 *
 * 商品订单支付沿用既有链路（订单号直连微信）；
 * 服务预约支付统一落 payment 表，与预约状态机解耦。
 */
public interface PaymentService {

    /**
     * 为服务预约创建（或复用）待支付支付单，并生成 JSAPI 支付参数
     *
     * @param userId      当前登录用户（归属校验依据）
     * @param serviceType 预约类型：grooming / hospital / door-cleaning
     * @param bizId       预约 ID
     * @return uni.requestPayment 所需支付参数（timeStamp/nonceStr/package/signType/paySign + payNo）
     */
    Map<String, String> createAppointmentPayParams(Long userId, String serviceType, Long bizId);

    /**
     * 查询预约支付结果：payment 已支付直接返回；否则查微信侧状态，
     * 已扣款未回调时补偿标记支付单（幂等）。
     *
     * @param userId      当前登录用户（归属校验依据）
     * @param serviceType 预约类型
     * @param bizId       预约 ID
     * @return paid / payNo / tradeState
     */
    Map<String, Object> queryAppointmentPayResult(Long userId, String serviceType, Long bizId);

    /**
     * 回调/查单补偿统一入口：按支付单号标记已支付（金额校验由调用方完成后传入）
     *
     * @param payNo         支付单号
     * @param transactionId 微信支付单号（可空）
     * @return true=本次标记生效；false=已标记过（幂等）或支付单不存在
     */
    boolean markPaymentPaid(String payNo, String transactionId);

    /**
     * 按支付单号查支付单（回调路由用）
     */
    Payment getByPayNo(String payNo);

    /**
     * 模拟支付确认（仅 mock 通道开启时由 Controller 放行）：
     * 归属校验后复用与微信回调一致的 markPaymentPaid 状态机标记已支付
     *
     * @param userId      当前登录用户（归属校验依据）
     * @param serviceType 预约类型
     * @param bizId       预约 ID
     * @return paid / payNo
     */
    Map<String, Object> mockConfirmAppointmentPay(Long userId, String serviceType, Long bizId);
}
