package com.pethome.service.common;

import java.util.Map;

/**
 * 微信支付 V2 服务（统一下单 / 查单）
 */
public interface WechatPayService {

    /**
     * JSAPI 统一下单，返回小程序 uni.requestPayment 所需支付参数
     *
     * @param outTradeNo   商户订单号（订单 orderNo）
     * @param totalFeeFen  支付金额（单位：分）
     * @param openid       支付用户在小程序下的 openid
     * @param bodyDesc     商品描述（订单摘要）
     * @return timeStamp / nonceStr / package / signType / paySign
     */
    Map<String, String> createJsapiPayParams(String outTradeNo, long totalFeeFen, String openid, String bodyDesc);

    /**
     * 查询微信侧订单支付状态
     *
     * @param outTradeNo 商户订单号
     * @return 含 trade_state（SUCCESS/NOTPAY/CLOSED/...）等字段的参数表；通信失败时抛业务异常
     */
    Map<String, String> queryOrder(String outTradeNo);
}
