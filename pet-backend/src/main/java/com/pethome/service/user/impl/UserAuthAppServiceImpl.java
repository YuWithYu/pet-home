package com.pethome.service.user.impl;

import com.pethome.common.BusinessException;
import com.pethome.entity.Admin;
import com.pethome.entity.User;
import com.pethome.security.JwtUtil;
import com.pethome.service.admin.AdminService;
import com.pethome.service.common.SmsService;
import com.pethome.service.user.UserAuthAppService;
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

@Service
public class UserAuthAppServiceImpl implements UserAuthAppService {

    private static final Logger log = LoggerFactory.getLogger(UserAuthAppServiceImpl.class);

    private static final String BEARER_PREFIX = "Bearer ";

    /** 登录防爆破：/api/user/login 同时支持 admin 与 user 表登录，必须与 /api/admin/login 同等的失败锁定 */
    private static final String LOGIN_FAIL_PREFIX = "user:login:fail:";
    private static final int MAX_LOGIN_FAILS = 5;
    private static final long LOGIN_LOCK_MINUTES = 15;

    @Autowired(required = false)
    private org.springframework.data.redis.core.StringRedisTemplate stringRedisTemplate;

    @Autowired
    private UserService userService;

    @Autowired
    private AdminService adminService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserIdentityResolver userIdentityResolver;

    @Autowired(required = false)
    private SmsService smsService;

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

    @Override
    public Map<String, Object> login(String username, String phone, String password) {
        String identifier = loginIdentifier(username, phone);
        if (!identifier.isEmpty()) {
            int failCount = readLoginFailCount(identifier);
            if (failCount >= MAX_LOGIN_FAILS) {
                log.warn("登录被锁定（连续失败 {} 次）: identifier={}", failCount, identifier);
                throw new BusinessException("登录失败次数过多，账号已锁定 15 分钟，请稍后再试");
            }
        }
        try {
            Map<String, Object> result = doLogin(username, phone, password);
            if (!identifier.isEmpty()) {
                clearLoginFailCount(identifier);
            }
            return result;
        } catch (BusinessException e) {
            // 仅凭证类错误计入失败次数，避免「账号被禁用」等业务提示也把正常用户锁死
            if (!identifier.isEmpty() && isCredentialError(e.getMessage())) {
                recordLoginFailure(identifier);
            }
            throw e;
        } catch (Exception e) {
            log.error("登录异常, username={}, phone={}", username, phone, e);
            throw new BusinessException("登录失败，请稍后重试");
        }
    }

    /** 登录失败计数键：优先账号，其次手机号（同一账号的多入口共用同一计数，防止绕过） */
    private String loginIdentifier(String username, String phone) {
        if (username != null && !username.trim().isEmpty()) {
            return username.trim().toLowerCase();
        }
        return phone != null ? phone.trim() : "";
    }

    private boolean isCredentialError(String message) {
        return message != null && (message.contains("密码错误")
                || message.contains("用户名或密码错误") || message.contains("账号不存在")
                || message.contains("账号或密码错误"));
    }

    private int readLoginFailCount(String identifier) {
        if (stringRedisTemplate == null) {
            return 0;
        }
        try {
            String value = stringRedisTemplate.opsForValue().get(LOGIN_FAIL_PREFIX + identifier);
            return value != null ? Integer.parseInt(value) : 0;
        } catch (Exception e) {
            log.warn("读取登录失败次数异常: identifier={}", identifier, e);
            return 0;
        }
    }

    private void recordLoginFailure(String identifier) {
        if (stringRedisTemplate == null) {
            return;
        }
        try {
            Long count = stringRedisTemplate.opsForValue().increment(LOGIN_FAIL_PREFIX + identifier);
            if (count != null && count == 1L) {
                stringRedisTemplate.expire(LOGIN_FAIL_PREFIX + identifier, LOGIN_LOCK_MINUTES, java.util.concurrent.TimeUnit.MINUTES);
            }
        } catch (Exception e) {
            log.warn("记录登录失败次数异常: identifier={}", identifier, e);
        }
    }

    private void clearLoginFailCount(String identifier) {
        if (stringRedisTemplate == null) {
            return;
        }
        try {
            stringRedisTemplate.delete(LOGIN_FAIL_PREFIX + identifier);
        } catch (Exception e) {
            log.warn("清除登录失败次数异常: identifier={}", identifier, e);
        }
    }

