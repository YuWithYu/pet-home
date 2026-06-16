package com.pethome.interceptor;

import com.pethome.service.AdminService;
import com.pethome.service.UserService;
import com.pethome.entity.Admin;
import com.pethome.entity.User;
import com.pethome.util.JwtUtil;
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
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        String method = request.getMethod();
        
        // 允许OPTIONS预检请求（CORS）
        if ("OPTIONS".equals(method)) {
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH");
            response.setHeader("Access-Control-Allow-Headers", "*");
            response.setHeader("Access-Control-Max-Age", "3600");
            response.setStatus(HttpServletResponse.SC_OK);
            return true;
        }
        
        // 允许的公开路径（不需要认证）
        if (uri.startsWith("/api/admin/login") || 
            uri.startsWith("/api/user/login") ||
            uri.startsWith("/api/user/register") ||
            uri.startsWith("/api/user/forgot-password") ||  // 忘记密码接口
            uri.startsWith("/api/user/wxapp/login") ||  // 微信小程序登录接口
            uri.startsWith("/api/sms/") ||  // 短信验证码接口不需要认证
            uri.startsWith("/api/upload/") ||  // 文件上传接口不需要认证
            uri.startsWith("/public/") || 
            uri.startsWith("/static/") ||
            uri.startsWith("/upload/") ||
            uri.startsWith("/error") ||
            uri.equals("/") ||
            uri.startsWith("/favicon.ico")) {
            return true;
        }
        
        // 小程序端公开接口（不需要token）- 只读接口
        // 移除查询参数，只比较路径部分
        String path = uri.contains("?") ? uri.substring(0, uri.indexOf("?")) : uri;
        
        if (path.equals("/api/banner/list") ||
            path.equals("/api/product/hot") ||
            path.startsWith("/api/product/list") ||
            ("GET".equalsIgnoreCase(method) && path.matches("^/api/product/\\d+$")) ||
            path.equals("/api/product/recommend") ||
            path.equals("/api/notice/last-one") ||
            path.startsWith("/api/notice/") ||  // 允许所有公告查询接口
            path.startsWith("/api/categories/all") ||  // 分类列表
            path.startsWith("/api/categories/page") ||  // 分类分页
            path.startsWith("/api/user/current") ||  // 获取当前用户信息（小程序端）
            path.startsWith("/api/user/stats") ||  // 用户统计信息（小程序端）
            path.startsWith("/api/user/detail") ||  // 用户详情（小程序端）
            path.startsWith("/api/pets/page") ||  // 宠物列表（小程序端）
            // 仅允许 GET /api/pets/{数字id} 查询详情；创建/更新/删除需登录，不可放行整段 /api/pets/
            ("GET".equalsIgnoreCase(method) && path.matches("^/api/pets/\\d+$")) ||
            path.startsWith("/api/grooming-appointments/user/list/") ||  // 用户洗护预约列表（小程序端）
            path.startsWith("/api/grooming-appointments/create") ||  // 小程序创建洗护预约
            // 洗护：仅放行创建、用户列表、GET 数字 id 详情；管理端 /page 等需 Token 解析 adminId
            ("GET".equalsIgnoreCase(method) && path.matches("^/api/grooming-appointments/\\d+$")) ||
            path.startsWith("/api/door-cleaning/user/list/") ||  // 用户上门铲屎预约列表（小程序端）
            path.startsWith("/api/door-cleaning/create") ||  // 小程序创建上门铲屎预约
            path.startsWith("/api/hospital-appointments/create") ||  // 小程序创建医院预约
            // 上门铲屎：仅放行创建、用户列表、以及「数字 id」详情 GET；管理端 /page、/member/list 等需走 Token 以解析 adminId
            ("GET".equalsIgnoreCase(method) && path.matches("^/api/door-cleaning/\\d+$")) ||
            path.startsWith("/api/hospital-appointments/user/list/") ||  // 用户医院预约列表（小程序端）
            path.startsWith("/api/user-appointments/list/") ||  // 用户预约统一列表（我的预约页，免 token）
            path.startsWith("/api/community/posts") ||
            path.startsWith("/api/community/users/") ||  // 社区用户相关接口（关注、推荐等）
            path.startsWith("/api/daily-topics") ||  // 每日专题（小程序端）
            path.startsWith("/api/tag/hot") ||
            path.startsWith("/api/grooming-services/page") ||
            path.startsWith("/api/hospital-services/page") ||
            path.startsWith("/api/litter-services/page") ||
            path.startsWith("/api/search/") ||
            path.startsWith("/api/regions/") ||
            path.startsWith("/api/stores/page") ||
            path.startsWith("/api/stores/all") ||
            path.startsWith("/api/grooming-banner") ||
            path.startsWith("/api/hospital-banners/position/") ||
            path.startsWith("/api/stores/by-service/") ||
            path.startsWith("/api/medical-banner") ||
            path.startsWith("/api/litter-banner") ||
            path.startsWith("/api/time-slots/") ||  // 时间段管理接口（小程序和管理后台都需要）
            path.startsWith("/api/schedule/available") ||  // 小程序查询可预约时间段
            path.startsWith("/api/schedule/day") ||  // 日程详情
            path.startsWith("/api/schedule/calendar") ||  // 日程月历
            path.startsWith("/api/schedule-config/generate-batch") ||  // 批量生成排班
            path.startsWith("/api/admin/staff/list") ||  // 服务人员列表
            path.startsWith("/api/department/list") ||  // 部门列表
            path.startsWith("/api/service-member/list") ||  // 小程序查询服务人员列表
            path.startsWith("/api/orders/" ) ||  // 管理端查询订单
            path.startsWith("/api/signin/") ||  // 签到相关接口（小程序端）
            path.startsWith("/api/tasks/") ||  // 任务相关接口（小程序端）
            // 积分商城：商品列表公开；兑换记录与订单列表一致，允许带 userId 查询（小程序端与 /api/orders/list 策略对齐）
            path.equals("/api/points/products") ||
            path.equals("/api/points/exchange/history") ||
            // 服务详情查询接口（格式：/api/xxx-services/{id}，不包含create/update/delete/upload等写操作）
            (path.startsWith("/api/grooming-services/") && !path.contains("/create") && !path.contains("/update") && !path.contains("/delete") && !path.contains("/upload") && !path.contains("/image")) ||
            (path.startsWith("/api/hospital-services/") && !path.contains("/create") && !path.contains("/update") && !path.contains("/delete") && !path.contains("/upload") && !path.contains("/image")) ||
            (path.startsWith("/api/litter-services/") && !path.contains("/create") && !path.contains("/update") && !path.contains("/delete") && !path.contains("/upload") && !path.contains("/image"))) {
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
                        request.setAttribute("userId", user.getId());
                        request.setAttribute("role", "user");
                    } else {
                        log.debug("User 表未找到或未激活 username={}", username);
                    }
                }
            }
        }
        
        log.debug("鉴权通过 uri={} role={} userId={} adminId={}", uri, role, request.getAttribute("userId"), request.getAttribute("adminId"));
        
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
        
        // SUPER_ADMIN 默认放行所有路径
        return true;
    }
}

