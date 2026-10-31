package com.pethome.interceptor;

import com.pethome.service.admin.AdminService;
import com.pethome.service.user.UserService;
import com.pethome.entity.Admin;
import com.pethome.entity.User;
import com.pethome.security.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 角色权限拦截器
 * 用于限制 STAFF 角色只能访问特定路径
 */
@Component
public class RoleInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RoleInterceptor.class);
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private AdminService adminService;
    
    @Autowired
    private UserService userService;

    private String normalizeAdminRole(String role) {
        if (role == null) return "staff";
        String r = role.toLowerCase().trim();
        if ("super_admin".equals(r)) return "admin";
        return r;
    }

    /**
     * 公开只读接口清单（仅 GET 放行，调用方已按 method 预筛）。
     *
     * 约定：这里只允许"只读数据"接口。需要登录的接口（/api/pets/page、/api/orders/list、
     * /api/time-slots 与 /api/notice 的写操作、/api/schedule-config/** 等）不在清单内，
     * 由下方 token 校验链路统一拦截，避免新增写接口被旧前缀顺带放行。
     */
    private boolean isPublicReadApi(String path) {
        return path.equals("/api/banner/list")
                || path.equals("/api/banner/active")            // 首页轮播图（未登录首页使用）
                || path.equals("/api/service-config/all")       // 服务配置（首页服务入口）
                || path.equals("/api/product/hot")
                || path.startsWith("/api/product/list")
                || path.matches("^/api/product/\\d+$")
                || path.equals("/api/product/recommend")
                || path.startsWith("/api/notice/")              // 公告仅公开只读
                || path.startsWith("/api/categories/all")
                || path.startsWith("/api/categories/page")
                || path.startsWith("/api/user/current")         // Controller 内部按 token 脱敏
                || path.startsWith("/api/user/stats")           // 仅返回关注/粉丝等计数
                || path.startsWith("/api/user/detail")          // Controller 内部要求登录并脱敏
                // 仅允许 GET /api/pets/{数字id} 查询详情；创建/更新/删除需登录
                || path.matches("^/api/pets/\\d+$")
                // 洗护：仅放行 GET 数字 id 详情；管理端 /page 等需 Token 解析 adminId
                || path.matches("^/api/grooming-appointments/\\d+$")
                // 上门铲屎：仅放行「数字 id」详情 GET
                || path.matches("^/api/door-cleaning/\\d+$")
                || path.startsWith("/api/community/posts")      // 社区帖子只读；发帖/点赞需登录
                || path.startsWith("/api/community/users/")
                || path.startsWith("/api/daily-topics")         // 每日专题只读；点赞/收藏需登录
                || path.startsWith("/api/tag/hot")
                || path.startsWith("/api/search/")
                || path.startsWith("/api/regions/")
                || path.startsWith("/api/stores/page")
                || path.startsWith("/api/stores/all")
                || path.startsWith("/api/stores/by-service/")
                // 安全修复：/api/grooming-banner 前缀会同时匹配 /api/grooming-banners/create|update，
                // 因此横幅类前缀只放行 GET；写操作需登录（/api/grooming-banners 等）
                || path.startsWith("/api/grooming-banner")
                || path.startsWith("/api/hospital-banners/position/")
                || path.startsWith("/api/medical-banner")
                || path.startsWith("/api/litter-banner")
                || path.startsWith("/api/time-slots/")          // 时间段只读；增删改需登录
                || path.startsWith("/api/schedule/available")
                || path.startsWith("/api/schedule/day")
                || path.startsWith("/api/schedule/calendar")
                || path.startsWith("/api/department/list")
                || path.startsWith("/api/service-member/list")
                || path.startsWith("/api/signin/")             // 未登录返回默认数据
                || path.startsWith("/api/tasks/")              // 进度查询，未登录返回空
                || path.equals("/api/points/products")
                // 洗护/医院/铲屎服务：仅放行只读查询（page/list/{数字id}），新增写接口默认需要登录
                || path.matches("^/api/(grooming|hospital|litter)-services/(page|list|\\d+)$");
    }
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        String method = request.getMethod();
        
        // 允许OPTIONS预检请求（CORS）
        // 安全修复：原实现在此硬编码 Access-Control-Allow-Origin: *，绕过 SecurityConfig 中的来源白名单。
        // 预检响应统一由 Spring Security 的 CorsFilter（CorsConfigurationSource）生成，此处仅放行。
        if ("OPTIONS".equals(method)) {
            return true;
        }
        
        // 允许的公开路径（不需要认证）
        if (uri.startsWith("/api/admin/login") ||
            uri.startsWith("/api/user/login") ||
            uri.startsWith("/api/user/register") ||
            uri.startsWith("/api/user/forgot-password") ||  // 忘记密码接口
            uri.startsWith("/api/user/wxapp/login") ||  // 微信小程序登录接口
            uri.startsWith("/api/sms/") ||  // 短信验证码接口不需要认证
            uri.startsWith("/api/payment/notify") ||  // 微信支付回调（自带签名校验，不走 JWT）
            // 安全修复：移除 /api/upload/ 的免登录放行——上传文件必须登录，避免匿名用户投递恶意文件
            uri.startsWith("/public/") ||
            uri.startsWith("/static/") ||
            uri.startsWith("/upload/") ||  // 仅静态资源访问前缀，与 /api/upload 接口区分
            uri.startsWith("/error") ||
            uri.equals("/") ||
            uri.startsWith("/favicon.ico")) {
            return true;
        }
        
        // 小程序端公开接口（不需要token）——安全基线：公开白名单一律「只读 GET」。
        // 安全修复：原白名单对 /api/notice/、/api/time-slots/、/api/schedule-config/generate-batch、
        // /api/grooming-banner(s)、/api/litter-banner(s) 等前缀不限 HTTP 方法放行，导致匿名用户可以：
        //   - 新增/修改/删除公告、时间段、服务横幅（数据篡改）
        //   - 批量生成任意门店/人员/日期的排班（数据污染与资源消耗）
        // 现在所有公开前缀统一要求 GET；任何新增写接口默认需要登录，避免"新增接口被旧前缀顺带放行"。
        // 移除查询参数，只比较路径部分
        String path = uri.contains("?") ? uri.substring(0, uri.indexOf("?")) : uri;

        if ("GET".equalsIgnoreCase(method) && isPublicReadApi(path)) {
            return true;
        }
        
        // 获取Authorization header
        String authHeader = request.getHeader("Authorization");
        log.debug("RoleInterceptor uri={}, method={}, bearerPresent={}", uri, method, authHeader != null && authHeader.startsWith("Bearer "));
        
        if (authHeader == null || authHeader.isBlank() || !authHeader.startsWith("Bearer ")) {
            log.debug("Authorization 缺失或格式错误: {}", uri);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"未登录或Token无效\"}");
            return false;
        }
        
        String token = authHeader.substring(7);
        
        // 先尝试从Redis获取用户token（用于微信登录和手机号登录）
        User userFromToken = userService.getUserByToken(token);
        Claims claims = null;
        String username = null;
        String role = null;
        Long departmentId = null;
        
        if (userFromToken != null && userFromToken.getStatus() != null && userFromToken.getStatus() == 1) {
            // Redis token验证成功，是普通用户
            log.debug("Redis token 验证成功, userId={}", userFromToken.getId());
            username = userFromToken.getUsername() != null ? userFromToken.getUsername() : 
                      (userFromToken.getPhone() != null ? userFromToken.getPhone() : "");
            role = "user";
            request.setAttribute("userId", userFromToken.getId());
            request.setAttribute("role", role);
            // 对于 /api/user/current 接口，直接允许通过
            if (uri.startsWith("/api/user/current")) {
                return true;
            }
        } else {
            // Redis token验证失败，尝试JWT token验证（用于admin）
            try {
                claims = jwtUtil.parseToken(token);
                if (claims == null) {
                    log.warn("JWT claims 为 null");
                    throw new Exception("Token解析失败");
                }
                log.debug("JWT 解析成功, subject={}", claims.getSubject());
                // 从JWT Token中获取角色和部门ID
                username = claims.getSubject();
                role = jwtUtil.getRoleFromToken(token);
                departmentId = jwtUtil.getDepartmentIdFromToken(token);
            } catch (Exception e) {
                log.warn("JWT 解析异常: {}", e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":401,\"msg\":\"Token无效或已过期\"}");
                return false;
            }
        }
        
        // 对于 /api/user/current 接口，只要token有效就允许通过，不需要检查admin是否存在
        // 因为这个接口会同时处理user表和admin表的查询
        if (uri.startsWith("/api/user/current")) {
            // 如果有username，尝试从admin表查询并设置属性
            if (username != null) {
                Admin admin = adminService.getByUsername(username);
                if (admin != null) {
                    request.setAttribute("adminId", admin.getId());
                    // 管理员身份以数据库为准，避免 JWT 无 role / 旧 token 仍为 staff 导致超管被按店员门店过滤
                    String dbRole = normalizeAdminRole(admin.getRole());
                    request.setAttribute("role", dbRole);
                    request.setAttribute("departmentId", departmentId != null ? departmentId : null);
                    request.setAttribute("department", admin.getDepartment());
                    request.setAttribute("serviceStoreId", admin.getServiceStoreId());
                }
            }
            // 允许通过，让 /api/user/current 接口自己处理user和admin的查询
            return true;
        }
        
        // 如果token中没有role，尝试从数据库查询
        if (role == null && username != null) {
            log.debug("Token 无 role，查库 username={}", username);
            Admin admin = adminService.getByUsername(username);
            if (admin != null) {
                log.debug("找到 Admin id={} role={}", admin.getId(), admin.getRole());
                role = normalizeAdminRole(admin.getRole());
                // 将role和departmentId写入request属性，供后续使用
                request.setAttribute("adminId", admin.getId());
                request.setAttribute("role", role);
                request.setAttribute("departmentId", departmentId);
                request.setAttribute("department", admin.getDepartment());
            } else {
                // 如果admin表中找不到，尝试从user表查找（普通用户）
                // 先尝试通过username查找（兼容旧用户）
                User user = userService.getUserByUsername(username);
                // 如果通过username找不到，尝试通过phone查找（新用户token的subject是phone）
                if (user == null) {
                    user = userService.getUserByPhone(username);
                }
                if (user != null && user.getStatus() != null && user.getStatus() == 1) {
                    log.debug("找到 User id={} status={}", user.getId(), user.getStatus());
                    // 普通用户，设置role为"user"，允许访问
                    role = "user";
                    request.setAttribute("userId", user.getId());
                    request.setAttribute("role", role);
                    // 普通用户允许访问，继续执行
                } else {
                    log.warn("未找到用户或未激活 username={}", username);
                    // 对于非 /api/user/current 的接口，如果找不到admin或user，返回401
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":401,\"msg\":\"用户不存在或未激活\"}");
                    return false;
                }
            }
        } else {
            // 从数据库获取admin信息（用于设置departmentId等）
            if (username != null) {
                Admin admin = adminService.getByUsername(username);
                if (admin != null) {
                    request.setAttribute("adminId", admin.getId());
                    String dbRole = normalizeAdminRole(admin.getRole());
                    request.setAttribute("role", dbRole);
                    role = dbRole;
                    request.setAttribute("departmentId", departmentId);
                    request.setAttribute("department", admin.getDepartment());
                    request.setAttribute("serviceStoreId", admin.getServiceStoreId());
                } else {
                    // 如果admin表中找不到，尝试从user表查找（普通用户）
                    // 先尝试通过username查找（兼容旧用户）
                    User user = userService.getUserByUsername(username);
                    // 如果通过username找不到，尝试通过phone查找（新用户token的subject是phone）
                    if (user == null) {
                        user = userService.getUserByPhone(username);
                    }
                    if (user != null && user.getStatus() != null && user.getStatus() == 1) {
                        log.debug("找到 User id={}", user.getId());
                        // 普通用户，设置role为"user"
                        // 安全修复：同步更新局部变量 role，避免沿用 JWT claim 中的旧角色（如 admin 被删除后其旧 token 仍以管理员身份放行）
                        role = "user";
                        request.setAttribute("userId", user.getId());
                        request.setAttribute("role", "user");
                    } else {
                        // 安全修复：token 携带 role 但 admin/user 表均查无此人（已删除或未激活），
                        // 原实现仅记录日志仍放行，导致已删除账号的旧 token 可继续访问，此处改为直接拒绝
                        log.warn("JWT 携带 role 但用户不存在或未激活，拒绝访问 username={}", username);
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write("{\"code\":401,\"msg\":\"用户不存在或未激活\"}");
                        return false;
                    }
                }
            }
        }

        // 安全修复：无法确定身份时一律拒绝（此前 role 为 null 会无条件放行所有接口）
        if (role == null) {
            log.warn("无法确定请求身份，拒绝访问: uri={}", uri);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"Token无效或已过期\"}");
            return false;
        }

        log.debug("鉴权通过 uri={} role={} userId={} adminId={}", uri, role, request.getAttribute("userId"), request.getAttribute("adminId"));

        // 安全修复：/api/user/list 返回全量用户资料（含手机号），仅管理端角色可访问。
        // 此前任意登录用户（含 C 端）均可调用，造成用户手机号批量泄露。
        if (uri.equals("/api/user/list")) {
            if (!"admin".equals(role) && !"store_admin".equals(role)) {
                log.warn("非管理端角色访问用户列表被拒绝: uri={}, role={}", uri, role);
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":403,\"msg\":\"无权访问用户列表\"}");
                return false;
            }
        }

        // STAFF 角色只能访问特定路径
        if ("staff".equals(role)) {
            // 允许的路径清单
            boolean allowed = uri.startsWith("/api/staff/") || 
                             uri.startsWith("/api/user/current") ||  // 获取当前用户信息，允许所有已登录用户访问
                             uri.startsWith("/api/appointment/listByStaff") ||
                             uri.startsWith("/api/appointment/my") ||
                             uri.startsWith("/api/admin/self/") ||
                             uri.startsWith("/api/litter-services/page") ||
                             uri.startsWith("/api/service-member/by-user") ||
                             uri.startsWith("/api/service-member/self") ||
                             uri.startsWith("/api/service-member/list") ||
                             uri.startsWith("/api/grooming-appointments/") ||
                             uri.startsWith("/api/appointment/") && (uri.contains("listByStaff") || uri.contains("my"));
            
            // 如果是服务管理相关接口，检查部门权限
            if (uri.startsWith("/api/litter-services/") && !uri.equals("/api/litter-services/page")) {
                // 铲屎服务管理页面，允许staff访问
                allowed = true;
            }
            
            // 允许staff访问自己的服务管理页面（根据部门）
            if (uri.startsWith("/api/grooming-services/page") ||
                uri.startsWith("/api/hospital-services/page") ||  // 医院服务列表
                uri.startsWith("/api/hospital-appointments/page") ||  // 医院预约列表
                uri.startsWith("/api/hospital-appointments/") ||  // 医院预约相关接口（查询、更新等）
                uri.startsWith("/api/appointment-change-request/") ||  // 预约变更审核
                uri.startsWith("/api/appointment-cancellation-request/") ||  // 预约取消审核
                uri.startsWith("/api/schedule/") ||  // 排班管理接口
                uri.startsWith("/api/schedule-config/") ||  // 排班配置接口
                uri.startsWith("/api/time-slots/") ||  // 时间段管理接口
                uri.startsWith("/api/verify/") ||  // 核销接口
                uri.startsWith("/api/door-cleaning/member/list") ||  // 上门铲屎：服务人员「我的订单」
                (uri.startsWith("/api/door-cleaning/") && uri.matches("^/api/door-cleaning/\\d+$")) ||  // 上门铲屎预约详情 GET
                (uri.startsWith("/api/door-cleaning/") && uri.matches("^/api/door-cleaning/\\d+/status$"))) {  // 上门铲屎预约状态更新
                // 这些页面会根据部门过滤，允许访问
                allowed = true;
            }
            
            if (!allowed) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":403,\"msg\":\"您没有权限访问该资源\"}");
                return false;
            }
        }

        // 安全修复（阻塞项）：管理端接口（/api/admin/**）默认仅允许 admin 角色访问。
        // 此前除 staff 外的所有角色（含普通用户）都会无条件放行，导致任意登录用户可调用
        // 商品管理、权限管理、后台上传（UploadController 也挂在 /api/admin 下）等全部管理接口。
        // 例外：/api/admin/self/** 允许 staff 查询自身信息（已含在上方 staff 白名单中）；
        //       /api/admin/login 已在上方匿名白名单放行。
        if (uri.startsWith("/api/admin/") && !uri.startsWith("/api/admin/self/")) {
            if (!"admin".equals(role)) {
                log.warn("非管理员访问管理端接口被拒绝: uri={}, role={}", uri, role);
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":403,\"msg\":\"无权访问管理端接口\"}");
                return false;
            }
            return true;
        }
        // staff 访问 /api/admin/self/** 时已在上方 staff 白名单放行；此处普通用户请求 /api/admin/self/ 仍需拒绝
        if (uri.startsWith("/api/admin/self/") && !"admin".equals(role) && !"staff".equals(role)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":403,\"msg\":\"无权访问管理端接口\"}");
            return false;
        }

        // 认证通过的普通接口放行（具体资源归属由各 Controller 校验）
        return true;
    }
}