    private Map<String, Object> doLogin(String username, String phone, String password) {
        // 校验密码不能为空
        if (password == null || password.trim().isEmpty()) {
            throw new BusinessException("密码不能为空");
        }
        // 校验用户名或手机号至少填一个
        if ((username == null || username.trim().isEmpty()) && (phone == null || phone.trim().isEmpty())) {
            throw new BusinessException("请输入用户名或手机号");
        }

        Map<String, Object> result = new HashMap<>();
        String token = null;

        // 优先尝试从 admin 表登录（管理员/服务人员）
        if (username != null) {
            Admin admin = adminService.getByUsername(username);
            if (admin != null && admin.isEnabled()) {
                // 验证密码（admin 表统一走 BCrypt，由 AdminService.verifyPassword 处理）
                if (adminService.verifyPassword(password, admin.getPassword())) {
                    try {
                        // 生成JWT Token
                        token = jwtUtil.generateToken(admin.getUsername());
                        if (log.isDebugEnabled()) {
                            log.debug("Admin 登录成功，adminId={}, username={}", admin.getId(), admin.getUsername());
                        }

                        result.put("token", token);
                        result.put("userId", admin.getId());
                        result.put("uid", admin.getId()); // 兼容前端使用的uid字段
                        result.put("nickname", admin.getName());
                        result.put("username", admin.getUsername());
                        result.put("avatar", admin.getAvatar());
                        // 统一 role 为小写，便于前端判断
                        String role = admin.getRole();
                        if (role != null) {
                            role = role.toLowerCase();
                        }
                        result.put("role", role);
                        result.put("department", admin.getDepartment());
                        result.put("storeId", admin.getStoreId());
                        result.put("serviceStoreId", admin.getServiceStoreId());
                        return result;
                    } catch (Exception tokenError) {
                        log.error("生成 JWT token 失败, adminId={}", admin.getId(), tokenError);
                        throw new BusinessException("登录失败: Token 生成异常");
                    }
                }
            }
        }

        // 如果 admin 表登录失败，尝试从 user 表登录（普通用户）
        User user = null;
        if (username != null) {
            user = userService.getUserByUsername(username);
            if (user != null) {
                if (user.getStatus() == null || user.getStatus() != 1) {
                    throw new BusinessException("该账号已被封禁");
                }
                // 验证密码：统一走 UserService.verifyPassword（BCrypt 校验 + 旧 MD5 自动升级，拒绝明文 fallback）
                if (userService.verifyPassword(password, user)) {
                    // 生成 JWT Token（与 admin 登录保持一致）
                    token = jwtUtil.generateToken(user.getUsername());
                    // 将登录状态和用户信息存入 Redis 缓存
                    userService.cacheLoginToken(token, user);
                } else {
                    user = null; // 密码错误
                }
            }
        } else if (phone != null) {
            user = userService.getUserByPhone(phone);
            // 若手机号未找到，尝试账号（username）登录
            if (user == null) {
                user = userService.getUserByUsername(phone);
            }
            // 仅支持手机号或账号登录，不支持昵称登录（昵称可随意修改，账号用于登录）
            if (user != null) {
                if (user.getStatus() == null || user.getStatus() != 1) {
                    throw new BusinessException("该账号已被封禁");
                }
                // 验证密码：统一走 UserService.verifyPassword（内部自动处理 BCrypt / 旧 MD5 升级）
                if (!userService.verifyPassword(password, user)) {
                    throw new BusinessException("密码错误");
                }
                // 生成 JWT Token（使用phone代替username，因为新用户username可能为null）
                String tokenUsername = user.getPhone() != null ? user.getPhone()
                        : (user.getUsername() != null ? user.getUsername() : String.valueOf(user.getId()));
                token = jwtUtil.generateToken(tokenUsername);
                userService.cacheLoginToken(token, user);
            } else {
                throw new BusinessException("当前账号不存在");
            }
        } else {
            throw new BusinessException("用户名或手机号不能为空");
        }

        if (user != null && token != null) {
            result.put("token", token);
            result.put("userId", user.getId());
            result.put("uid", user.getId()); // 兼容前端使用的uid字段
            result.put("nickname", user.getNickname());
            result.put("username", user.getUsername());
            result.put("avatar", user.getAvatar());
            return result;
        }
        // 用户名/密码错误（user 被置 null 或 token 未生成）
        throw new BusinessException("用户名或密码错误");
    }

