package com.pethome.service.user.support;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.pethome.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

/**
 * 用户凭据（密码、账号、手机号绑定）相关逻辑，从 UserServiceImpl 抽取而来。
 */
@Component
public class UserCredentialService {

    private static final Logger logger = LoggerFactory.getLogger(UserCredentialService.class);

    private static final java.util.regex.Pattern USERNAME_PATTERN = java.util.regex.Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_-]{5,19}$");

    @Autowired
    private com.pethome.mapper.UserMapper mpUserMapper;

    /**
     * BCrypt 密码编码器（项目已在 PasswordEncoderConfig 中注册 Bean）
     * 用于新密码加密与登录校验，替代旧的 MD5Encoder。
     * 兼容期：登录时若数据库为旧 MD5 密码，匹配后自动升级为 BCrypt。
     */
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserQueryService userQueryService;

    @Autowired
    private UserTokenCacheService userTokenCacheService;

    /**
     * 校验密码：兼容旧 MD5 密码，匹配后自动升级为 BCrypt
     * @return true 表示密码正确
     */
    public boolean checkPasswordAndUpgrade(String rawPassword, User user) {
        if (user == null) return false;
        String dbPwd = user.getPassword();
        if (dbPwd == null || dbPwd.trim().isEmpty()) return false;

        // 1. 优先用 BCrypt 校验（BCrypt 密文以 $2a$/$2b$/$2y$ 开头）
        if (dbPwd.startsWith("$2a$") || dbPwd.startsWith("$2b$") || dbPwd.startsWith("$2y$")) {
            return passwordEncoder.matches(rawPassword, dbPwd);
        }

        // 2. 兼容旧 MD5 密码（不加盐）：MD5 是 32 位十六进制
        if (dbPwd.length() == 32) {
            String md5Input = DigestUtils.md5DigestAsHex(rawPassword.getBytes());
            if (md5Input.equalsIgnoreCase(dbPwd)) {
                // 自动升级为 BCrypt
                try {
                    user.setPassword(passwordEncoder.encode(rawPassword));
                    mpUserMapper.updateById(user);
                    logger.info("用户密码已从 MD5 升级为 BCrypt, userId={}", user.getId());
                } catch (Exception e) {
                    logger.error("密码升级失败, userId={}", user.getId(), e);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 加密密码：统一使用 BCrypt
     */
    public String encodePassword(String rawPassword) {
        if (rawPassword == null) return null;
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * 对外暴露的密码校验入口（供 Controller 调用）。
     * 委托给 {@link #checkPasswordAndUpgrade}，避免 Controller 自行处理加密细节，
     * 也避免再出现 MD5/明文 fallback 等不安全实现。
     */
    public boolean verifyPassword(String rawPassword, User user) {
        if (rawPassword == null) return false;
        return checkPasswordAndUpgrade(rawPassword, user);
    }

    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        if (userId == null || newPassword == null) return false;
        if (newPassword.trim().length() < 6) {
            throw new IllegalArgumentException("新密码长度不能少于6位");
        }
        User user = mpUserMapper.selectById(userId);
        if (user == null) return false;
        String dbPwd = user.getPassword();
        boolean noPassword = (dbPwd == null || dbPwd.trim().isEmpty());
        // 微信用户：即使历史遗留了随机密码（用户不知），也允许首次设置时原密码可空
        boolean isWechatUser = (user.getOpenid() != null && !user.getOpenid().trim().isEmpty());
        boolean canSetWithoutOld = noPassword || (isWechatUser && (oldPassword == null || oldPassword.trim().isEmpty()));
        if (canSetWithoutOld) {
            // 微信等第三方登录用户无密码，或微信用户首次设置（历史随机密码用户不知），可直接设置新密码
            user.setPassword(encodePassword(newPassword.trim()));
        } else {
            if (oldPassword == null || oldPassword.trim().isEmpty()) {
                throw new IllegalArgumentException("该账号已设置过密码，请输入原密码。若忘记请使用「忘记原密码」通过手机验证码重置。");
            }
            // 校验原密码：兼容旧 MD5 密码，匹配后自动升级为 BCrypt
            // 不再允许明文密码对比，杜绝 dbPwd 为明文也能通过校验的旁路
            boolean match = checkPasswordAndUpgrade(oldPassword.trim(), user);
            if (!match) {
                throw new IllegalArgumentException("原密码错误");
            }
            user.setPassword(encodePassword(newPassword.trim()));
        }
        boolean ok = mpUserMapper.updateById(user) > 0;
        if (ok) userTokenCacheService.clearUserCache(userId);
        return ok;
    }

    public boolean resetPasswordByPhone(String phone, String newPassword) {
        if (phone == null || phone.trim().isEmpty() || newPassword == null || newPassword.trim().isEmpty()) {
            return false;
        }
        String p = phone.trim();
        long count = mpUserMapper.selectCount(new QueryWrapper<User>().eq("phone", p));
        if (count > 1) {
            throw new IllegalArgumentException("该手机号已绑定多个账号，无法通过忘记密码重置。请使用当前登录账号在「修改密码」中用原密码修改，或联系客服处理。");
        }
        User user = userQueryService.getUserByPhone(p);
        if (user == null) {
            logger.warn("忘记密码：手机号未注册，phone: {}", phone);
            return false;
        }
        if (newPassword.length() < 6) {
            throw new IllegalArgumentException("密码长度不能少于6位");
        }
        user.setPassword(encodePassword(newPassword.trim()));
        boolean ok = mpUserMapper.updateById(user) > 0;
        if (ok && user.getId() != null) {
            userTokenCacheService.clearUserCache(user.getId());
        }
        return ok;
    }

    public boolean unbindPhoneByUserId(Long userId) {
        if (userId == null) return false;
        UpdateWrapper<User> uw = new UpdateWrapper<>();
        uw.eq("id", userId).set("phone", null);
        int rows = mpUserMapper.update(null, uw);
        if (rows > 0) {
            userTokenCacheService.clearUserCache(userId);
            return true;
        }
        return false;
    }

    public String changeUsername(Long userId, String newUsername) {
        if (userId == null) return "请先登录";
        if (newUsername == null || (newUsername = newUsername.trim()).isEmpty()) return "账号不能为空";
        if (!USERNAME_PATTERN.matcher(newUsername).matches()) {
            return "账号须以字母或下划线开头，长度6-20位，仅可使用字母、数字、下划线、减号";
        }
        User other = userQueryService.getUserByUsername(newUsername);
        if (other != null && !other.getId().equals(userId)) return "该账号已被占用";
        User user = userQueryService.getUserById(userId);
        if (user == null) return "用户不存在";
        user.setUsername(newUsername);
        user.setUsernameUpdatedAt(new java.util.Date());
        boolean ok = mpUserMapper.updateById(user) > 0;
        if (ok) userTokenCacheService.clearUserCache(userId);
        return ok ? null : "修改失败";
    }
}
