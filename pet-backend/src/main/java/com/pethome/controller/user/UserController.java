package com.pethome.controller.user;


import com.pethome.common.SafeCall;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import javax.servlet.http.HttpServletRequest;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.pethome.common.Result;
import com.pethome.entity.User;
import com.pethome.entity.UserFollow;
import com.pethome.entity.Post;
import com.pethome.entity.Admin;
import com.pethome.service.user.UserService;
import com.pethome.service.user.UserFollowService;
import com.pethome.service.community.PostService;
import com.pethome.service.admin.AdminService;
import com.pethome.service.order.PointsRecordService;
import com.pethome.entity.PointsRecord;
import com.pethome.security.JwtUtil;
import io.jsonwebtoken.Claims;

import java.util.ArrayList;
import java.util.List;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/user")  // 统一使用单数路径，对应数据库user表
@Tag(name = "用户管理")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;
    
    @Autowired
    private UserFollowService userFollowService;
    
    @Autowired
    private PostService postService;
    
    @Autowired
    private AdminService adminService;
    
    @Autowired
    private PointsRecordService pointsRecordService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired(required = false)
    private com.pethome.service.common.SmsService smsService;
    
    @Autowired(required = false)
    private com.pethome.service.admin.AdminPermissionService adminPermissionService;
    
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

    @GetMapping("/current")
    @Operation(summary = "获取当前用户信息（支持 user 和 admin 表）")
    public Result<Map<String, Object>> getCurrentUser(
                                       @RequestHeader(value = "Authorization", required = false) String token,
                                       @RequestParam(required = false, value = "userId") Long userId,
                                       @RequestParam(required = false, value = "username") String username,
                                       HttpServletRequest request) {
        try {
            // 安全修复：原代码直接信任前端传入的 userId 查询任意用户资料，并返回手机号等敏感字段，
            // 构成越权读取漏洞。修复策略：
            // 1) 当传入 userId 且与当前登录用户不一致时，视为"查看他人资料"，仅返回公开字段（脱敏）；
            // 2) 删除大量调试 System.out.println，避免敏感信息进入日志；
            // 3) 不再从 request.getParameter 重复读取 userId，统一由 @RequestParam 绑定。

            // 解析当前登录用户身份（用于判定是自查还是查他人）
            // 注意：subject 可能是手机号（纯数字），不能直接 Long.valueOf，须走反查链
            Long currentUserId = getUserIdFromToken(token);
            // 也尝试从拦截器写入的 request 属性中获取
            if (currentUserId == null) {
                Object attrId = request.getAttribute("userId");
                if (attrId instanceof Long) currentUserId = (Long) attrId;
                else if (attrId != null) {
                    try { currentUserId = Long.valueOf(attrId.toString()); } catch (NumberFormatException ignored) {}
                }
            }

            if (userId != null && userId > 0) {
                // 查询他人资料：仅返回公开字段，不返回手机号/邮箱/密码等敏感信息
                User targetUser = userService.getUserById(userId);
                if (targetUser == null) {
                    return Result.error("用户不存在或已被禁用");
                }

                // 确保memberLevel有默认值
                if (targetUser.getMemberLevel() == null) {
                    targetUser.setMemberLevel(1);
                }

                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", targetUser.getId());
                userInfo.put("uid", targetUser.getId());
                userInfo.put("username", targetUser.getUsername() != null ? targetUser.getUsername() : "");
                userInfo.put("nickname", targetUser.getNickname() != null ? targetUser.getNickname() : "用户");
                userInfo.put("avatar", targetUser.getAvatar() != null ? targetUser.getAvatar() : "/static/images/garfield-default-avatar.png");
                userInfo.put("backgroundImage", targetUser.getBackgroundImage());
                userInfo.put("memberLevel", targetUser.getMemberLevel());
                userInfo.put("level", targetUser.getMemberLevel());
                userInfo.put("gender", targetUser.getGender());
                userInfo.put("signature", targetUser.getSignature() != null ? targetUser.getSignature() : "");
                userInfo.put("bio", targetUser.getSignature() != null ? targetUser.getSignature() : "");
                userInfo.put("charm", targetUser.getCharm() != null ? targetUser.getCharm() : 0);
                userInfo.put("role", "user");
                userInfo.put("permissions", new String[]{"user"});
                userInfo.put("isAdmin", false);
                // 仅当查询的是自己时才返回 points 与手机号；查询他人时不返回
                if (currentUserId != null && currentUserId.equals(userId)) {
                    userInfo.put("points", targetUser.getPoints() != null ? targetUser.getPoints() : 0);
                    userInfo.put("phone", targetUser.getPhone() != null ? targetUser.getPhone() : "");
                }
                return Result.success(userInfo);
            }

            // 未指定userId或userId无效，继续使用token查询当前用户

            // 优先从 token 中获取用户名
            String tokenUsername = null;
            if (token != null && token.startsWith("Bearer ")) {
                try {
                    token = token.substring(7);
                    tokenUsername = jwtUtil.getUsernameFromToken(token);
                } catch (Exception e) {
                    // token 解析失败不打印敏感细节
                }
            }

            // 1. 先尝试从 admin 表查询（管理员或服务人员）
            if (tokenUsername != null) {
                Admin admin = adminService.getByUsername(tokenUsername);
                if (admin != null) {
                    // 即使未启用也返回，由前端处理（前端可以根据 status 判断）
                    // 返回管理员信息
                    Map<String, Object> adminInfo = new HashMap<>();
                    adminInfo.put("id", admin.getId());
                    adminInfo.put("username", admin.getUsername());
                    adminInfo.put("name", admin.getName());
                    adminInfo.put("avatar", admin.getAvatar());
                    // 统一 role 为小写，便于前端判断
                    String role = admin.getRole();
                    if (role != null) {
                        role = role.toLowerCase();
                    } else {
                        role = "staff"; // 默认角色
                    }
                    adminInfo.put("role", role);
                    adminInfo.put("department", admin.getDepartment());
                    
                    // 获取权限列表（从数据库查询）
                    List<String> permissionCodes = new ArrayList<>();
                    try {
                        if (adminPermissionService != null) {
                            try {
                                List<com.pethome.entity.AdminPermission> permissions = adminPermissionService.getPermissionsByAdminId(admin.getId());

                                if (permissions != null && !permissions.isEmpty()) {
                                    permissionCodes = permissions.stream()
                                        .filter(p -> p != null && p.getStatus() != null && p.getStatus() == 1)
                                        .map(com.pethome.entity.AdminPermission::getPermissionCode)
                                        .filter(code -> code != null && !code.trim().isEmpty())
                                        .collect(java.util.stream.Collectors.toList());
                                }
                            } catch (Exception e) {
                                log.error("查询权限失败, adminId={}", admin.getId(), e);
                            }
                        }

                        // 如果权限表中没有记录，根据角色决定默认权限
                        if (permissionCodes.isEmpty()) {
                            if (admin.isSuperAdmin()) {
                                // 超级管理员且没有设置权限，默认拥有所有权限
                                if (adminPermissionService != null) {
                                    List<com.pethome.entity.AdminPermission> allPermissions = adminPermissionService.getAllAvailablePermissions();
                                    permissionCodes = allPermissions.stream()
                                        .map(com.pethome.entity.AdminPermission::getPermissionCode)
                                        .collect(java.util.stream.Collectors.toList());
                                } else {
                                    permissionCodes.add("admin");
                                }
                            } else {
                                // 员工等角色：未在权限管理中设置则不自动分配，由管理员自行在权限设置中分配
                            }
                        }
                        // 门店管理员自动拥有门店客服权限（把门店管理员当成客服）
                        if (admin.getServiceStoreId() != null && !permissionCodes.contains("/services/outlet-customer-chat")) {
                            permissionCodes.add("/services/outlet-customer-chat");
                        }
                        // 店铺管理员自动拥有店铺客服权限
                        if (admin.getStoreId() != null && !permissionCodes.contains("/mall/store-customer-chat")) {
                            permissionCodes.add("/mall/store-customer-chat");
                        }
                    } catch (Exception permError) {
                        // 如果权限表不存在或其他错误，根据角色使用默认权限
                        log.error("获取权限失败（可能是表不存在）, adminId={}", admin.getId(), permError);
                        if (admin.isSuperAdmin()) {
                            permissionCodes.add("admin");
                        }
                        // 员工等角色不自动加 admin，需在权限管理中手动分配
                    }
                    
                    adminInfo.put("permissions", permissionCodes.toArray(new String[0]));
                    adminInfo.put("memberLevel", admin.isSuperAdmin() ? 10 : 5); // 管理员等级更高
                    adminInfo.put("isAdmin", true); // 标记为管理员
                    adminInfo.put("status", admin.getStatus()); // 状态信息
                    adminInfo.put("storeId", admin.getStoreId());
                    adminInfo.put("serviceStoreId", admin.getServiceStoreId()); // 所属服务门店，用于排班/预约按门店过滤

                    return Result.success(adminInfo);
                }
            }

            // 2. 如果 admin 表中没有，再查询 user 表（普通用户）
            User user = null;

            // 优先使用 token 中的用户名/手机号查询（微信/手机登录时 subject 可能是手机号）
            if (tokenUsername != null) {
                user = userService.getUserByUsername(tokenUsername);
                if (user == null) {
                    user = userService.getUserByPhone(tokenUsername);
                }
            }
            // 其次使用传入的 userId 查询
            else if (userId != null) {
                user = userService.getUserById(userId);
            } 
            // 再次使用传入的 username 查询
            else if (username != null && !username.isEmpty()) {
                user = userService.getUserByUsername(username);
            }
            // 默认返回ID为1的用户（用于测试）
            else {
                user = userService.getUserById(1L);
            }
            
            if (user == null) {
                // 如果 token 存在但用户不存在，返回更详细的错误信息
                if (tokenUsername != null) {
                    return Result.error(401, "用户不存在: " + tokenUsername + "。请检查账号是否正确或联系管理员");
                } else {
                    // 如果没有提供任何参数，说明是未登录状态，返回401而不是500
                    return Result.error(401, "未登录，请先登录");
                }
            }

            if (user.getStatus() == null || user.getStatus() != 1) {
                return Result.error(401, "该账号已被封禁");
            }
            
            // 隐藏密码
            user.setPassword(null);
            
            // 确保memberLevel有默认值（如果为null，设为1）
            if (user.getMemberLevel() == null) {
                user.setMemberLevel(1);
            }
            
            // 转换为 Map 格式，保持与前端期望的格式一致
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("uid", user.getId()); // 兼容前端使用的uid字段
            userInfo.put("username", user.getUsername());
            userInfo.put("nickname", user.getNickname());
            userInfo.put("avatar", user.getAvatar());
            userInfo.put("phone", user.getPhone() != null ? user.getPhone() : ""); // 添加手机号字段
            userInfo.put("backgroundImage", user.getBackgroundImage()); // 添加背景图字段
            userInfo.put("memberLevel", user.getMemberLevel());
            userInfo.put("level", user.getMemberLevel()); // 兼容前端使用的level字段
            userInfo.put("points", user.getPoints() != null ? user.getPoints() : 0); // 积分（罐头数量）
            userInfo.put("charm", user.getCharm() != null ? user.getCharm() : 0); // 魅力值
            userInfo.put("role", "user"); // 普通用户
            userInfo.put("permissions", new String[]{"user"});
            userInfo.put("isAdmin", false);

            return Result.success(userInfo);
        } catch (Exception e) {
            log.error("获取用户信息失败", e);

            // 如果是认证相关的异常（如 token 解析失败、JWT 异常等），返回 401
            String exceptionMessage = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if (exceptionMessage.contains("token") ||
                exceptionMessage.contains("jwt") || 
                exceptionMessage.contains("authentication") ||
                exceptionMessage.contains("unauthorized") ||
                e.getClass().getName().contains("JwtException") ||
                e.getClass().getName().contains("Token")) {
                return Result.error(401, "认证失败，请重新登录");
            }
            
            // 其他异常返回 500
            return Result.error(500, "获取用户信息失败，请稍后重试");
        }
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录（兼容 user 和 admin 表）")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> loginData) {
        String username = loginData.get("username");
        String phone = loginData.get("phone");
        String password = loginData.get("password");

        // 校验密码不能为空
        if (password == null || password.trim().isEmpty()) {
            return Result.error("密码不能为空");
        }

        // 校验用户名或手机号至少填一个
        if ((username == null || username.trim().isEmpty()) && (phone == null || phone.trim().isEmpty())) {
            return Result.error("请输入用户名或手机号");
        }

        Map<String, Object> result = new HashMap<>();
        String token = null;
        
        try {
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
                            return Result.success(result);
                        } catch (Exception tokenError) {
                            log.error("生成 JWT token 失败, adminId={}", admin.getId(), tokenError);
                            return Result.error("登录失败: Token 生成异常");
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
                        return Result.error("该账号已被封禁");
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
                        return Result.error("该账号已被封禁");
                    }
                    // 验证密码：统一走 UserService.verifyPassword（内部自动处理 BCrypt / 旧 MD5 升级）
                    if (!userService.verifyPassword(password, user)) {
                        return Result.error("密码错误");
                    }
                    // 生成 JWT Token（使用phone代替username，因为新用户username可能为null）
                    String tokenUsername = user.getPhone() != null ? user.getPhone() : 
                                           (user.getUsername() != null ? user.getUsername() : String.valueOf(user.getId()));
                    token = jwtUtil.generateToken(tokenUsername);
                    userService.cacheLoginToken(token, user);
                } else {
                    // 用户不存在，返回特定错误信息
                    return Result.error("当前账号不存在");
                }
            } else {
                return Result.error("用户名或手机号不能为空");
            }
            
            if (user != null && token != null) {
                result.put("token", token);
                result.put("userId", user.getId());
                result.put("uid", user.getId()); // 兼容前端使用的uid字段
                result.put("nickname", user.getNickname());
                result.put("username", user.getUsername());
                result.put("avatar", user.getAvatar());
                return Result.success(result);
            } else {
                // 用户名/密码错误（user 被置 null 或 token 未生成）
                return Result.error("用户名或密码错误");
            }
        } catch (Exception e) {
            log.error("登录异常, username={}, phone={}", username, phone, e);
            return Result.error("登录失败，请稍后重试");
        }
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public Result<Map<String, Object>> register(@RequestBody Map<String, String> registerData) {
        try {
            String phone = registerData.get("phone");
            String password = registerData.get("password");
            String nickname = registerData.get("nickname");
            String smsCode = registerData != null ? registerData.get("smsCode") : null;

            if (phone == null || phone.trim().isEmpty()) {
                return Result.error("手机号不能为空");
            }
            // 已配置短信服务时必须校验验证码，防止接口被滥用注册
            if (smsService != null) {
                if (smsCode == null || smsCode.trim().isEmpty()) {
                    return Result.error("请输入短信验证码");
                }
                if (!smsService.verifyCode(phone.trim(), smsCode.trim())) {
                    return Result.error("验证码错误或已过期");
                }
            }

            // 调用注册服务
            userService.register(phone, password, nickname);
            
            // 根据phone获取用户
            User registeredUser = userService.getUserByPhone(phone);
            
            if (registeredUser == null) {
                return Result.error("注册失败：无法获取注册用户信息");
            }
            
            // 生成JWT Token（使用phone代替username）
            String tokenUsername = registeredUser.getPhone() != null ? registeredUser.getPhone() : 
                                   (registeredUser.getNickname() != null ? registeredUser.getNickname() : String.valueOf(registeredUser.getId()));
            String token = jwtUtil.generateToken(tokenUsername);
            // 将登录状态和用户信息存入 Redis 缓存
            userService.cacheLoginToken(token, registeredUser);
            
            Map<String, Object> result = new HashMap<>();
            result.put("token", token);
            result.put("userId", registeredUser.getId());
            result.put("uid", registeredUser.getId()); // 兼容前端使用的uid字段
            result.put("nickname", registeredUser.getNickname());
            // 不返回username，用户不需要这个字段
            // result.put("username", registeredUser.getUsername());
            result.put("avatar", registeredUser.getAvatar());
            return Result.success(result);
        } catch (IllegalArgumentException e) {
            log.error("注册失败", e);
            // 昵称重复等业务异常
            log.warn("注册失败（业务异常）: {}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("注册失败", e);
            return Result.error("注册失败，请稍后重试");
        }
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "忘记密码：通过手机号+短信验证码重置密码")
    public Result<String> forgotPassword(@RequestBody Map<String, String> body) {
        try {
            String phone = body != null ? body.get("phone") : null;
            String smsCode = body != null ? body.get("smsCode") : null;
            String newPassword = body != null ? body.get("newPassword") : null;
            if (phone == null || phone.trim().isEmpty()) {
                return Result.error("手机号不能为空");
            }
            if (smsCode == null || smsCode.trim().isEmpty()) {
                return Result.error("验证码不能为空");
            }
            if (newPassword == null || newPassword.trim().isEmpty()) {
                return Result.error("新密码不能为空");
            }
            if (newPassword.length() < 6) {
                return Result.error("密码长度不能少于6位");
            }
            if (smsService == null) {
                return Result.error("短信服务未配置，暂不支持忘记密码");
            }
            if (!smsService.verifyCode(phone.trim(), smsCode.trim())) {
                return Result.error("验证码错误或已过期");
            }
            boolean ok = userService.resetPasswordByPhone(phone.trim(), newPassword.trim());
            if (ok) {
                return Result.success("密码重置成功，请使用新密码登录");
            }
            return Result.error("该手机号未注册，无法重置密码");
        } catch (IllegalArgumentException e) {
            log.error("忘记密码重置失败", e);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("忘记密码重置失败", e);
            return Result.error("重置失败，请稍后重试");
        }
    }

    @GetMapping("/has-password")
    @Operation(summary = "检查当前账号是否已设置密码（微信登录用户可能无密码）")
    public Result<Boolean> hasPassword(@RequestHeader(value = "Authorization", required = false) String token) {
        try {
            if (token == null || !token.startsWith("Bearer ")) {
                return Result.error("请先登录");
            }
            User user = userService.getUserByToken(token.substring(7).trim());
            if (user == null) {
                try {
                    String subject = jwtUtil.getUsernameFromToken(token.substring(7).trim());
                    if (subject != null) {
                        user = userService.getUserByPhone(subject);
                        if (user == null) user = userService.getUserByUsername(subject);
                    }
                } catch (Exception e) {
                    log.debug("hasPassword: token 解析用户失败: {}", e.toString());
                }
            }
            if (user == null) return Result.error("请先登录");
            // 微信用户视为未设置密码（即使用户表历史遗留了随机密码，用户也不知情）
            if (user.getOpenid() != null && !user.getOpenid().trim().isEmpty()) {
                return Result.success(false);
            }
            String pwd = user.getPassword();
            return Result.success(pwd != null && !pwd.trim().isEmpty());
        } catch (Exception e) {
            return Result.error("查询失败");
        }
    }

    @PostMapping("/change-password")
    @Operation(summary = "修改密码（登录后）")
    public Result<String> changePassword(@RequestHeader(value = "Authorization", required = false) String token,
                                         @RequestBody Map<String, String> body) {
        try {
            String oldPassword = body != null ? body.get("oldPassword") : null;
            String newPassword = body != null ? body.get("newPassword") : null;
            // 微信等第三方登录用户可能无密码，原密码可空时由 changePassword 内部判断
            if (newPassword == null || newPassword.trim().isEmpty()) {
                return Result.error("新密码不能为空");
            }
            if (newPassword.trim().length() < 6) {
                return Result.error("新密码长度不能少于6位");
            }
            Long userId = null;
            User user = null;
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                try {
                    user = userService.getUserByToken(token);
                    if (user != null) userId = user.getId();
                } catch (Exception e) {
                    log.debug("changePassword: getUserByToken 失败: {}", e.toString());
                }
                if (user == null) {
                    try {
                        String tokenUsername = jwtUtil.getUsernameFromToken(token);
                        user = userService.getUserByPhone(tokenUsername);
                        if (user == null) user = userService.getUserByUsername(tokenUsername);
                        if (user != null) userId = user.getId();
                    } catch (Exception e) {
                        log.debug("changePassword: 从 JWT 解析用户失败: {}", e.toString());
                    }
                }
            }
            if (userId == null) {
                return Result.error(401, "未登录或登录已过期，请重新登录");
            }
            boolean ok = userService.changePassword(userId, oldPassword != null ? oldPassword.trim() : "", newPassword.trim());
            if (ok) {
                return Result.success("密码修改成功，请使用新密码登录");
            }
            return Result.error("修改失败");
        } catch (IllegalArgumentException e) {
            log.error("修改密码失败", e);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("修改密码失败", e);
            return Result.error("修改失败，请稍后重试");
        }
    }

    @PostMapping("/delete")
    @Operation(summary = "注销账号")
    public Result<String> deleteAccount(@RequestHeader(value = "Authorization", required = false) String token) {
        try {
            // 从token中获取用户ID
            Long userId = null;
            User user = null;
            
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);

                // 方法1: 尝试从缓存中获取用户信息
                try {
                    user = userService.getUserByToken(token);
                    if (user != null) {
                        userId = user.getId();
                    }
                } catch (Exception e) {
                    log.debug("注销账号: 从缓存获取用户失败: {}", e.toString());
                }

                // 方法2: 如果缓存中没有，从token解析username，然后查询数据库
                if (user == null) {
                    try {
                        String tokenUsername = jwtUtil.getUsernameFromToken(token);

                        // 先尝试从user表查询（通过phone，因为注册时token是用phone生成的）
                        user = userService.getUserByPhone(tokenUsername);
                        if (user == null) {
                            // 如果通过phone找不到，尝试通过username查找（兼容旧用户）
                            user = userService.getUserByUsername(tokenUsername);
                        }

                        if (user != null) {
                            userId = user.getId();
                        }
                    } catch (Exception e) {
                        log.error("注销账号: 从token解析用户信息失败", e);
                    }
                }
            }

            // 如果无法从token获取，返回错误
            if (userId == null) {
                return Result.error("无法获取用户信息，请重新登录");
            }

            // 删除用户
            boolean deleted = userService.deleteUser(userId);
            if (deleted) {
                // 清除用户缓存
                userService.clearUserCache(userId);
                return Result.success("账号注销成功");
            } else {
                return Result.error("注销失败，用户不存在");
            }
        } catch (Exception e) {
            log.error("注销账号失败", e);
            return Result.error("注销失败，请稍后重试");
        }
    }

    @PostMapping("/update")
    @Operation(summary = "更新用户信息")
    public Result<User> updateUser(@RequestBody User user, HttpServletRequest request) {
        try {
            // 安全修复：原实现信任 body 中的 id，任意登录用户可改他人资料，
            // 且 updateById 为全字段更新——body 可带 points/role/status/password 造成提权。
            // 修复：归属强制取登录态；非管理端仅允许改个人资料字段。
            Long loginId = loginUserId(request);
            if (loginId == null) {
                return Result.error(401, "用户未登录或token无效");
            }
            if (isStaffOrAdmin(request)) {
                // 管理端：保留原逻辑（管理后台编辑用户）
                if (user.getId() == null) {
                    return Result.error("用户ID不能为空");
                }
            } else {
                // 普通用户：只能改自己的资料，且仅限安全字段
                user.setId(loginId);
                User safe = new User();
                safe.setId(loginId);
                safe.setNickname(user.getNickname());
                safe.setAvatar(user.getAvatar());
                safe.setAvatarUrl(user.getAvatarUrl());
                safe.setBackgroundImage(user.getBackgroundImage());
                safe.setGender(user.getGender());
                safe.setSignature(user.getSignature());
                safe.setInterestTags(user.getInterestTags());
                user = safe;
            }

            // 检查用户ID
            if (user.getId() == null) {
                return Result.error("用户ID不能为空");
            }

            // 检查头像URL是否是临时路径，如果是则不更新头像
            if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                String avatar = user.getAvatar();
                if (avatar.contains("/tmp/") || avatar.contains("/__tmp__/") ||
                    avatar.startsWith("http://tmp/") || avatar.startsWith("http://__tmp__/") ||
                    avatar.startsWith("https://tmp/") || avatar.startsWith("https://__tmp__/")) {
                    // 不更新头像，保持原有头像
                    User existingUser = userService.getUserById(user.getId());
                    if (existingUser != null) {
                        user.setAvatar(existingUser.getAvatar());
                    } else {
                        user.setAvatar(null);
                    }
                }
            }

            boolean success = userService.updateUser(user);

            if (!success) {
                return Result.error("更新用户信息失败");
            }

            // 重新获取更新后的用户信息
            User updatedUser = userService.getUserById(user.getId());

            // 隐藏密码
            if (updatedUser != null) {
                updatedUser.setPassword(null);
            }
            return Result.success(updatedUser);
        } catch (IllegalArgumentException e) {
            log.error("更新用户信息失败", e);
            // 昵称重复等业务异常
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("更新用户信息失败", e);
            return Result.error("更新用户信息失败，请稍后重试");
        }
    }

    @GetMapping("/stats")
    @Operation(summary = "获取用户统计信息")
    public Result<Map<String, Object>> getUserStats(@RequestParam(required = false) Long userId, HttpServletRequest httpRequest) {
        try {
            if (userId == null) {
                String token = getTokenFromRequest(httpRequest);
                if (token != null && !token.isEmpty()) {
                    try {
                        User user = userService.getUserByToken(token);
                        if (user != null) {
                            userId = user.getId();
                        } else {
                            String tokenUsername = jwtUtil.getUsernameFromToken(token);
                            user = userService.getUserByPhone(tokenUsername);
                            if (user == null) {
                                user = userService.getUserByUsername(tokenUsername);
                            }
                            if (user != null) {
                                userId = user.getId();
                            }
                        }
                    } catch (Exception ignored) {
                    }
                }
            }

            if (userId == null) {
                return Result.error(1, "请先登录");
            }

            Map<String, Integer> stats = userService.getUserStats(userId);

            Map<String, Object> result = new HashMap<>();
            result.put("followCount", stats.get("followCount"));
            result.put("fansCount", stats.get("fansCount"));
            result.put("postCount", stats.get("postCount"));
            result.put("likeCount", stats.get("likeCount"));

            result.put("follows", stats.get("followCount"));
            result.put("fans", stats.get("fansCount"));
            result.put("dynamics", stats.get("postCount"));
            result.put("likes", stats.get("likeCount"));

            return Result.success(result);
        } catch (Exception e) {
            log.error("获取用户统计失败", e);
            log.error("获取用户统计失败: {}", e.getMessage());
            return Result.error("获取用户统计失败，请稍后重试");
        }
    }

    @PostMapping("/wxapp/login")
    @Operation(summary = "微信小程序登录")
    public Result<Map<String, Object>> wxappLogin(@RequestBody Map<String, String> loginData) {
        return com.pethome.common.SafeCall.of(() -> {
            String code = loginData.get("code");

            if (code == null || code.trim().isEmpty()) {
                return Result.error(1, "授权码不能为空");
            }

            // 调用用户服务进行微信登录
            String token;
            try {
                token = userService.loginByWechat(code);
                // 现在loginByWechat应该总是返回token或抛出异常，不应该返回null
                if (token == null) {
                    log.error("微信登录失败：userService.loginByWechat返回null（这不应该发生）");
                    return Result.error(1, "微信登录失败：服务返回null，请查看后端日志");
                }
            } catch (RuntimeException e) {
                log.error("微信登录异常", e);
                return Result.error(1, "微信登录失败，请稍后重试");
            } catch (Exception e) {
                log.error("微信登录异常", e);
                return Result.error(1, "微信登录失败，请稍后重试");
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
                return Result.error(1, "微信登录失败，无法获取用户ID");
            }

            // 获取用户详细信息
            User user = userService.getUserById(userId);
            if (user == null) {
                return Result.error(1, "微信登录失败，用户不存在");
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
            userInfo.put("avatar", user.getAvatar() != null ? user.getAvatar() : "/static/images/garfield-default-avatar.png");
            userInfo.put("username", user.getUsername() != null ? user.getUsername() : "");
            if (user.getPhone() != null) {
                userInfo.put("phone", user.getPhone());
            }
            data.put("userInfo", userInfo);

            return Result.success(data);
        }, "微信登录失败，请稍后重试", log);
    }

    @PostMapping("/wxapp/authorize")
    @Operation(summary = "微信小程序授权")
    public Result<Map<String, Object>> wxappAuthorize(@RequestBody Map<String, String> authData) {
        return com.pethome.common.SafeCall.of(() -> {
            // 这里应该调用微信授权服务
            Map<String, Object> result = new HashMap<>();
            result.put("token", "dummy-token");
            result.put("userId", 1);
            return Result.success(result);
        }, "微信授权失败，请稍后重试", log);
    }

    @GetMapping("/detail")
    @Operation(summary = "获取用户详情")
    public Result<User> getUserDetail(@RequestParam(required = false) String token,
                                      @RequestParam(required = false) Long userId,
                                      HttpServletRequest request) {
        return com.pethome.common.SafeCall.of(() -> {
            // 安全修复：原实现匿名可达且支持任意 userId 查询，并回退 getUserById(1L)（泄露1号用户全部信息）。
            // 修复：本人可看完整资料；查他人仅返回脱敏公开字段；未登录拒绝。
            Long currentUserId = getUserIdFromToken(token);
            if (currentUserId == null) {
                currentUserId = loginUserId(request);
            }
            if (currentUserId == null) {
                return Result.error(401, "请先登录");
            }
            Long targetId = userId != null ? userId : currentUserId;
            User user = userService.getUserById(targetId);
            if (user == null) {
                return Result.error("用户不存在");
            }

            // 隐藏密码
            user.setPassword(null);
            if (!isStaffOrAdmin(request) && !currentUserId.equals(targetId)) {
                maskUserForPublic(user);
            }
            return Result.success(user);
        }, "获取用户详情失败，请稍后重试", log);
    }

    @GetMapping("/amount")
    @Operation(summary = "获取用户资产")
    public Result<Map<String, Object>> getUserAmount(@RequestParam(required = false) String token) {
        return com.pethome.common.SafeCall.of(() -> {
            // 这里应该获取用户的资产信息
            Map<String, Object> amount = new HashMap<>();
            amount.put("balance", 0.0);
            amount.put("points", 0);
            return Result.success(amount);
        }, "获取用户资产失败，请稍后重试", log);
    }

    @GetMapping("/list")
    @Operation(summary = "获取用户列表（分页）")
    public Result<Map<String, Object>> getUserList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            HttpServletRequest request) {
        // 安全修复：用户列表含手机号等 PII，属管理端能力，仅限 staff/admin（防止普通用户枚举全量用户）
        if (!isStaffOrAdmin(request)) {
            return Result.error(403, "无权获取用户列表");
        }
        return com.pethome.common.SafeCall.of(() -> {
            // 使用MyBatis-Plus分页查询（支持关键字跨页搜索：昵称/手机号/用户名）
            Page<User> userPage = new Page<>(page, size);
            IPage<User> result = userService.getUserList(userPage, keyword);

            // 统一补充状态文案，前端不再自行猜测
            List<Map<String, Object>> users = new ArrayList<>();
            if (result.getRecords() != null) {
                for (User u : result.getRecords()) {
                    if (u == null) continue;
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", u.getId());
                    item.put("username", u.getUsername());
                    item.put("nickname", u.getNickname());
                    item.put("avatar", u.getAvatar());
                    item.put("phone", u.getPhone());
                    item.put("gender", u.getGender());
                    item.put("role", u.getRole());
                    item.put("points", u.getPoints());
                    item.put("memberLevel", u.getMemberLevel());
                    item.put("charm", u.getCharm());
                    item.put("status", u.getStatus());
                    item.put("statusText", (u.getStatus() != null && u.getStatus() == 1) ? "正常" : "禁用");
                    item.put("createTime", u.getCreateTime());
                    item.put("updateTime", u.getUpdateTime());
                    users.add(item);
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("users", users);
            response.put("total", result.getTotal());
            response.put("pages", result.getPages());
            response.put("current", result.getCurrent());
            response.put("size", result.getSize());
            
            return Result.success(response);
        }, "获取用户列表失败，请稍后重试", log);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取用户信息")
    public Result<User> getUserById(@PathVariable Long id, HttpServletRequest request) {
        // 安全修复：原实现向任意登录用户返回完整 User（含手机号/openid/积分）。
        // 修复：本人或 staff/admin 可看完整资料，查他人仅返回脱敏公开字段。
        return com.pethome.common.SafeCall.of(() -> {
            User user = userService.getUserById(id);
            if (user == null) {
                return Result.error("用户不存在");
            }
            // 隐藏密码
            user.setPassword(null);
            Long currentUserId = loginUserId(request);
            boolean self = currentUserId != null && currentUserId.equals(id);
            if (!isStaffOrAdmin(request) && !self) {
                maskUserForPublic(user);
            }
            return Result.success(user);
        }, "获取用户信息失败，请稍后重试", log);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新用户信息")
    public Result<User> updateUserById(@PathVariable Long id, @RequestBody User user, HttpServletRequest request) {
        // 安全修复：更新任意用户属管理端能力（updateById 全字段更新可改 points/role/status），
        // 仅限 staff/admin，防止普通用户篡改他人资料/提权
        if (!isStaffOrAdmin(request)) {
            return Result.error(403, "无权更新用户信息");
        }
        try {
            user.setId(id);
            // 强制归一化：只接受 0/1，避免前端异常值导致“禁用后回弹正常”
            if (user.getStatus() != null) {
                user.setStatus(user.getStatus() == 1 ? 1 : 0);
            }
            boolean success = userService.updateUser(user);
            if (!success) {
                return Result.error("更新用户信息失败");
            }
            // 重新获取更新后的用户信息
            User updatedUser = userService.getUserById(id);
            // 隐藏密码
            if (updatedUser != null) {
                updatedUser.setPassword(null);
            }
            return Result.success(updatedUser);
        } catch (IllegalArgumentException e) {
            log.error("操作失败", e);
            // 昵称重复等业务异常
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("更新用户信息失败", e);
            return Result.error("更新用户信息失败，请稍后重试");
        }
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新用户状态（1正常，0禁用）")
    public Result<Boolean> updateUserStatus(@PathVariable Long id, @RequestParam Integer status,
                                            HttpServletRequest request) {
        // 安全修复：禁用/启用账号属管理端能力，仅限 staff/admin（防止普通用户禁用他人账号）
        if (!isStaffOrAdmin(request)) {
            return Result.error(403, "无权更新用户状态");
        }
        return com.pethome.common.SafeCall.of(() -> {
            if (status == null || (status != 0 && status != 1)) {
                return Result.error("状态参数错误，仅支持0(禁用)/1(正常)");
            }
            User existing = userService.getUserById(id);
            if (existing == null) {
                return Result.error("用户不存在");
            }
            existing.setStatus(status);
            boolean ok = userService.updateUser(existing);
            if (!ok) {
                return Result.error("更新用户状态失败");
            }
            return Result.success(true);
        }, "更新用户状态失败，请稍后重试", log);
    }

    @GetMapping("/{userId}/following")
    @Operation(summary = "获取用户关注列表")
    public Result<Map<String, Object>> getFollowingList(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) Long currentUserId) {
        return com.pethome.common.SafeCall.of(() -> {
            List<UserFollow> allFollows = userFollowService.getFollowingList(userId);
            
            // 手动分页
            int start = (page - 1) * size;
            int end = Math.min(start + size, allFollows.size());
            List<UserFollow> pagedFollows = allFollows.subList(Math.min(start, allFollows.size()), end);
            
            // 转换为前端格式并检查是否已关注
            List<Map<String, Object>> resultList = new ArrayList<>();
            for (UserFollow follow : pagedFollows) {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", follow.getFollowingId());
                // 使用昵称，如果没有则使用用户名
                String displayName = follow.getFollowingName() != null ? follow.getFollowingName() : "";
                // 如果displayName是手机号格式，尝试从数据库获取昵称
                if (displayName.matches("^1[3-9]\\d{9}$")) {
                    com.pethome.entity.User user = userService.getUserById(follow.getFollowingId());
                    if (user != null && user.getNickname() != null && !user.getNickname().isEmpty()) {
                        displayName = user.getNickname();
                    }
                }
                userInfo.put("username", displayName);
                // 头像URL，如果为空则使用默认头像路径
                String avatarUrl = follow.getFollowingAvatar() != null && !follow.getFollowingAvatar().isEmpty() 
                    ? follow.getFollowingAvatar() 
                    : "/static/images/garfield-default-avatar.png";
                // 确保头像URL有效（不是临时标识符）
                if (avatarUrl.length() < 10 || 
                    (!avatarUrl.startsWith("http://") && !avatarUrl.startsWith("https://") && !avatarUrl.startsWith("/"))) {
                    avatarUrl = "/static/images/garfield-default-avatar.png";
                }
                userInfo.put("avatarUrl", avatarUrl);
                userInfo.put("avatar", avatarUrl); // 同时提供avatar字段以兼容前端
                // 获取用户信息以获取signature
                User followUser = userService.getUserById(follow.getFollowingId());
                userInfo.put("signature", followUser != null && followUser.getSignature() != null ? followUser.getSignature() : "");
                userInfo.put("bio", followUser != null && followUser.getSignature() != null ? followUser.getSignature() : ""); // bio 使用 signature 的值
                
                // 获取粉丝数
                int fansCount = userFollowService.getFollowerCount(follow.getFollowingId());
                userInfo.put("fansCount", fansCount);
                
                // 判断当前用户是否已关注列表中的用户（关注列表中的用户都是已关注的，所以这里应该始终为true）
                userInfo.put("isFollowing", true);
                
                resultList.add(userInfo);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("list", resultList);
            response.put("hasMore", end < allFollows.size());
            
            return Result.success(response);
        }, "获取关注列表失败，请稍后重试", log);
    }

    @GetMapping("/{userId}/fans")
    @Operation(summary = "获取用户粉丝列表")
    public Result<Map<String, Object>> getFansList(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) Long currentUserId) {
        return com.pethome.common.SafeCall.of(() -> {
            List<UserFollow> allFans = userFollowService.getFollowerList(userId);

            // 手动分页
            int start = (page - 1) * size;
            int end = Math.min(start + size, allFans != null ? allFans.size() : 0);
            List<UserFollow> pagedFans = (allFans != null && !allFans.isEmpty())
                ? allFans.subList(Math.min(start, allFans.size()), end)
                : new ArrayList<>();
            
            // 转换为前端格式并检查是否已关注
            List<Map<String, Object>> resultList = new ArrayList<>();
            for (UserFollow follow : pagedFans) {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", follow.getFollowerId());
                // 使用昵称，如果没有则使用用户名
                String displayName = follow.getFollowerName() != null ? follow.getFollowerName() : "";
                // 如果displayName是手机号格式，尝试从数据库获取昵称
                if (displayName.matches("^1[3-9]\\d{9}$")) {
                    com.pethome.entity.User user = userService.getUserById(follow.getFollowerId());
                    if (user != null && user.getNickname() != null && !user.getNickname().isEmpty()) {
                        displayName = user.getNickname();
                    }
                }
                userInfo.put("username", displayName);
                // 头像URL，如果为空则使用默认头像路径
                String avatarUrl = follow.getFollowerAvatar() != null && !follow.getFollowerAvatar().isEmpty() 
                    ? follow.getFollowerAvatar() 
                    : "/static/images/garfield-default-avatar.png";
                // 确保头像URL有效（不是临时标识符）
                if (avatarUrl.length() < 10 || 
                    (!avatarUrl.startsWith("http://") && !avatarUrl.startsWith("https://") && !avatarUrl.startsWith("/"))) {
                    avatarUrl = "/static/images/garfield-default-avatar.png";
                }
                userInfo.put("avatarUrl", avatarUrl);
                userInfo.put("avatar", avatarUrl); // 同时提供avatar字段以兼容前端
                // 获取用户信息以获取signature
                User followUser = userService.getUserById(follow.getFollowerId());
                userInfo.put("signature", followUser != null && followUser.getSignature() != null ? followUser.getSignature() : "");
                userInfo.put("bio", followUser != null && followUser.getSignature() != null ? followUser.getSignature() : ""); // bio 使用 signature 的值
                
                // 获取该粉丝的粉丝数
                int fansCount = userFollowService.getFollowerCount(follow.getFollowerId());
                userInfo.put("fansCount", fansCount);
                
                // 判断当前用户是否已关注列表中的用户（用于显示"回关"或"互相关注"）
                boolean isFollowing = false;
                boolean isMutualFollow = false; // 是否互相关注
                if (currentUserId != null && currentUserId != 0) {
                    // 检查当前用户是否关注了这个粉丝
                    isFollowing = userFollowService.isFollowing(currentUserId, follow.getFollowerId());
                    // 检查这个粉丝是否也关注了当前用户（互相关注）
                    boolean fanIsFollowingMe = userFollowService.isFollowing(follow.getFollowerId(), currentUserId);
                    isMutualFollow = isFollowing && fanIsFollowingMe;
                }
                userInfo.put("isFollowing", isFollowing);
                userInfo.put("isMutualFollow", isMutualFollow); // 添加互相关注标识

                resultList.add(userInfo);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("list", resultList);
            response.put("hasMore", allFans != null && end < allFans.size());

            return Result.success(response);
        }, "获取粉丝列表失败，请稍后重试", log);
    }

    @GetMapping("/{userId}/posts")
    @Operation(summary = "获取用户动态列表")
    public Result<Map<String, Object>> getUserPosts(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        return com.pethome.common.SafeCall.of(() -> {
            // 使用PostService的分页查询
            Page<Post> postPage = new Page<>(page, size);
            IPage<Post> result = postService.getPostList(postPage, null, userId);
            
            // 转换为前端格式（只需要id, content, image）
            List<Map<String, Object>> resultList = new ArrayList<>();
            for (Post post : result.getRecords()) {
                Map<String, Object> postInfo = new HashMap<>();
                postInfo.put("id", post.getId());
                postInfo.put("content", post.getContent() != null ? post.getContent() : "");
                // 如果images是多个，取第一个作为image
                String image = null;
                if (post.getImages() != null && !post.getImages().isEmpty()) {
                    String[] images = post.getImages().split(",");
                    if (images.length > 0) {
                        image = images[0].trim();
                    }
                }
                postInfo.put("image", image);
                resultList.add(postInfo);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("list", resultList);
            response.put("hasMore", result.getCurrent() < result.getPages());
            
            return Result.success(response);
        }, "获取动态列表失败，请稍后重试", log);
    }
    
    @PostMapping("/getPhoneNumber")
    @Operation(summary = "获取微信手机号")
    public Result<Map<String, String>> getPhoneNumber(
            @RequestBody Map<String, String> requestData,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            String code = requestData.get("code");
            if (code == null || code.isEmpty()) {
                return Result.error("code不能为空");
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
            ResponseEntity<Map<String, Object>> response = rt.exchange(url, HttpMethod.POST, entity, (Class<Map<String, Object>>)(Class<?>)Map.class);
            Map<String, Object> responseBody = response.getBody();

            if (responseBody != null) {
                Integer errcode = (Integer) responseBody.get("errcode");
                if (errcode != null && errcode == 0) {
                    Map<String, Object> phoneInfo = (Map<String, Object>) responseBody.get("phone_info");
                    if (phoneInfo != null) {
                        String phoneNumber = (String) phoneInfo.get("phoneNumber");
                        String purePhoneNumber = (String) phoneInfo.get("purePhoneNumber");
                        String countryCode = (String) phoneInfo.get("countryCode");

                        // 更新当前用户的手机号（通过请求头获取token）
                        if (authHeader != null && authHeader.startsWith("Bearer ")) {
                            String token = authHeader.substring(7);
                            User currentUser = userService.getUserByToken(token);
                            if (currentUser == null) {
                                // 如果从token获取不到，尝试从token解析phone
                                try {
                                    String tokenUsername = jwtUtil.getUsernameFromToken(token);
                                    currentUser = userService.getUserByPhone(tokenUsername);
                                    if (currentUser == null) {
                                        currentUser = userService.getUserByUsername(tokenUsername);
                                    }
                                } catch (Exception e) {
                                    log.debug("getPhoneNumber: token 解析用户失败: {}", e.toString());
                                }
                            }
                            if (currentUser != null) {
                                // 更新用户手机号
                                currentUser.setPhone(purePhoneNumber != null ? purePhoneNumber : phoneNumber);
                                userService.updateUser(currentUser);
                            }
                        }

                        Map<String, String> result = new HashMap<>();
                        result.put("phone", purePhoneNumber != null ? purePhoneNumber : phoneNumber);
                        result.put("countryCode", countryCode != null ? countryCode : "86");
                        return Result.success(result);
                    } else {
                        return Result.error("微信API返回数据格式错误");
                    }
                } else {
                    String errmsg = (String) responseBody.get("errmsg");
                    log.error("获取微信手机号失败: 微信API错误 errcode={}, errmsg={}", errcode, errmsg);
                    return Result.error("获取手机号失败: " + (errmsg != null ? errmsg : "未知错误"));
                }
            } else {
                return Result.error("微信API返回为空");
            }
        } catch (Exception e) {
            log.error("获取微信手机号异常", e);
            return Result.error("获取手机号失败，请稍后重试");
        }
    }

    @PostMapping("/bind-phone")
    @Operation(summary = "绑定手机号（使用其它号码时需短信验证码，需登录）")
    public Result<String> bindPhone(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                    @RequestBody Map<String, String> body) {
        return com.pethome.common.SafeCall.of(() -> {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return Result.error("请先登录");
            }
            String token = authHeader.substring(7);
            User currentUser = userService.getUserByToken(token);
            if (currentUser == null) {
                try {
                    String subject = jwtUtil.getUsernameFromToken(token);
                    currentUser = userService.getUserByPhone(subject);
                    if (currentUser == null) currentUser = userService.getUserByUsername(subject);
                } catch (Exception e) {
                    return Result.error("登录已过期，请重新登录");
                }
            }
            if (currentUser == null) {
                return Result.error("请先登录");
            }
            String phone = body != null ? body.get("phone") : null;
            String smsCode = body != null ? body.get("smsCode") : null;
            if (phone == null || phone.trim().isEmpty()) {
                return Result.error("手机号不能为空");
            }
            if (smsCode == null || smsCode.trim().isEmpty()) {
                return Result.error("验证码不能为空");
            }
            if (smsService == null) {
                return Result.error("短信服务未配置，暂不支持绑定其他号码");
            }
            if (!smsService.verifyCode(phone.trim(), smsCode.trim())) {
                return Result.error("验证码错误或已过期");
            }
            // 已绑定的手机号不能再被其他账号绑定
            User otherUser = userService.getUserByPhone(phone.trim());
            if (otherUser != null && !otherUser.getId().equals(currentUser.getId())) {
                return Result.error("该手机号已被绑定");
            }
            currentUser.setPhone(phone.trim());
            boolean ok = userService.updateUser(currentUser);
            if (ok) {
                return Result.success("绑定成功");
            }
            return Result.error("绑定失败");
        }, "绑定失败，请稍后重试", log);
    }

    @PostMapping("/unbind-phone")
    @Operation(summary = "解绑手机号（需登录）。解绑后该手机号可被其他账号绑定；当前账号可再绑定其他手机号。")
    public Result<String> unbindPhone(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        return com.pethome.common.SafeCall.of(() -> {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return Result.error("请先登录");
            }
            String token = authHeader.substring(7);
            User currentUser = userService.getUserByToken(token);
            if (currentUser == null) {
                try {
                    String subject = jwtUtil.getUsernameFromToken(token);
                    currentUser = userService.getUserByPhone(subject);
                    if (currentUser == null) currentUser = userService.getUserByUsername(subject);
                } catch (Exception e) {
                    return Result.error("登录已过期，请重新登录");
                }
            }
            if (currentUser == null) {
                return Result.error("请先登录");
            }
            if (currentUser.getPhone() == null || currentUser.getPhone().trim().isEmpty()) {
                return Result.error("当前账号未绑定手机号，无需解绑");
            }
            boolean ok = userService.unbindPhoneByUserId(currentUser.getId());
            if (ok) {
                return Result.success("解绑成功");
            }
            return Result.error("解绑失败");
        }, "解绑失败，请稍后重试", log);
    }

    @PostMapping("/verify-password")
    @Operation(summary = "验证当前账号登录密码（用于修改账号等敏感操作前）")
    public Result<String> verifyPassword(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                          @RequestBody(required = false) Map<String, String> body) {
        return com.pethome.common.SafeCall.of(() -> {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return Result.error("请先登录");
            }
            String token = authHeader.substring(7).trim();
            if (token.isEmpty()) {
                return Result.error("请先登录");
            }
            User currentUser = userService.getUserByToken(token);
            if (currentUser == null) {
                try {
                    String subject = jwtUtil.getUsernameFromToken(token);
                    if (subject != null && !subject.isEmpty()) {
                        currentUser = userService.getUserByPhone(subject);
                        if (currentUser == null) currentUser = userService.getUserByUsername(subject);
                    }
                } catch (Exception e) {
                    return Result.error("登录已过期，请重新登录");
                }
            }
            if (currentUser == null) {
                return Result.error("请先登录");
            }
            String password = (body != null && body.containsKey("password")) ? body.get("password") : null;
            if (password == null || password.trim().isEmpty()) {
                return Result.error("请输入密码");
            }
            String dbPwd = currentUser.getPassword();
            if (dbPwd == null || dbPwd.trim().isEmpty()) {
                return Result.error("该账号未设置密码，请先到「修改密码」中设置密码");
            }
            // 统一走 UserService.verifyPassword（BCrypt 校验 + 旧 MD5 自动升级，拒绝明文 fallback）
            if (!userService.verifyPassword(password.trim(), currentUser)) {
                return Result.error("密码错误");
            }
            return Result.success("验证成功");
        }, "验证失败，请稍后重试", log);
    }

    @PostMapping("/change-username")
    @Operation(summary = "修改账号（格式：字母或下划线开头，6-20位，仅字母数字下划线减号）")
    public Result<String> changeUsername(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                         @RequestBody Map<String, String> body) {
        return com.pethome.common.SafeCall.of(() -> {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return Result.error("请先登录");
            }
            String token = authHeader.substring(7);
            User currentUser = userService.getUserByToken(token);
            if (currentUser == null) {
                try {
                    String subject = jwtUtil.getUsernameFromToken(token);
                    currentUser = userService.getUserByPhone(subject);
                    if (currentUser == null) currentUser = userService.getUserByUsername(subject);
                } catch (Exception e) {
                    return Result.error("登录已过期，请重新登录");
                }
            }
            if (currentUser == null) {
                return Result.error("请先登录");
            }
            String newUsername = body != null ? body.get("newUsername") : null;
            String err = userService.changeUsername(currentUser.getId(), newUsername);
            if (err != null) {
                return Result.error(err);
            }
            return Result.success("修改成功，请使用新账号登录");
        }, "修改失败，请稍后重试", log);
    }
    
    /**
     * 获取微信Access Token
     * 注意：实际生产环境中应该缓存token，避免频繁请求
     */
    private String getAccessToken() {
        try {
            RestTemplate rt = getRestTemplate();

            String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=" +
                        wechatAppId + "&secret=" + wechatSecret;

            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = rt.getForEntity(url, (Class<Map<String, Object>>)(Class<?>)Map.class);
            Map<String, Object> responseBody = response.getBody();

            if (responseBody != null) {
                String accessToken = (String) responseBody.get("access_token");
                if (accessToken != null) {
                    return accessToken;
                } else {
                    String errmsg = (String) responseBody.get("errmsg");
                    log.error("获取Access Token失败: errmsg={}", errmsg);
                    throw new RuntimeException("获取Access Token失败: " + errmsg);
                }
            } else {
                throw new RuntimeException("获取Access Token返回为空");
            }
        } catch (Exception e) {
            log.error("获取Access Token异常", e);
            throw new RuntimeException("获取Access Token异常: " + e.getMessage());
        }
    }
    
    /**
     * 从token中获取用户ID
     */
    private Long getUserIdFromToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            // 首先尝试从Redis缓存获取用户信息（如果token是登录时存储的）
            try {
                User cachedUser = userService.getUserByToken(token);
                if (cachedUser != null) {
                    return cachedUser.getId();
                }
            } catch (Exception e) {
                log.debug("getUserIdFromToken: 从缓存获取用户失败: {}", e.toString());
            }

            // 如果缓存中没有，尝试解析JWT token
            try {
                Claims claims = jwtUtil.parseToken(token);
                if (claims != null) {
                    String subject = claims.getSubject();
                    if (subject != null) {
                        // 先尝试通过username查找
                        User user = userService.getUserByUsername(subject);
                        if (user == null) {
                            // 如果username找不到，尝试通过phone查找（新注册用户的token subject是phone）
                            user = userService.getUserByPhone(subject);
                        }
                        if (user != null) {
                            return user.getId();
                        }
                    }
                }
            } catch (Exception e) {
                log.error("getUserIdFromToken: Token解析失败", e);
            }
        }
        return null;
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        if (request == null) return null;
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7).trim();
        }
        return null;
    }

    /** 从拦截器写入的登录态属性解析当前用户 ID（普通用户 Redis token） */
    private Long loginUserId(HttpServletRequest request) {
        Object attr = request.getAttribute("userId");
        return attr instanceof Number ? ((Number) attr).longValue() : null;
    }

    /** staff/admin 判定：RoleInterceptor 在管理端登录后写入 adminId attribute */
    private boolean isStaffOrAdmin(HttpServletRequest request) {
        return request.getAttribute("adminId") instanceof Number;
    }

    /** 非管理端视角的用户脱敏：抹掉手机号/邮箱/openid/激活码/积分/状态等敏感字段 */
    private void maskUserForPublic(User user) {
        if (user == null) return;
        user.setPassword(null);
        user.setPhone(null);
        user.setPhoneNumber(null);
        user.setEmail(null);
        user.setOpenid(null);
        user.setUnionid(null);
        user.setActivationCode(null);
        user.setPoints(null);
        user.setStatus(null);
        user.setType(null);
    }
    
    @GetMapping("/points/records")
    @Operation(summary = "获取用户积分记录")
    public Result<Map<String, Object>> getPointsRecords(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false, defaultValue = "earn") String type,
            HttpServletRequest request) {
        return com.pethome.common.SafeCall.of(() -> {
            // 从token获取用户ID
            Long currentUserId = getUserIdFromToken(token);
            if (currentUserId == null && userId == null) {
                return Result.error(401, "未登录");
            }

            // 安全修复：积分记录属个人数据，非管理端强制以登录态为准（原信任 query userId 可查他人消费记录）
            Long targetUserId;
            if (isStaffOrAdmin(request)) {
                targetUserId = userId != null ? userId : currentUserId;
            } else {
                targetUserId = currentUserId;
            }

            Map<String, Object> result = new HashMap<>();
            List<PointsRecord> records;
            
            if ("earn".equals(type)) {
                records = pointsRecordService.getEarnRecords(targetUserId);
            } else if ("spend".equals(type)) {
                records = pointsRecordService.getSpendRecords(targetUserId);
            } else {
                return Result.error("类型参数错误，只能是 'earn' 或 'spend'");
            }
            
            // 转换为前端需要的格式
            List<Map<String, Object>> recordList = new ArrayList<>();
            for (PointsRecord record : records) {
                Map<String, Object> recordMap = new HashMap<>();
                recordMap.put("id", record.getId());
                recordMap.put("description", record.getDescription());
                recordMap.put("amount", record.getPoints() != null ? record.getPoints() : 0); // 使用points字段，但返回给前端时用amount
                recordMap.put("type", record.getType());
                recordMap.put("source", record.getSource() != null ? record.getSource() : ""); // source可能为null
                recordMap.put("createTime", record.getCreateTime());
                recordMap.put("time", record.getCreateTime()); // 兼容前端
                recordList.add(recordMap);
            }
            
            result.put("records", recordList);
            
            // 获取统计信息
            Map<String, Object> statistics = pointsRecordService.getPointsStatistics(targetUserId);
            result.put("statistics", statistics);
            
            return Result.success(result);
        }, "获取积分记录失败，请稍后重试", log);
    }
    
    @GetMapping("/points/statistics")
    @Operation(summary = "获取用户积分统计")
    public Result<Map<String, Object>> getPointsStatistics(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam(required = false) Long userId,
            HttpServletRequest request) {
        return com.pethome.common.SafeCall.of(() -> {
            // 从token获取用户ID
            Long currentUserId = getUserIdFromToken(token);
            if (currentUserId == null && userId == null) {
                return Result.error(401, "未登录");
            }

            // 安全修复：积分余额/统计属个人数据，非管理端强制以登录态为准
            Long targetUserId;
            if (isStaffOrAdmin(request)) {
                targetUserId = userId != null ? userId : currentUserId;
            } else {
                targetUserId = currentUserId;
            }
            
            // 先清除用户缓存，再查库，保证与签到/任务中心显示的余额一致（避免 Redis 缓存旧积分）
            userService.clearUserCache(targetUserId);
            User user = userService.getUserById(targetUserId);
            Integer currentBalance = user != null ? user.getPoints() : 0;
            
            // 获取统计信息（来自 points_record 的累计）
            Map<String, Object> statistics = pointsRecordService.getPointsStatistics(targetUserId);
            statistics.put("currentBalance", currentBalance);
            
            // 无流水时保持逻辑一致：当前剩余有值则累计获取至少等于当前剩余
            Object te = statistics.get("totalEarned");
            Object ts = statistics.get("totalSpent");
            int totalEarned = (te instanceof Number) ? ((Number) te).intValue() : 0;
            int totalSpent = (ts instanceof Number) ? ((Number) ts).intValue() : 0;
            if (totalEarned == 0 && currentBalance != null && currentBalance > 0 && totalSpent == 0) {
                statistics.put("totalEarned", currentBalance);
            }
            
            return Result.success(statistics);
        }, "获取积分统计失败，请稍后重试", log);
    }
    
    /**
     * 手动更新用户的魅力值和等级
     */
    @PostMapping("/update/charm-level")
    @Operation(summary = "更新用户的魅力值和等级")
    public Result<Map<String, Object>> updateCharmAndLevel(
            @RequestHeader(value = "Authorization", required = false) String token) {
        return com.pethome.common.SafeCall.of(() -> {
            Long userId = getUserIdFromToken(token);

            if (userId == null) {
                return Result.error(401, "未登录或登录信息无效");
            }

            // 调用服务更新魅力值和等级
            userService.updateCharmAndLevel(userId);

            // 获取更新后的用户信息
            User user = userService.getUserById(userId);
            if (user == null) {
                return Result.error("用户不存在");
            }

            Map<String, Object> result = new HashMap<>();
            result.put("charm", user.getCharm() != null ? user.getCharm() : 0);
            result.put("level", user.getMemberLevel() != null ? user.getMemberLevel() : 0);
            result.put("points", user.getPoints() != null ? user.getPoints() : 0);

            // 计算总历史积分
            Map<String, Object> statistics = pointsRecordService.getPointsStatistics(userId);
            result.put("totalEarned", statistics.get("totalEarned"));
            result.put("totalSpent", statistics.get("totalSpent"));

            return Result.success("更新成功", result);
        }, "更新失败，请稍后重试", log);
    }
}

