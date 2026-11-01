package com.pethome.service.user.support;

import com.pethome.common.BusinessException;
import com.pethome.entity.User;
import com.pethome.service.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 微信小程序端相关：小程序登录、授权占位、手机号获取与回写，从 UserAuthAppServiceImpl 抽取而来。
 */
@Service
public class UserWechatAppService {

    private static final Logger log = LoggerFactory.getLogger(UserWechatAppService.class);

    private static final String BEARER_PREFIX = "Bearer ";

    @Autowired
    private UserService userService;

    @Autowired
    private UserIdentityResolver userIdentityResolver;

    @Value("${wechat.miniapp.app-id}")
    private String wechatAppId;

    @Value("${wechat.miniapp.secret}")
    private String wechatSecret;

    private RestTemplate restTemplate;

    private RestTemplate getRestTemplate() {
        if (restTemplate == null) {
            restTemplate = new RestTemplate();
        }
        return restTemplate;
    }

    public Map<String, Object> wxappLogin(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new BusinessException(1, "授权码不能为空");
        }

        // 调用用户服务进行微信登录
        String token;
        try {
            token = userService.loginByWechat(code);
            // 现在loginByWechat应该总是返回token或抛出异常，不应该返回null
            if (token == null) {
                log.error("微信登录失败：userService.loginByWechat返回null（这不应该发生）");
                throw new BusinessException(1, "微信登录失败：服务返回null，请查看后端日志");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("微信登录异常", e);
            throw new BusinessException(1, "微信登录失败，请稍后重试");
        }

        // 从token中提取用户ID（格式：token_userId_timestamp）
        String[] tokenParts = token.split("_");
        Long userId = null;
        if (tokenParts.length >= 2) {
            try {
                userId = Long.parseLong(tokenParts[1]);
            } catch (NumberFormatException e) {
                log.error("微信登录: 无法从token中解析用户ID", e);
            }
        }

        if (userId == null) {
            throw new BusinessException(1, "微信登录失败，无法获取用户ID");
        }

        // 获取用户详细信息
        User user = userService.getUserById(userId);
        if (user == null) {
            throw new BusinessException(1, "微信登录失败，用户不存在");
        }

        // 构建返回数据
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", user.getId());
        data.put("uid", user.getId()); // 兼容字段

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("uid", user.getId());
        userInfo.put("nickname", user.getNickname() != null ? user.getNickname() : "微信用户");
        userInfo.put("avatar", user.getAvatar() != null ? user.getAvatar()
                : "/static/images/garfield-default-avatar.png");
        userInfo.put("username", user.getUsername() != null ? user.getUsername() : "");
        if (user.getPhone() != null) {
            userInfo.put("phone", user.getPhone());
        }
        data.put("userInfo", userInfo);
        return data;
    }

    public Map<String, Object> wxappAuthorize() {
        // 这里应该调用微信授权服务
        Map<String, Object> result = new HashMap<>();
        result.put("token", "dummy-token");
        result.put("userId", 1);
        return result;
    }

    public Map<String, String> getPhoneNumber(String code, String authorization) {
        try {
            if (code == null || code.isEmpty()) {
                throw new BusinessException("code不能为空");
            }

            // 调用微信API获取手机号
            RestTemplate rt = getRestTemplate();
            String url = "https://api.weixin.qq.com/wxa/business/getuserphonenumber?access_token=" + getAccessToken();

            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("code", code);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = rt.exchange(url, HttpMethod.POST, entity,
                    (Class<Map<String, Object>>) (Class<?>) Map.class);
            Map<String, Object> responseBody = response.getBody();

            if (responseBody == null) {
                throw new BusinessException("微信API返回为空");
            }
            Integer errcode = (Integer) responseBody.get("errcode");
            if (errcode == null || errcode != 0) {
                String errmsg = (String) responseBody.get("errmsg");
                log.error("获取微信手机号失败: 微信API错误 errcode={}, errmsg={}", errcode, errmsg);
                throw new BusinessException("获取手机号失败: " + (errmsg != null ? errmsg : "未知错误"));
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> phoneInfo = (Map<String, Object>) responseBody.get("phone_info");
            if (phoneInfo == null) {
                throw new BusinessException("微信API返回数据格式错误");
            }
            String phoneNumber = (String) phoneInfo.get("phoneNumber");
            String purePhoneNumber = (String) phoneInfo.get("purePhoneNumber");
            String countryCode = (String) phoneInfo.get("countryCode");

            // 更新当前用户的手机号（通过请求头获取token）
            if (authorization != null && authorization.startsWith(BEARER_PREFIX)) {
                User currentUser = userIdentityResolver.resolveUser(authorization);
                if (currentUser != null) {
                    currentUser.setPhone(purePhoneNumber != null ? purePhoneNumber : phoneNumber);
                    userService.updateUser(currentUser);
                }
            }

            Map<String, String> result = new HashMap<>();
            result.put("phone", purePhoneNumber != null ? purePhoneNumber : phoneNumber);
            result.put("countryCode", countryCode != null ? countryCode : "86");
            return result;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取微信手机号异常", e);
            throw new BusinessException("获取手机号失败，请稍后重试");
        }
    }

    /** 获取微信Access Token（注意：生产环境应缓存 token，避免频繁请求） */
    private String getAccessToken() {
        try {
            RestTemplate rt = getRestTemplate();
            String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid="
                    + wechatAppId + "&secret=" + wechatSecret;

            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = rt.getForEntity(url,
                    (Class<Map<String, Object>>) (Class<?>) Map.class);
            Map<String, Object> responseBody = response.getBody();

            if (responseBody != null) {
                String accessToken = (String) responseBody.get("access_token");
                if (accessToken != null) {
                    return accessToken;
                }
                String errmsg = (String) responseBody.get("errmsg");
                log.error("获取Access Token失败: errmsg={}", errmsg);
                throw new RuntimeException("获取Access Token失败: " + errmsg);
            }
            throw new RuntimeException("获取Access Token返回为空");
        } catch (Exception e) {
            log.error("获取Access Token异常", e);
            throw new RuntimeException("获取Access Token异常: " + e.getMessage());
        }
    }
}
