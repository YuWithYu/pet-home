package com.pethome.service.order.support;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 物流轨迹解析支撑：快递公司编码映射、快递100 响应解析与请求签名、模拟轨迹状态判定。
 * 纯无状态逻辑，不依赖任何 Mapper 或业务 Service。
 */
@Service
public class LogisticsTrackParser {

    // 物流公司代码映射表
    private static final Map<String, String> COMPANY_CODE_MAP = new HashMap<>();
    static {
        COMPANY_CODE_MAP.put("顺丰快递", "shunfeng");
        COMPANY_CODE_MAP.put("顺丰", "shunfeng");
        COMPANY_CODE_MAP.put("SF", "shunfeng");
        COMPANY_CODE_MAP.put("申通快递", "shentong");
        COMPANY_CODE_MAP.put("申通", "shentong");
        COMPANY_CODE_MAP.put("STO", "shentong");
        COMPANY_CODE_MAP.put("圆通速递", "yuantong");
        COMPANY_CODE_MAP.put("圆通", "yuantong");
        COMPANY_CODE_MAP.put("YTO", "yuantong");
        COMPANY_CODE_MAP.put("韵达速递", "yunda");
        COMPANY_CODE_MAP.put("韵达", "yunda");
        COMPANY_CODE_MAP.put("YD", "yunda");
        COMPANY_CODE_MAP.put("中通快递", "zhongtong");
        COMPANY_CODE_MAP.put("中通", "zhongtong");
        COMPANY_CODE_MAP.put("ZTO", "zhongtong");
        COMPANY_CODE_MAP.put("百世快递", "huitongkuaidi");
        COMPANY_CODE_MAP.put("百世", "huitongkuaidi");
        COMPANY_CODE_MAP.put("HTKY", "huitongkuaidi");
        COMPANY_CODE_MAP.put("EMS", "ems");
        COMPANY_CODE_MAP.put("中国邮政", "ems");
        COMPANY_CODE_MAP.put("邮政", "ems");
        COMPANY_CODE_MAP.put("德邦快递", "debangwuliu");
        COMPANY_CODE_MAP.put("德邦", "debangwuliu");
        COMPANY_CODE_MAP.put("DBL", "debangwuliu");
        COMPANY_CODE_MAP.put("京东物流", "jd");
        COMPANY_CODE_MAP.put("京东", "jd");
        COMPANY_CODE_MAP.put("JD", "jd");
    }

    private static final List<String> SUPPORTED_COMPANY_NAMES = Arrays.asList(
            "顺丰快递", "申通快递", "圆通速递", "韵达速递", "中通快递", "百世快递", "EMS", "德邦快递", "京东物流");

    /**
     * 获取物流公司代码
     */
    public String resolveCompanyCode(String companyName) {
        if (companyName == null) {
            return null;
        }
        return COMPANY_CODE_MAP.get(companyName) != null 
            ? COMPANY_CODE_MAP.get(companyName) 
            : COMPANY_CODE_MAP.get(companyName.toUpperCase());
    }

    /** 对外展示的物流公司名称列表（顺序固定） */
    public List<String> supportedCompanyNames() {
        return new ArrayList<>(SUPPORTED_COMPANY_NAMES);
    }

    /**
     * 解析快递100返回的数据
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> parseKuaidi100Response(Map<String, Object> response) {
        Map<String, Object> result = new HashMap<>();
        
        List<Map<String, Object>> tracks = new ArrayList<>();
        List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
        
        if (data != null && !data.isEmpty()) {
            for (Map<String, Object> item : data) {
                Map<String, Object> track = new HashMap<>();
                track.put("trackingTime", item.get("time"));
                track.put("trackingInfo", item.get("context"));
                track.put("location", item.get("location"));
                tracks.add(track);
            }
        }
        
        result.put("tracks", tracks);
        result.put("state", response.get("state")); // 0-在途，1-揽收，2-疑难，3-已签收，4-退签，5-派件，6-退回，7-转投
        result.put("isCheck", response.get("ischeck")); // 是否签收
        result.put("company", response.get("com"));
        result.put("number", response.get("nu"));
        
        return result;
    }

    /**
     * 快递100 请求签名：MD5(paramJson + key + customer) 转大写
     */
    public String sign(String paramJson, String key, String customer) {
        return MD5(paramJson + key + customer).toUpperCase();
    }

    /**
     * 依据最新一条模拟轨迹判定物流状态：0-在途，3-已签收
     */
    public String resolveMockState(List<Map<String, Object>> tracks) {
        String latestInfo = tracks.isEmpty() ? "" : String.valueOf(tracks.get(0).get("trackingInfo"));
        return tracks.isEmpty() ? "0" : ((latestInfo.contains("已取走") || latestInfo.contains("已签收")) ? "3" : "0");
    }

    /**
     * MD5加密
     */
    private String MD5(String input) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
