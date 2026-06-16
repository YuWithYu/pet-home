package com.pethome.controller;

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
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.CrossOrigin;
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
import com.pethome.service.UserService;
import com.pethome.service.UserFollowService;
import com.pethome.service.PostService;
import com.pethome.service.AdminService;
import com.pethome.service.PointsRecordService;
import com.pethome.entity.PointsRecord;
import com.pethome.util.JwtUtil;
import com.pethome.util.MD5Encoder;
import io.jsonwebtoken.Claims;

import java.util.ArrayList;
import java.util.List;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/user")  // 统一使用单数路径，对应数据库user表
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
@Api(tags = "用户管理")
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
    private com.pethome.service.SmsService smsService;
    
    @Autowired(required = false)
    private com.pethome.service.AdminPermissionService adminPermissionService;
    
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
    @ApiOperation("获取当前用户信息（支持 user 和 admin 表）")
    public Result<Map<String, Object>> getCurrentUser(
                                       @RequestHeader(value = "Authorization", required = false) String token,
                                       @RequestParam(required = false, value = "userId") Long userId,
                                       @RequestParam(required = false, value = "username") String username,
                                       HttpServletRequest request) {
        try {
            // 【关键修复】如果指定了 userId，直接根据 userId 查询用户（用于查看其他用户的资料）
            // 这个检查必须在 token 解析之前，确保传入的 userId 优先级最高
            System.out.println("========== getCurrentUser 方法被调用 ==========");
            System.out.println("接收到的参数 - userId: " + userId + " (类型: " + (userId != null ? userId.getClass().getSimpleName() : "null") + "), username: " + username);
            System.out.println("token 是否存在: " + (token != null));
            // 从请求参数中直接获取，以防@RequestParam没有正确绑定
            String userIdParam = request.getParameter("userId");
            System.out.println("从request.getParameter获取的userId: " + userIdParam);
            if (userIdParam != null && !userIdParam.isEmpty()) {
                try {
                    Long parsedUserId = Long.parseLong(userIdParam);
                    System.out.println("解析后的userId: " + parsedUserId);
                    if (userId == null || userId == 0) {
                        userId = parsedUserId;
                        System.out.println("使用从request.getParameter解析的userId: " + userId);
                    }
                } catch (NumberFormatException e) {
                    System.err.println("无法解析userId参数: " + userIdParam);
                }
            }
            
            if (userId != null && userId > 0) {
                System.out.println("========== 根据 userId 查询用户（查看他人资料） ==========");
                System.out.println("指定的 userId: " + userId);
                System.out.println("忽略 token，直接查询指定用户");
                
                User targetUser = userService.getUserById(userId);
                if (targetUser == null) {
                    System.out.println("用户不存在，userId: " + userId);
                    return Result.error("用户不存在或已被禁用");
                }
                // 不再自动修复 status：管理员禁用的用户应保持禁用，避免“禁用后又被改回正常”
                
                System.out.println("找到目标用户: ID=" + targetUser.getId() + ", username=" + targetUser.getUsername() + ", nickname=" + targetUser.getNickname());
                
                // 隐藏密码
                targetUser.setPassword(null);
                
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
                userInfo.put("points", targetUser.getPoints() != null ? targetUser.getPoints() : 0);
                userInfo.put("memberLevel", targetUser.getMemberLevel());
                userInfo.put("level", targetUser.getMemberLevel());
                userInfo.put("gender", targetUser.getGender());
                userInfo.put("signature", targetUser.getSignature() != null ? targetUser.getSignature() : "");
                userInfo.put("bio", targetUser.getSignature() != null ? targetUser.getSignature() : ""); // bio 使用 signature 的值
                userInfo.put("phone", targetUser.getPhone() != null ? targetUser.getPhone() : ""); // 添加手机号字段
                userInfo.put("charm", targetUser.getCharm() != null ? targetUser.getCharm() : 0);
                userInfo.put("role", "user");
                userInfo.put("permissions", new String[]{"user"});
                userInfo.put("isAdmin", false);
                
                System.out.println("返回目标用户信息: ID=" + userInfo.get("id") + ", nickname=" + userInfo.get("nickname") + ", username=" + userInfo.get("username"));
                System.out.println("====================================");
                return Result.success(userInfo);
            }
            
            System.out.println("未指定userId或userId无效，继续使用token查询当前用户");
            
            // 优先从 token 中获取用户名
            String tokenUsername = null;
            if (token != null && token.startsWith("Bearer ")) {
                try {
                    token = token.substring(7);
                    tokenUsername = jwtUtil.getUsernameFromToken(token);
                    System.out.println("========== Token 解析 ==========");
                    System.out.println("从 Token 解析出的用户名: " + tokenUsername);
                    System.out.println("====================================");
                } catch (Exception e) {
                    System.err.println("解析 token 失败: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            // 1. 先尝试从 admin 表查询（管理员或服务人员）
            if (tokenUsername != null) {
                System.out.println("尝试从 admin 表查询用户: " + tokenUsername);
                Admin admin = adminService.getByUsername(tokenUsername);
                if (admin != null) {
                    System.out.println("在 admin 表中找到用户: " + admin.getId());
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
                        System.out.println("=== UserController: 开始获取权限 ===");
                        System.out.println("adminId: " + admin.getId() + ", username: " + admin.getUsername() + ", role: " + role);
                        System.out.println("adminPermissionService 是否为 null: " + (adminPermissionService == null));
                        
                        if (adminPermissionService != null) {
                            try {
                                System.out.println("调用 getPermissionsByAdminId, adminId: " + admin.getId());
                                List<com.pethome.entity.AdminPermission> permissions = adminPermissionService.getPermissionsByAdminId(admin.getId());
                                System.out.println("getPermissionsByAdminId 返回结果 - 是否为 null: " + (permissions == null));
                                System.out.println("获取到的权限数量: " + (permissions != null ? permissions.size() : 0));
                                
                                if (permissions != null && !permissions.isEmpty()) {
                                    System.out.println("开始过滤权限...");
                                    permissionCodes = permissions.stream()
                                        .filter(p -> {
                                            boolean valid = p != null && p.getStatus() != null && p.getStatus() == 1;
                                            if (!valid && p != null) {
                                                System.out.println("权限被过滤: id=" + p.getId() + ", code=" + p.getPermissionCode() + ", status=" + p.getStatus());
                                            }
                                            return valid;
                                        })
                                        .map(com.pethome.entity.AdminPermission::getPermissionCode)
                                        .filter(code -> {
                                            boolean valid = code != null && !code.trim().isEmpty();
                                            if (!valid) {
                                                System.out.println("权限代码被过滤（为空）: " + code);
                                            }
                                            return valid;
                                        })
                                        .collect(java.util.stream.Collectors.toList());
                                    System.out.println("过滤后的权限代码: " + permissionCodes);
                                    System.out.println("过滤后的权限数量: " + permissionCodes.size());
                                } else {
                                    System.out.println("权限列表为空或为 null");
                                }
                            } catch (Exception e) {
                                System.err.println("查询权限时出错: " + e.getMessage());
                                e.printStackTrace();
                            }
                        } else {
                            System.err.println("adminPermissionService 为 null，无法查询权限");
                        }
                        
                        // 如果权限表中没有记录，根据角色决定默认权限
                        if (permissionCodes.isEmpty()) {
                            System.out.println("权限代码列表为空，使用默认权限逻辑");
                            if (admin.isSuperAdmin()) {
                                // 超级管理员且没有设置权限，默认拥有所有权限
                                if (adminPermissionService != null) {
                                    List<com.pethome.entity.AdminPermission> allPermissions = adminPermissionService.getAllAvailablePermissions();
                                    permissionCodes = allPermissions.stream()
                                        .map(com.pethome.entity.AdminPermission::getPermissionCode)
                                        .collect(java.util.stream.Collectors.toList());
                                    System.out.println("超级管理员 - 使用所有可用权限: " + permissionCodes.size() + " 个");
                                } else {
                                    permissionCodes.add("admin");
                                    System.out.println("超级管理员 - adminPermissionService 为 null，使用 admin 权限");
                                }
                            } else {
                                // 员工等角色：未在权限管理中设置则不自动分配，由管理员自行在权限设置中分配
                                System.out.println("员工等角色 - 未设置权限，返回空列表，需在权限管理中手动分配");
                            }
                        } else {
                            System.out.println("使用数据库中的权限，不应用默认权限");
                        }
                        // 门店管理员自动拥有门店客服权限（把门店管理员当成客服）
                        if (admin.getServiceStoreId() != null && !permissionCodes.contains("/services/outlet-customer-chat")) {
                            permissionCodes.add("/services/outlet-customer-chat");
                        }
                        // 店铺管理员自动拥有店铺客服权限
                        if (admin.getStoreId() != null && !permissionCodes.contains("/mall/store-customer-chat")) {
                            permissionCodes.add("/mall/store-customer-chat");
                        }
                        System.out.println("=== UserController: 权限获取完成 ===");
                    } catch (Exception permError) {
                        // 如果权限表不存在或其他错误，根据角色使用默认权限
                        System.err.println("获取权限失败（可能是表不存在）: " + permError.getMessage());
                        permError.printStackTrace();
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
                    
                    System.out.println("========== 获取管理员信息 ==========");
                    System.out.println("管理员ID: " + admin.getId());
                    System.out.println("用户名: " + admin.getUsername());
                    System.out.println("角色: " + role);
                    System.out.println("部门: " + admin.getDepartment());
                    System.out.println("状态: " + admin.getStatus());
                    System.out.println("权限列表: " + permissionCodes);
                    System.out.println("====================================");
                    
                    return Result.success(adminInfo);
                } else {
                    System.out.println("在 admin 表中未找到用户: " + tokenUsername);
                }
            }
            
            // 2. 如果 admin 表中没有，再查询 user 表（普通用户）
            User user = null;
            
            // 优先使用 token 中的用户名/手机号查询（微信/手机登录时 subject 可能是手机号）
            if (tokenUsername != null) {
                System.out.println("尝试从 user 表查询用户: " + tokenUsername);
                user = userService.getUserByUsername(tokenUsername);
                if (user == null) {
                    user = userService.getUserByPhone(tokenUsername);
                }
                if (user != null) {
                    System.out.println("在 user 表中找到用户: " + user.getId());
                } else {
                    System.out.println("在 user 表中未找到用户: " + tokenUsername);
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
                    System.err.println("========== 用户不存在错误 ==========");
                    System.err.println("Token 用户名: " + tokenUsername);
                    System.err.println("在 admin 和 user 表中都未找到该用户");
                    System.err.println("====================================");
                    return Result.error(401, "用户不存在: " + tokenUsername + "。请检查账号是否正确或联系管理员");
                } else {
                    // 如果没有提供任何参数，说明是未登录状态，返回401而不是500
                    System.err.println("========== 未登录访问 ==========");
                    System.err.println("未提供有效的 token 或用户ID");
                    System.err.println("====================================");
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
            
            // 调试：打印用户信息
            System.out.println("========== 获取用户信息 ==========");
            System.out.println("用户ID: " + user.getId());
            System.out.println("用户名(username): " + user.getUsername());
            System.out.println("昵称(nickname): " + user.getNickname());
            System.out.println("头像(avatar): " + user.getAvatar());
            System.out.println("背景图(backgroundImage): " + user.getBackgroundImage());
            System.out.println("等级(memberLevel): " + user.getMemberLevel());
            System.out.println("积分(points): " + user.getPoints());
            System.out.println("====================================");
            
            return Result.success(userInfo);
        } catch (Exception e) {
            // 记录异常信息
            System.err.println("========== 获取用户信息异常 ==========");
            System.err.println("异常类型: " + e.getClass().getName());
            System.err.println("异常消息: " + e.getMessage());
            e.printStackTrace();
            System.err.println("====================================");
            
            // 如果是认证相关的异常（如 token 解析失败、JWT 异常等），返回 401
            String exceptionMessage = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if (exceptionMessage.contains("token") || 
                exceptionMessage.contains("jwt") || 
                exceptionMessage.contains("authentication") ||
                exceptionMessage.contains("unauthorized") ||
                e.getClass().getName().contains("JwtException") ||
                e.getClass().getName().contains("Token")) {
                return Result.error(401, "认证失败，请重新登录: " + e.getMessage());
            }
            
            // 其他异常返回 500
            return Result.error(500, "获取用户信息失败: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    @ApiOperation("用户登录（兼容 user 和 admin 表）")
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
                    // 验证密码
                    if (adminService.verifyPassword(password, admin.getPassword())) {
                        try {
                            // 生成JWT Token
                            token = jwtUtil.generateToken(admin.getUsername());
                            System.out.println("Admin 登录成功，生成 token: " + (token != null ? token.substring(0, Math.min(20, token.length())) + "..." : "null"));
                            
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
                            System.err.println("生成 JWT token 失败: " + tokenError.getMessage());
                            tokenError.printStackTrace();
                            return Result.error("登录失败: Token 生成异常 - " + tokenError.getMessage());
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
                    // 验证密码（使用 MD5 加密）
                    String md5Password = MD5Encoder.md5(password);
                    if (md5Password.equals(user.getPassword())) {
                        // 生成 JWT Token（与 admin 登录保持一致）
                        token = jwtUtil.generateToken(user.getUsername());
                        // 将登录状态和用户信息存入 Redis 缓存
                        userService.cacheLoginToken(token, user);
                    } else {
                        user = null; // 密码错误
                    }
                }
            } else if (phone != null) {
                System.out.println("========== 手机号登录 ==========");
                System.out.println("手机号: " + phone);
                user = userService.getUserByPhone(phone);
                // 若手机号未找到，尝试账号（username）登录
                if (user == null) {
                    System.out.println("通过phone未找到用户，尝试通过账号(username)查找");
                    user = userService.getUserByUsername(phone);
                }
                // 仅支持手机号或账号登录，不支持昵称登录（昵称可随意修改，账号用于登录）
                if (user != null) {
                    System.out.println("找到用户: ID=" + user.getId() + ", username=" + user.getUsername() + ", status=" + user.getStatus());
                    if (user.getStatus() != null && user.getStatus() == 1) {
                        // 验证密码（使用 MD5 加密）
                        String md5Password = MD5Encoder.md5(password);
                        
                        // 兼容旧用户的明文密码和新用户的MD5密码
                        boolean passwordMatch = false;
                        if (md5Password.equals(user.getPassword())) {
                            passwordMatch = true;
                        } else if (password.equals(user.getPassword())) {
                            passwordMatch = true;
                            user.setPassword(md5Password);
                            userService.updateUser(user);
                        }
                        
                        if (passwordMatch) {
                            // 生成 JWT Token（使用phone代替username，因为新用户username可能为null）
                            String tokenUsername = user.getPhone() != null ? user.getPhone() : 
                                                   (user.getUsername() != null ? user.getUsername() : String.valueOf(user.getId()));
                            token = jwtUtil.generateToken(tokenUsername);
                            userService.cacheLoginToken(token, user);
                        } else {
                            // 密码错误，返回特定错误信息
                            return Result.error("密码错误");
                        }
                    } else {
                        System.out.println("用户状态已禁用: " + user.getStatus());
                        return Result.error("该账号已被封禁");
                    }
                } else {
                    System.out.println("用户不存在");
                    // 用户不存在，返回特定错误信息
                    return Result.error("当前账号不存在");
                }
                System.out.println("====================================");
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
                // 这种情况不应该发生，因为上面已经处理了所有情况
                return Result.error("登录失败，请检查用户名和密码");
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 打印详细错误信息用于调试
            System.err.println("登录异常详情:");
            System.err.println("异常类型: " + e.getClass().getName());
            System.err.println("异常消息: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("原因: " + e.getCause().getMessage());
            }
            e.printStackTrace();
            return Result.error("登录失败: " + e.getMessage());
        }
    }

    @PostMapping("/register")
    @ApiOperation("用户注册")
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
            // 昵称重复等业务异常
            e.printStackTrace();
            return Result.error(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("注册失败: " + e.getMessage());
        }
    }

    @PostMapping("/forgot-password")
    @ApiOperation("忘记密码：通过手机号+短信验证码重置密码")
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
            return Result.error(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("重置失败: " + e.getMessage());
        }
    }

    @GetMapping("/has-password")
    @ApiOperation("检查当前账号是否已设置密码（微信登录用户可能无密码）")
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
    @ApiOperation("修改密码（登录后）")
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
            return Result.error(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("修改失败: " + e.getMessage());
        }
    }

    @PostMapping("/delete")
    @ApiOperation("注销账号")
    public Result<String> deleteAccount(@RequestHeader(value = "Authorization", required = false) String token) {
        try {
            // 从token中获取用户ID
            Long userId = null;
            User user = null;
            
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                System.out.println("注销账号 - 接收到的token: " + (token != null ? token.substring(0, Math.min(20, token.length())) + "..." : "null"));
                
                // 方法1: 尝试从缓存中获取用户信息
                try {
                    user = userService.getUserByToken(token);
                    if (user != null) {
                        userId = user.getId();
                        System.out.println("注销账号 - 从缓存获取用户: ID=" + userId);
                    }
                } catch (Exception e) {
                    System.out.println("从缓存获取用户信息失败: " + e.getMessage());
                }
                
                // 方法2: 如果缓存中没有，从token解析username，然后查询数据库
                if (user == null) {
                    try {
                        String tokenUsername = jwtUtil.getUsernameFromToken(token);
                        System.out.println("注销账号 - 从Token解析出的用户名: " + tokenUsername);
                        
                        // 先尝试从user表查询（通过phone，因为注册时token是用phone生成的）
                        user = userService.getUserByPhone(tokenUsername);
                        if (user == null) {
                            // 如果通过phone找不到，尝试通过username查找（兼容旧用户）
                            user = userService.getUserByUsername(tokenUsername);
                        }
                        
                        if (user != null) {
                            userId = user.getId();
                            System.out.println("注销账号 - 从数据库找到用户: ID=" + userId + ", phone=" + user.getPhone());
                        } else {
                            System.out.println("注销账号 - 未找到用户，tokenUsername: " + tokenUsername);
                        }
                    } catch (Exception e) {
                        System.out.println("从token解析用户信息失败: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
            
            // 如果无法从token获取，返回错误
            if (userId == null) {
                System.out.println("========== 注销账号失败 ==========");
                System.out.println("无法获取用户ID");
                System.out.println("token是否存在: " + (token != null));
                if (token != null) {
                    System.out.println("token前20字符: " + token.substring(0, Math.min(20, token.length())) + "...");
                    try {
                        String tokenUsername = jwtUtil.getUsernameFromToken(token);
                        System.out.println("从token解析出的username: " + tokenUsername);
                        // 尝试查找用户
                        User testUser = userService.getUserByPhone(tokenUsername);
                        if (testUser == null) {
                            testUser = userService.getUserByUsername(tokenUsername);
                        }
                        System.out.println("测试查找用户结果: " + (testUser != null ? "找到，ID=" + testUser.getId() : "未找到"));
                    } catch (Exception e) {
                        System.out.println("解析token失败: " + e.getMessage());
                    }
                }
                System.out.println("====================================");
                return Result.error("无法获取用户信息，请重新登录");
            }
            
            // 删除用户
            boolean deleted = userService.deleteUser(userId);
            if (deleted) {
                // 清除用户缓存
                userService.clearUserCache(userId);
                System.out.println("注销账号成功，用户ID: " + userId);
                return Result.success("账号注销成功");
            } else {
                return Result.error("注销失败，用户不存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("注销失败: " + e.getMessage());
        }
    }

    @PostMapping("/update")
    @ApiOperation("更新用户信息")
    public Result<User> updateUser(@RequestBody User user) {
        try {
            System.out.println("========== 更新用户信息 ==========");
            System.out.println("用户ID: " + user.getId());
            System.out.println("背景图URL: " + user.getBackgroundImage());
            System.out.println("头像URL: " + user.getAvatar());
            System.out.println("昵称: " + user.getNickname());
            System.out.println("个性签名: " + user.getSignature());
            
            // 检查用户ID
            if (user.getId() == null) {
                System.err.println("错误: 用户ID为空");
                return Result.error("用户ID不能为空");
            }
            
            // 检查头像URL是否是临时路径，如果是则不更新头像
            if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                String avatar = user.getAvatar();
                if (avatar.contains("/tmp/") || avatar.contains("/__tmp__/") || 
                    avatar.startsWith("http://tmp/") || avatar.startsWith("http://__tmp__/") ||
                    avatar.startsWith("https://tmp/") || avatar.startsWith("https://__tmp__/")) {
                    System.err.println("拒绝保存临时路径头像: " + avatar);
                    // 不更新头像，保持原有头像
                    User existingUser = userService.getUserById(user.getId());
                    if (existingUser != null) {
                        user.setAvatar(existingUser.getAvatar());
                    } else {
                        user.setAvatar(null);
                    }
                    System.out.println("使用原有头像: " + user.getAvatar());
                }
            }
            
            boolean success = userService.updateUser(user);
            System.out.println("更新结果: " + success);
            
            if (!success) {
                System.err.println("更新失败: updateById返回false");
                return Result.error("更新用户信息失败");
            }
            
            // 重新获取更新后的用户信息
            User updatedUser = userService.getUserById(user.getId());
            System.out.println("更新后的背景图URL: " + (updatedUser != null ? updatedUser.getBackgroundImage() : "null"));
            System.out.println("更新后的头像URL: " + (updatedUser != null ? updatedUser.getAvatar() : "null"));
            System.out.println("更新后的个性签名: " + (updatedUser != null ? updatedUser.getSignature() : "null"));
            System.out.println("====================================");
            
            // 隐藏密码
            if (updatedUser != null) {
                updatedUser.setPassword(null);
            }
            return Result.success(updatedUser);
        } catch (IllegalArgumentException e) {
            // 昵称重复等业务异常
            System.err.println("更新用户信息业务异常: " + e.getMessage());
            e.printStackTrace();
            return Result.error(e.getMessage());
        } catch (Exception e) {
            System.err.println("更新用户信息异常: " + e.getMessage());
            e.printStackTrace();
            return Result.error("更新用户信息失败: " + e.getMessage());
        }
    }

    @GetMapping("/stats")
    @ApiOperation("获取用户统计信息")
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
            log.error("获取用户统计失败: {}", e.getMessage());
            return Result.error("获取用户统计失败: " + e.getMessage());
        }
    }

    @PostMapping("/wxapp/login")
    @ApiOperation("微信小程序登录")
    public Result<Map<String, Object>> wxappLogin(@RequestBody Map<String, String> loginData) {
        try {
            String code = loginData.get("code");
            
            if (code == null || code.trim().isEmpty()) {
                return Result.error(1, "授权码不能为空");
            }
            
            // 调用用户服务进行微信登录
            System.out.println("========== 微信登录请求 ==========");
            System.out.println("接收到的code: " + code.substring(0, Math.min(10, code.length())) + "...");
            
            String token;
            try {
                token = userService.loginByWechat(code);
                // 现在loginByWechat应该总是返回token或抛出异常，不应该返回null
                if (token == null) {
                    System.err.println("微信登录失败：userService.loginByWechat返回null（这不应该发生）");
                    return Result.error(1, "微信登录失败：服务返回null，请查看后端日志");
                }
            } catch (RuntimeException e) {
                // 捕获详细的错误信息并返回给前端
                String errorMsg = e.getMessage();
                System.err.println("========== 微信登录异常 ==========");
                System.err.println("异常信息: " + errorMsg);
                System.err.println("异常类型: " + e.getClass().getName());
                e.printStackTrace();
                System.err.println("====================================");
                return Result.error(1, errorMsg != null ? errorMsg : "微信登录失败，请查看后端日志");
            } catch (Exception e) {
                // 捕获其他异常
                String errorMsg = "微信登录失败: " + e.getMessage();
                System.err.println("========== 微信登录异常 ==========");
                System.err.println("异常信息: " + errorMsg);
                System.err.println("异常类型: " + e.getClass().getName());
                e.printStackTrace();
                System.err.println("====================================");
                return Result.error(1, errorMsg);
            }
            
            System.out.println("微信登录成功，token: " + token.substring(0, Math.min(20, token.length())) + "...");
            
            // 从token中提取用户ID（格式：token_userId_timestamp）
            String[] tokenParts = token.split("_");
            Long userId = null;
            if (tokenParts.length >= 2) {
                try {
                    userId = Long.parseLong(tokenParts[1]);
                } catch (NumberFormatException e) {
                    System.err.println("无法从token中解析用户ID: " + e.getMessage());
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
        } catch (Exception e) {
            System.err.println("微信登录失败: " + e.getMessage());
            e.printStackTrace();
            return Result.error("微信登录失败: " + e.getMessage());
        }
    }

    @PostMapping("/wxapp/authorize")
    @ApiOperation("微信小程序授权")
    public Result<Map<String, Object>> wxappAuthorize(@RequestBody Map<String, String> authData) {
        try {
            // 这里应该调用微信授权服务
            Map<String, Object> result = new HashMap<>();
            result.put("token", "dummy-token");
            result.put("userId", 1);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("微信授权失败: " + e.getMessage());
        }
    }

    @GetMapping("/detail")
    @ApiOperation("获取用户详情")
    public Result<User> getUserDetail(@RequestParam(required = false) String token,
                                      @RequestParam(required = false) Long userId) {
        try {
            User user = null;
            if (userId != null) {
                user = userService.getUserById(userId);
            } else {
                // 根据token获取用户，简化处理
                user = userService.getUserById(1L);
            }
            
            if (user == null) {
                return Result.error("用户不存在");
            }
            
            // 隐藏密码
            user.setPassword(null);
            return Result.success(user);
        } catch (Exception e) {
            return Result.error("获取用户详情失败: " + e.getMessage());
        }
    }

    @GetMapping("/amount")
    @ApiOperation("获取用户资产")
    public Result<Map<String, Object>> getUserAmount(@RequestParam(required = false) String token) {
        try {
            // 这里应该获取用户的资产信息
            Map<String, Object> amount = new HashMap<>();
            amount.put("balance", 0.0);
            amount.put("points", 0);
            return Result.success(amount);
        } catch (Exception e) {
            return Result.error("获取用户资产失败: " + e.getMessage());
        }
    }

    @GetMapping("/list")
    @ApiOperation("获取用户列表（分页）")
    public Result<Map<String, Object>> getUserList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        try {
            // 使用MyBatis-Plus分页查询
            Page<User> userPage = new Page<>(page, size);
            IPage<User> result = userService.getUserList(userPage);

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
        } catch (Exception e) {
            return Result.error("获取用户列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @ApiOperation("根据ID获取用户信息")
    public Result<User> getUserById(@PathVariable Long id) {
        try {
            User user = userService.getUserById(id);
            if (user == null) {
                return Result.error("用户不存在");
            }
            // 隐藏密码
            user.setPassword(null);
            return Result.success(user);
        } catch (Exception e) {
            return Result.error("获取用户信息失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @ApiOperation("更新用户信息")
    public Result<User> updateUserById(@PathVariable Long id, @RequestBody User user) {
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
            // 昵称重复等业务异常
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("更新用户信息失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    @ApiOperation("更新用户状态（1正常，0禁用）")
    public Result<Boolean> updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        try {
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
        } catch (Exception e) {
            return Result.error("更新用户状态失败: " + e.getMessage());
        }
    }

    @GetMapping("/{userId}/following")
    @ApiOperation("获取用户关注列表")
    public Result<Map<String, Object>> getFollowingList(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) Long currentUserId) {
        try {
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
        } catch (Exception e) {
            return Result.error("获取关注列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/{userId}/fans")
    @ApiOperation("获取用户粉丝列表")
    public Result<Map<String, Object>> getFansList(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) Long currentUserId) {
        try {
            System.out.println("========== 获取粉丝列表 ==========");
            System.out.println("用户ID: " + userId);
            System.out.println("页码: " + page + ", 每页大小: " + size);
            
            List<UserFollow> allFans = userFollowService.getFollowerList(userId);
            System.out.println("从数据库查询到的粉丝总数: " + (allFans != null ? allFans.size() : 0));
            if (allFans != null && !allFans.isEmpty()) {
                System.out.println("粉丝列表详情:");
                for (UserFollow follow : allFans) {
                    System.out.println("  - 粉丝ID: " + follow.getFollowerId() + ", 粉丝名: " + follow.getFollowerName());
                }
            }
            
            // 手动分页
            int start = (page - 1) * size;
            int end = Math.min(start + size, allFans != null ? allFans.size() : 0);
            List<UserFollow> pagedFans = (allFans != null && !allFans.isEmpty()) 
                ? allFans.subList(Math.min(start, allFans.size()), end)
                : new ArrayList<>();
            System.out.println("分页后的粉丝数量: " + pagedFans.size());
            
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
                    System.out.println("检查关注状态 - 当前用户ID: " + currentUserId + ", 粉丝ID: " + follow.getFollowerId() + ", 是否已关注: " + isFollowing + ", 是否互相关注: " + isMutualFollow);
                }
                userInfo.put("isFollowing", isFollowing);
                userInfo.put("isMutualFollow", isMutualFollow); // 添加互相关注标识
                
                resultList.add(userInfo);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("list", resultList);
            response.put("hasMore", allFans != null && end < allFans.size());
            
            System.out.println("返回的粉丝列表数量: " + resultList.size());
            System.out.println("是否有更多: " + (allFans != null && end < allFans.size()));
            System.out.println("====================================");
            
            return Result.success(response);
        } catch (Exception e) {
            return Result.error("获取粉丝列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/{userId}/posts")
    @ApiOperation("获取用户动态列表")
    public Result<Map<String, Object>> getUserPosts(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        try {
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
        } catch (Exception e) {
            return Result.error("获取动态列表失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/getPhoneNumber")
    @ApiOperation("获取微信手机号")
    public Result<Map<String, String>> getPhoneNumber(
            @RequestBody Map<String, String> requestData,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            String code = requestData.get("code");
            if (code == null || code.isEmpty()) {
                return Result.error("code不能为空");
            }
            
            System.out.println("========== 获取微信手机号 ==========");
            System.out.println("接收到的code: " + code);
            System.out.println("AppID: " + wechatAppId);
            
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
            
            System.out.println("微信API响应: " + responseBody);
            
            if (responseBody != null) {
                Integer errcode = (Integer) responseBody.get("errcode");
                if (errcode != null && errcode == 0) {
                    Map<String, Object> phoneInfo = (Map<String, Object>) responseBody.get("phone_info");
                    if (phoneInfo != null) {
                        String phoneNumber = (String) phoneInfo.get("phoneNumber");
                        String purePhoneNumber = (String) phoneInfo.get("purePhoneNumber");
                        String countryCode = (String) phoneInfo.get("countryCode");
                        
                        System.out.println("获取到的手机号: " + phoneNumber);
                        System.out.println("纯手机号: " + purePhoneNumber);
                        System.out.println("国家代码: " + countryCode);
                        
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
                                    System.err.println("解析token失败: " + e.getMessage());
                                }
                            }
                            if (currentUser != null) {
                                // 更新用户手机号
                                currentUser.setPhone(purePhoneNumber != null ? purePhoneNumber : phoneNumber);
                                userService.updateUser(currentUser);
                                System.out.println("已更新用户手机号: ID=" + currentUser.getId() + ", phone=" + currentUser.getPhone());
                            } else {
                                System.out.println("无法找到当前用户，手机号将仅返回给前端");
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
                    System.err.println("微信API错误: " + errcode + " - " + errmsg);
                    return Result.error("获取手机号失败: " + (errmsg != null ? errmsg : "未知错误"));
                }
            } else {
                return Result.error("微信API返回为空");
            }
        } catch (Exception e) {
            System.err.println("获取微信手机号异常: " + e.getMessage());
            e.printStackTrace();
            return Result.error("获取手机号失败: " + e.getMessage());
        }
    }

    @PostMapping("/bind-phone")
    @ApiOperation("绑定手机号（使用其它号码时需短信验证码，需登录）")
    public Result<String> bindPhone(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                    @RequestBody Map<String, String> body) {
        try {
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
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("绑定失败: " + e.getMessage());
        }
    }

    @PostMapping("/unbind-phone")
    @ApiOperation("解绑手机号（需登录）。解绑后该手机号可被其他账号绑定；当前账号可再绑定其他手机号。")
    public Result<String> unbindPhone(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
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
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("解绑失败: " + e.getMessage());
        }
    }

    @PostMapping("/verify-password")
    @ApiOperation("验证当前账号登录密码（用于修改账号等敏感操作前）")
    public Result<String> verifyPassword(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                          @RequestBody(required = false) Map<String, String> body) {
        try {
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
            String pwdTrim = password.trim();
            String md5 = MD5Encoder.md5(pwdTrim);
            String dbPwd = currentUser.getPassword();
            if (dbPwd == null || dbPwd.trim().isEmpty()) {
                return Result.error("该账号未设置密码，请先到「修改密码」中设置密码");
            }
            if (!md5.equals(dbPwd) && !pwdTrim.equals(dbPwd)) {
                return Result.error("密码错误");
            }
            return Result.success("验证成功");
        } catch (Exception e) {
            e.printStackTrace();
            String msg = e.getMessage() != null ? e.getMessage() : "验证失败";
            return Result.error("验证失败: " + msg);
        }
    }

    @PostMapping("/change-username")
    @ApiOperation("修改账号（格式：字母或下划线开头，6-20位，仅字母数字下划线减号）")
    public Result<String> changeUsername(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                         @RequestBody Map<String, String> body) {
        try {
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
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("修改失败: " + e.getMessage());
        }
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
                    System.out.println("获取Access Token成功");
                    return accessToken;
                } else {
                    String errmsg = (String) responseBody.get("errmsg");
                    System.err.println("获取Access Token失败: " + errmsg);
                    throw new RuntimeException("获取Access Token失败: " + errmsg);
                }
            } else {
                throw new RuntimeException("获取Access Token返回为空");
            }
        } catch (Exception e) {
            System.err.println("获取Access Token异常: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("获取Access Token异常: " + e.getMessage());
        }
    }
    
    /**
     * 从token中获取用户ID
     */
    private Long getUserIdFromToken(String authHeader) {
        System.out.println("========== UserController.getUserIdFromToken ==========");
        System.out.println("authHeader: " + (authHeader != null ? (authHeader.length() > 50 ? authHeader.substring(0, 50) + "..." : authHeader) : "null"));
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            System.out.println("提取的token: " + (token.length() > 50 ? token.substring(0, 50) + "..." : token));
            
            // 首先尝试从Redis缓存获取用户信息（如果token是登录时存储的）
            try {
                User cachedUser = userService.getUserByToken(token);
                if (cachedUser != null) {
                    System.out.println("从缓存获取用户成功，用户ID: " + cachedUser.getId());
                    System.out.println("====================================");
                    return cachedUser.getId();
                }
            } catch (Exception e) {
                System.err.println("从缓存获取用户失败: " + e.getMessage());
            }
            
            // 如果缓存中没有，尝试解析JWT token
            try {
                Claims claims = jwtUtil.parseToken(token);
                System.out.println("解析的Claims: " + (claims != null ? "不为空" : "为空"));
                if (claims != null) {
                    String subject = claims.getSubject();
                    System.out.println("Token subject: " + subject);
                    if (subject != null) {
                        // 先尝试通过username查找
                        User user = userService.getUserByUsername(subject);
                        System.out.println("通过username查找用户: " + (user != null ? ("找到，ID: " + user.getId()) : "未找到"));
                        if (user == null) {
                            // 如果username找不到，尝试通过phone查找（新注册用户的token subject是phone）
                            user = userService.getUserByPhone(subject);
                            System.out.println("通过phone查找用户: " + (user != null ? ("找到，ID: " + user.getId()) : "未找到"));
                        }
                        if (user != null) {
                            System.out.println("最终返回用户ID: " + user.getId());
                            System.out.println("====================================");
                            return user.getId();
                        } else {
                            System.out.println("警告: 无法通过username或phone找到用户，subject: " + subject);
                        }
                    } else {
                        System.out.println("警告: Token subject为null");
                    }
                } else {
                    System.out.println("警告: Claims解析为null");
                }
            } catch (Exception e) {
                System.err.println("Token解析失败: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("警告: authHeader为null或不以'Bearer '开头");
        }
        System.out.println("返回null（未找到用户ID）");
        System.out.println("====================================");
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
    
    @GetMapping("/points/records")
    @ApiOperation("获取用户积分记录")
    public Result<Map<String, Object>> getPointsRecords(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false, defaultValue = "earn") String type) {
        try {
            // 从token获取用户ID
            Long currentUserId = getUserIdFromToken(token);
            if (currentUserId == null && userId == null) {
                return Result.error(401, "未登录");
            }
            
            Long targetUserId = userId != null ? userId : currentUserId;
            
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
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取积分记录失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/points/statistics")
    @ApiOperation("获取用户积分统计")
    public Result<Map<String, Object>> getPointsStatistics(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam(required = false) Long userId) {
        try {
            // 从token获取用户ID
            Long currentUserId = getUserIdFromToken(token);
            if (currentUserId == null && userId == null) {
                return Result.error(401, "未登录");
            }
            
            Long targetUserId = userId != null ? userId : currentUserId;
            
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
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取积分统计失败: " + e.getMessage());
        }
    }
    
    /**
     * 手动更新用户的魅力值和等级
     */
    @PostMapping("/update/charm-level")
    @ApiOperation("更新用户的魅力值和等级")
    @CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
    public Result<Map<String, Object>> updateCharmAndLevel(
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            System.out.println("========== 更新用户的魅力值和等级 ==========");
            System.out.println("Authorization Header: " + (token != null ? (token.length() > 50 ? token.substring(0, 50) + "..." : token) : "null"));
            
            Long userId = getUserIdFromToken(token);
            System.out.println("从Token解析出的用户ID: " + userId);
            
            if (userId == null) {
                System.err.println("用户未登录或token无效");
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
            
            System.out.println("更新成功，用户ID: " + userId + ", 魅力值: " + result.get("charm") + ", 等级: " + result.get("level"));
            System.out.println("====================================");
            
            return Result.success("更新成功", result);
        } catch (Exception e) {
            System.err.println("========== 更新用户的魅力值和等级异常 ==========");
            e.printStackTrace();
            System.err.println("====================================");
            return Result.error("更新失败: " + e.getMessage());
        }
    }
}

