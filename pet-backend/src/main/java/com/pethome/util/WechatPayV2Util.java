package com.pethome.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 微信支付 API V2 协议工具（签名 / XML 解析与构造）
 *
 * 支付回调验签与统一下单签名共用同一实现，避免两处各自维护导致验签口径漂移。
 */
public final class WechatPayV2Util {

    /** 微信回调/响应 XML 字段提取（兼容 CDATA 包裹） */
    private static final Pattern XML_FIELD_PATTERN = Pattern.compile("<([a-zA-Z0-9_]+)>(.*?)</\\1>", Pattern.DOTALL);

    private WechatPayV2Util() {
    }

    /**
     * 微信支付 V2 MD5 签名：
     * 非 sign、非空参数按 key 字典序拼 key=value&…&key=API_KEY，MD5 后转大写
     */
    public static String generateSign(Map<String, String> params, String apiKey) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : params.entrySet()) {
            String k = e.getKey();
            String v = e.getValue();
            if ("sign".equals(k) || v == null || v.isEmpty()) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(k).append("=").append(v);
        }
        sb.append("&key=").append(apiKey);
        return md5Hex(sb.toString()).toUpperCase();
    }

    /**
     * 解析微信回调/响应 XML 为参数表。
     * TreeMap 按 key 字典序排列，可直接用于 V2 签名拼接；自动剥离 CDATA 包裹。
     */
    public static Map<String, String> parseXml(String xml) {
        Map<String, String> params = new TreeMap<>();
        if (xml == null || xml.isEmpty()) {
            return params;
        }
        Matcher m = XML_FIELD_PATTERN.matcher(xml);
        while (m.find()) {
            String key = m.group(1);
            String value = m.group(2).trim();
            if (value.startsWith("<![CDATA[") && value.endsWith("]]>")) {
                value = value.substring(9, value.length() - 3);
            }
            params.put(key, value);
        }
        return params;
    }

    /**
     * 构造微信支付请求 XML（字段值统一 CDATA 包裹，规避特殊字符转义问题）
     */
    public static String buildXml(Map<String, String> params) {
        StringBuilder sb = new StringBuilder("<xml>");
        for (Map.Entry<String, String> e : params.entrySet()) {
            sb.append("<").append(e.getKey()).append("><![CDATA[").append(e.getValue()).append("]]></").append(e.getKey()).append(">");
        }
        return sb.append("</xml>").toString();
    }

    public static String md5Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                String h = Integer.toHexString(0xff & b);
                if (h.length() == 1) {
                    hex.append('0');
                }
                hex.append(h);
            }
            return hex.toString();
        } catch (Exception e) {
            throw new IllegalStateException("MD5 算法不可用", e);
        }
    }
}
