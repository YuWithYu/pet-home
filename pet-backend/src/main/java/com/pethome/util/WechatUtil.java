package com.pethome.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 微信工具类
 * 用于调用微信API
 */
@Component
public class WechatUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(WechatUtil.class);
    
    // 微信API地址
    private static final String WECHAT_API_BASE = "https://api.weixin.qq.com";
    
    @Value("${wechat.miniapp.app-id:}")
    private String appId;
    
    @Value("${wechat.miniapp.secret:}")
    private String appSecret;
    
    /**
     * 通过code获取微信openid和session_key
     * @param code 微信小程序登录code
     * @return JSONObject包含openid和session_key，如果失败返回null
     */
    public JSONObject getOpenidByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            logger.error("微信登录code不能为空");
            return null;
        }
        
        if (appId == null || appId.isEmpty() || appSecret == null || appSecret.isEmpty()) {
            String errorMsg = "微信小程序配置不完整，请检查application.yml中的wechat.miniapp配置。当前app-id: " + 
                (appId != null ? appId : "null") + ", secret: " + (appSecret != null ? "已设置" : "null");
            logger.error(errorMsg);
            throw new RuntimeException(errorMsg);
        }
        
        try {
            // 构建请求URL
            String urlStr = WECHAT_API_BASE + "/sns/jscode2session" +
                    "?appid=" + appId +
                    "&secret=" + appSecret +
                    "&js_code=" + code +
                    "&grant_type=authorization_code";
            
            logger.info("调用微信API获取openid，URL: {}", urlStr.replace(appSecret, "***"));
            
            // 发送HTTP请求
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                String errorMsg = String.format("微信API请求失败，HTTP响应码: %d", responseCode);
                logger.error(errorMsg);
                // 尝试读取错误响应
                try {
                    BufferedReader errorReader = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"));
                    StringBuilder errorResponse = new StringBuilder();
                    String errorLine;
                    while ((errorLine = errorReader.readLine()) != null) {
                        errorResponse.append(errorLine);
                    }
                    errorReader.close();
                    if (errorResponse.length() > 0) {
                        errorMsg += "，错误响应: " + errorResponse.toString();
                    }
                } catch (Exception e) {
                    // 忽略读取错误响应的异常
                }
                throw new RuntimeException(errorMsg);
            }
            
            // 读取响应
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            conn.disconnect();
            
            String responseStr = response.toString();
            logger.info("微信API响应: {}", responseStr);
            
            // 解析JSON响应
            JSONObject jsonObject = JSON.parseObject(responseStr);
            
            // 检查是否有错误
            if (jsonObject.containsKey("errcode")) {
                Integer errcode = jsonObject.getInteger("errcode");
                String errmsg = jsonObject.getString("errmsg");
                logger.error("微信API返回错误，errcode: {}, errmsg: {}", errcode, errmsg);
                
                // 根据错误码返回更详细的错误信息
                String detailedError = "微信API错误";
                if (errcode == 40013) {
                    detailedError = "无效的AppID，请检查application.yml中的wechat.miniapp.app-id配置";
                } else if (errcode == 40125) {
                    detailedError = "无效的Secret，请检查application.yml中的wechat.miniapp.secret配置";
                } else if (errcode == 40029) {
                    detailedError = "code已过期或无效，请重新获取";
                } else if (errcode == 45011) {
                    detailedError = "API调用太频繁，请稍后再试";
                } else {
                    detailedError = String.format("微信API错误(errcode: %d, errmsg: %s)", errcode, errmsg);
                }
                
                // 抛出异常，包含详细错误信息
                throw new RuntimeException(detailedError);
            }
            
            // 返回openid和session_key
            return jsonObject;
            
        } catch (RuntimeException e) {
            // 重新抛出RuntimeException，保留详细错误信息
            throw e;
        } catch (Exception e) {
            logger.error("调用微信API获取openid失败", e);
            throw new RuntimeException("调用微信API失败: " + e.getMessage() + "。请检查网络连接和微信配置", e);
        }
    }
    
    /**
     * 设置微信小程序配置（用于非Spring环境）
     */
    public void setWechatConfig(String appId, String appSecret) {
        this.appId = appId;
        this.appSecret = appSecret;
    }
}