    @Override
    public Map<String, Object> register(String phone, String password, String nickname, String smsCode) {
        try {
            if (phone == null || phone.trim().isEmpty()) {
                throw new BusinessException("手机号不能为空");
            }
            // 已配置短信服务时必须校验验证码，防止接口被滥用注册
            if (smsService != null) {
                if (smsCode == null || smsCode.trim().isEmpty()) {
                    throw new BusinessException("请输入短信验证码");
                }
                if (!smsService.verifyCode(phone.trim(), smsCode.trim())) {
                    throw new BusinessException("验证码错误或已过期");
                }
            }

            // 调用注册服务
            userService.register(phone, password, nickname);

            // 根据phone获取用户
            User registeredUser = userService.getUserByPhone(phone);
            if (registeredUser == null) {
                throw new BusinessException("注册失败：无法获取注册用户信息");
            }

            // 生成JWT Token（使用phone代替username）
            String tokenUsername = registeredUser.getPhone() != null ? registeredUser.getPhone()
                    : (registeredUser.getNickname() != null ? registeredUser.getNickname()
                    : String.valueOf(registeredUser.getId()));
            String token = jwtUtil.generateToken(tokenUsername);
            // 将登录状态和用户信息存入 Redis 缓存
            userService.cacheLoginToken(token, registeredUser);

            Map<String, Object> result = new HashMap<>();
            result.put("token", token);
            result.put("userId", registeredUser.getId());
            result.put("uid", registeredUser.getId()); // 兼容前端使用的uid字段
            result.put("nickname", registeredUser.getNickname());
            // 不返回username，用户不需要这个字段
            result.put("avatar", registeredUser.getAvatar());
            return result;
        } catch (BusinessException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            log.error("注册失败", e);
            // 昵称重复等业务异常
            throw new BusinessException(e.getMessage());
        } catch (Exception e) {
            log.error("注册失败", e);
            throw new BusinessException("注册失败，请稍后重试");
        }
    }

    @Override
    public String forgotPassword(String phone, String smsCode, String newPassword) {
        try {
            if (phone == null || phone.trim().isEmpty()) {
                throw new BusinessException("手机号不能为空");
            }
            if (smsCode == null || smsCode.trim().isEmpty()) {
                throw new BusinessException("验证码不能为空");
            }
            if (newPassword == null || newPassword.trim().isEmpty()) {
                throw new BusinessException("新密码不能为空");
            }
            if (newPassword.length() < 6) {
                throw new BusinessException("密码长度不能少于6位");
            }
            if (smsService == null) {
                throw new BusinessException("短信服务未配置，暂不支持忘记密码");
            }
            if (!smsService.verifyCode(phone.trim(), smsCode.trim())) {
                throw new BusinessException("验证码错误或已过期");
            }
            if (userService.resetPasswordByPhone(phone.trim(), newPassword.trim())) {
                return "密码重置成功，请使用新密码登录";
            }
            throw new BusinessException("该手机号未注册，无法重置密码");
        } catch (BusinessException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            log.error("忘记密码重置失败", e);
            throw new BusinessException(e.getMessage());
        } catch (Exception e) {
            log.error("忘记密码重置失败", e);
            throw new BusinessException("重置失败，请稍后重试");
        }
    }

    @Override
    public Boolean hasPassword(String authorization) {
        try {
            User user = userIdentityResolver.resolveUser(authorization);
            if (user == null) {
                throw new BusinessException("请先登录");
            }
            // 微信用户视为未设置密码（即使用户表历史遗留了随机密码，用户也不知情）
            if (user.getOpenid() != null && !user.getOpenid().trim().isEmpty()) {
                return false;
            }
            String pwd = user.getPassword();
            return pwd != null && !pwd.trim().isEmpty();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("查询失败");
        }
    }

