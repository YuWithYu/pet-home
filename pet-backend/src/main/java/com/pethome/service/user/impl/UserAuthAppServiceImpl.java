package com.pethome.service.user.impl;

import com.pethome.common.BusinessException;
import com.pethome.entity.User;
import com.pethome.security.JwtUtil;
import com.pethome.service.common.SmsService;
import com.pethome.service.user.UserAuthAppService;
import com.pethome.service.user.UserService;
import com.pethome.service.user.support.UserIdentityResolver;
import com.pethome.service.user.support.UserLoginGuardService;
import com.pethome.service.user.support.UserPasswordLoginService;
import com.pethome.service.user.support.UserWechatAppService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserAuthAppServiceImpl implements UserAuthAppService {

    private static final Logger log = LoggerFactory.getLogger(UserAuthAppServiceImpl.class);

    private static final String BEARER_PREFIX = "Bearer ";

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserIdentityResolver userIdentityResolver;

    @Autowired(required = false)
    private SmsService smsService;

    /** 登录防爆破计数（Redis） */
    @Autowired
    private UserLoginGuardService loginGuardService;

    /** admin / user 表账号密码登录与结果装配 */
    @Autowired
    private UserPasswordLoginService passwordLoginService;

    /** 微信小程序登录、授权与手机号 */
    @Autowired
    private UserWechatAppService wechatAppService;

    @Override
    public Map<String, Object> login(String username, String phone, String password) {
        String identifier = loginGuardService.resolveIdentifier(username, phone);
        if (!identifier.isEmpty()) {
            int failCount = loginGuardService.readFailCount(identifier);
            if (failCount >= UserLoginGuardService.MAX_LOGIN_FAILS) {
                log.warn("登录被锁定（连续失败 {} 次）: identifier={}", failCount, identifier);
                throw new BusinessException("登录失败次数过多，账号已锁定 15 分钟，请稍后再试");
            }
        }
        try {
            Map<String, Object> result = passwordLoginService.doLogin(username, phone, password);
            if (!identifier.isEmpty()) {
                loginGuardService.clearFailures(identifier);
            }
            return result;
        } catch (BusinessException e) {
            // 仅凭证类错误计入失败次数，避免「账号被禁用」等业务提示也把正常用户锁死
            if (!identifier.isEmpty() && loginGuardService.isCredentialError(e.getMessage())) {
                loginGuardService.recordFailure(identifier);
            }
            throw e;
        } catch (Exception e) {
            log.error("登录异常, username={}, phone={}", username, phone, e);
            throw new BusinessException("登录失败，请稍后重试");
        }
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
        return wechatAppService.wxappLogin(code);
    }

    @Override
    public Map<String, Object> wxappAuthorize() {
        return wechatAppService.wxappAuthorize();
    }

    @Override
    public Map<String, String> getPhoneNumber(String code, String authorization) {
        return wechatAppService.getPhoneNumber(code, authorization);
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
