package com.pethome.service.user.support;

import com.pethome.common.BusinessException;
import com.pethome.entity.Admin;
import com.pethome.entity.User;
import com.pethome.security.JwtUtil;
import com.pethome.service.admin.AdminService;
import com.pethome.service.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 账号密码登录：admin 表 / user 表双入口校验与登录结果装配，从 UserAuthAppServiceImpl 抽取而来。
 *
 * <p>只负责凭证校验与 token 装配，登录防爆破计数仍由 {@link UserLoginGuardService} 与调用方负责。</p>
 */
@Service
public class UserPasswordLoginService {

    private static final Logger log = LoggerFactory.getLogger(UserPasswordLoginService.class);

    @Autowired
    private UserService userService;

    @Autowired
    private AdminService adminService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 依次尝试 admin 表、user 表登录并按原语义装配返回结构；失败抛 {@link BusinessException}。
     */
    public Map<String, Object> doLogin(String username, String phone, String password) {
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
}