    @Override
    public String changePassword(String authorization, String oldPassword, String newPassword) {
        try {
            // 微信等第三方登录用户可能无密码，原密码可空时由 changePassword 内部判断
            if (newPassword == null || newPassword.trim().isEmpty()) {
                throw new BusinessException("新密码不能为空");
            }
            if (newPassword.trim().length() < 6) {
                throw new BusinessException("新密码长度不能少于6位");
            }
            User user = userIdentityResolver.resolveUser(authorization);
            Long userId = user != null ? user.getId() : null;
            if (userId == null) {
                throw new BusinessException(401, "未登录或登录已过期，请重新登录");
            }
            boolean ok = userService.changePassword(userId, oldPassword != null ? oldPassword.trim() : "",
                    newPassword.trim());
            if (ok) {
                return "密码修改成功，请使用新密码登录";
            }
            throw new BusinessException("修改失败");
        } catch (BusinessException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            log.error("修改密码失败", e);
            throw new BusinessException(e.getMessage());
        } catch (Exception e) {
            log.error("修改密码失败", e);
            throw new BusinessException("修改失败，请稍后重试");
        }
    }

    @Override
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

    @Override
    public Map<String, Object> wxappAuthorize() {
        // 这里应该调用微信授权服务
        Map<String, Object> result = new HashMap<>();
        result.put("token", "dummy-token");
        result.put("userId", 1);
        return result;
    }

    @Override
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

    @Override
    public String bindPhone(String authorization, String phone, String smsCode) {
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            throw new BusinessException("请先登录");
        }
        User currentUser = userIdentityResolver.resolveUserStrict(authorization.substring(BEARER_PREFIX.length()));
        if (currentUser == null) {
            throw new BusinessException("请先登录");
        }
        if (phone == null || phone.trim().isEmpty()) {
            throw new BusinessException("手机号不能为空");
        }
        if (smsCode == null || smsCode.trim().isEmpty()) {
            throw new BusinessException("验证码不能为空");
        }
        if (smsService == null) {
            throw new BusinessException("短信服务未配置，暂不支持绑定其他号码");
        }
        if (!smsService.verifyCode(phone.trim(), smsCode.trim())) {
            throw new BusinessException("验证码错误或已过期");
        }
        // 已绑定的手机号不能再被其他账号绑定
        User otherUser = userService.getUserByPhone(phone.trim());
        if (otherUser != null && !otherUser.getId().equals(currentUser.getId())) {
            throw new BusinessException("该手机号已被绑定");
        }
        currentUser.setPhone(phone.trim());
        if (!userService.updateUser(currentUser)) {
            throw new BusinessException("绑定失败");
        }
        return "绑定成功";
    }

    @Override
    public String unbindPhone(String authorization) {
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            throw new BusinessException("请先登录");
        }
        User currentUser = userIdentityResolver.resolveUserStrict(authorization.substring(BEARER_PREFIX.length()));
        if (currentUser == null) {
            throw new BusinessException("请先登录");
        }
        if (currentUser.getPhone() == null || currentUser.getPhone().trim().isEmpty()) {
            throw new BusinessException("当前账号未绑定手机号，无需解绑");
        }
        if (!userService.unbindPhoneByUserId(currentUser.getId())) {
            throw new BusinessException("解绑失败");
        }
        return "解绑成功";
    }

    @Override
    public String verifyPassword(String authorization, String password) {
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            throw new BusinessException("请先登录");
        }
        String token = authorization.substring(BEARER_PREFIX.length()).trim();
        if (token.isEmpty()) {
            throw new BusinessException("请先登录");
        }
        User currentUser = userIdentityResolver.resolveUserStrict(token);
        if (currentUser == null) {
            throw new BusinessException("请先登录");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new BusinessException("请输入密码");
        }
        String dbPwd = currentUser.getPassword();
        if (dbPwd == null || dbPwd.trim().isEmpty()) {
            throw new BusinessException("该账号未设置密码，请先到「修改密码」中设置密码");
        }
        // 统一走 UserService.verifyPassword（BCrypt 校验 + 旧 MD5 自动升级，拒绝明文 fallback）
        if (!userService.verifyPassword(password.trim(), currentUser)) {
            throw new BusinessException("密码错误");
        }
        return "验证成功";
    }

    @Override
    public String changeUsername(String authorization, String newUsername) {
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            throw new BusinessException("请先登录");
        }
        User currentUser = userIdentityResolver.resolveUserStrict(authorization.substring(BEARER_PREFIX.length()));
        if (currentUser == null) {
            throw new BusinessException("请先登录");
        }
        String err = userService.changeUsername(currentUser.getId(), newUsername);
        if (err != null) {
            throw new BusinessException(err);
        }
        return "修改成功，请使用新账号登录";
    }
}
