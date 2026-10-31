package com.pethome.service.common.impl;

import com.pethome.common.BusinessException;
import com.pethome.service.common.WechatPayService;
import com.pethome.util.WechatPayV2Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

/**
 * 微信支付 API V2 实现（JSAPI 统一下单 / 查单）
 *
 * 安全设计：
 * 1. 商户配置（app-id/mch-id/api-key）未注入时 fail-fast，绝不发起无密钥签名请求
 * 2. 金额以分为单位由调用方从 DB 应付金额换算，签名前不做二次计算
 * 3. 响应验签：统一下单/查单返回的 sign 同样校验，防止响应被篡改
 */
@Service
public class WechatPayServiceImpl implements WechatPayService {

    private static final Logger log = LoggerFactory.getLogger(WechatPayServiceImpl.class);

    private static final String UNIFIED_ORDER_URL = "https://api.mch.weixin.qq.com/pay/unifiedorder";
    private static final String ORDER_QUERY_URL = "https://api.mch.weixin.qq.com/pay/orderquery";
    private static final int HTTP_TIMEOUT_MS = 10_000;

    @Value("${wechat.pay.app-id:}")
    private String appId;

    @Value("${wechat.pay.mch-id:}")
    private String mchId;

    @Value("${wechat.pay.api-key:}")
    private String apiKey;

    @Value("${wechat.pay.notify-url:}")
    private String notifyUrl;

    @Override
    public Map<String, String> createJsapiPayParams(String outTradeNo, long totalFeeFen, String openid, String bodyDesc) {
        validateConfig();

        Map<String, String> params = new TreeMap<>();
        params.put("appid", appId);
        params.put("mch_id", mchId);
        params.put("nonce_str", nonceStr());
        params.put("body", bodyDesc);
        params.put("out_trade_no", outTradeNo);
        params.put("total_fee", String.valueOf(totalFeeFen));
        params.put("spbill_create_ip", localIp());
        params.put("notify_url", notifyUrl);
        params.put("trade_type", "JSAPI");
        params.put("openid", openid);
        params.put("sign", WechatPayV2Util.generateSign(params, apiKey));

        Map<String, String> resp = postXml(UNIFIED_ORDER_URL, WechatPayV2Util.buildXml(params));
        verifyResponseSign(resp);

        String prepayId = resp.get("prepay_id");
        if (!"SUCCESS".equals(resp.get("return_code")) || !"SUCCESS".equals(resp.get("result_code")) || prepayId == null || prepayId.isEmpty()) {
            String err = resp.get("err_code_des") != null ? resp.get("err_code_des") : resp.get("return_msg");
            log.error("微信统一下单失败: orderNo={}, err={}", outTradeNo, err);
            throw new BusinessException("微信下单失败: " + err);
        }

        // 小程序拉起支付二次签名（字段名与统一下单不同：appId 驼峰）
        String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);
        String payNonceStr = nonceStr();
        Map<String, String> signParams = new TreeMap<>();
        signParams.put("appId", appId);
        signParams.put("timeStamp", timeStamp);
        signParams.put("nonceStr", payNonceStr);
        signParams.put("package", "prepay_id=" + prepayId);
        signParams.put("signType", "MD5");

        Map<String, String> payParams = new TreeMap<>();
        payParams.put("timeStamp", timeStamp);
        payParams.put("nonceStr", payNonceStr);
        payParams.put("package", "prepay_id=" + prepayId);
        payParams.put("signType", "MD5");
        payParams.put("paySign", WechatPayV2Util.generateSign(signParams, apiKey));
        return payParams;
    }

    @Override
    public Map<String, String> queryOrder(String outTradeNo) {
        validateConfig();

        Map<String, String> params = new TreeMap<>();
        params.put("appid", appId);
        params.put("mch_id", mchId);
        params.put("out_trade_no", outTradeNo);
        params.put("nonce_str", nonceStr());
        params.put("sign", WechatPayV2Util.generateSign(params, apiKey));

        Map<String, String> resp = postXml(ORDER_QUERY_URL, WechatPayV2Util.buildXml(params));
        verifyResponseSign(resp);

        if (!"SUCCESS".equals(resp.get("return_code"))) {
            log.error("微信查单通信失败: orderNo={}, return_msg={}", outTradeNo, resp.get("return_msg"));
            throw new BusinessException("微信查单失败: " + resp.get("return_msg"));
        }
        return resp;
    }

    /** 商户配置完整性 fail-fast：密钥缺失时不发起任何签名请求 */
    private void validateConfig() {
        if (appId == null || appId.trim().isEmpty()
                || mchId == null || mchId.trim().isEmpty()
                || apiKey == null || apiKey.trim().isEmpty()) {
            log.error("微信支付商户配置缺失（WECHAT_PAY_APP_ID / WECHAT_PAY_MCH_ID / WECHAT_PAY_API_KEY）");
            throw new BusinessException("支付功能未配置，请联系商户管理员");
        }
    }

    /** 响应验签：微信返回的 sign 不匹配时拒绝使用该响应（防篡改） */
    private void verifyResponseSign(Map<String, String> resp) {
        String sign = resp.get("sign");
        if (sign != null && !sign.isEmpty()
                && !sign.equals(WechatPayV2Util.generateSign(resp, apiKey))) {
            log.error("微信支付响应验签失败");
            throw new BusinessException("支付响应校验失败");
        }
    }

    /** POST XML 并解析响应 */
    private Map<String, String> postXml(String urlStr, String xmlBody) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(HTTP_TIMEOUT_MS);
            conn.setReadTimeout(HTTP_TIMEOUT_MS);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
            try (OutputStream os = conn.getOutputStream()) {
                os.write(xmlBody.getBytes(StandardCharsets.UTF_8));
            }
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                conn.disconnect();
                log.error("微信支付接口 HTTP 异常: url={}, code={}", urlStr, responseCode);
                throw new BusinessException("支付服务通信异常，请稍后重试");
            }
            StringBuilder response = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }
            conn.disconnect();
            return WechatPayV2Util.parseXml(response.toString());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("微信支付接口调用失败: url={}", urlStr, e);
            throw new BusinessException("支付服务暂不可用，请稍后重试");
        }
    }

    private String nonceStr() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private String localIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "127.0.0.1";
        }
    }
}
