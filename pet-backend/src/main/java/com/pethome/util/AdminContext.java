package com.pethome.util;

import com.pethome.entity.Admin;
import com.pethome.service.AdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 管理员上下文工具类
 * 用于获取当前登录的管理员信息，实现数据隔离
 */
@Component
public class AdminContext {

    private static final Logger log = LoggerFactory.getLogger(AdminContext.class);

    @Autowired(required = false)
    private AdminService adminService;

    @Autowired(required = false)
    private JwtUtil jwtUtil;

    /**
     * 从token中获取当前登录的管理员
     * @param token Authorization header中的token（包含"Bearer "前缀）
     * @return 管理员对象，如果未登录或token无效则返回null
     */
    public Admin getCurrentAdmin(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return null;
        }

        try {
            String tokenStr = token.substring(7);
            if (jwtUtil == null || adminService == null) {
                return null;
            }
            String username = jwtUtil.getUsernameFromToken(tokenStr);
            if (username == null) {
                return null;
            }
            return adminService.getByUsername(username);
        } catch (Exception e) {
            log.warn("解析管理员 token 失败: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 获取当前登录管理员的店铺ID（商品店铺，stores表）
     * @param token Authorization header中的token
     * @return 店铺ID，如果是平台管理员则返回null
     */
    public Long getCurrentStoreId(String token) {
        Admin admin = getCurrentAdmin(token);
        return admin != null ? admin.getStoreId() : null;
    }

    /**
     * 获取当前登录管理员的服务门店ID（服务门店，service_stores表）
     * @param token Authorization header中的token
     * @return 服务门店ID，平台级则返回null
     */
    public Long getCurrentServiceStoreId(String token) {
        Admin admin = getCurrentAdmin(token);
        return admin != null ? admin.getServiceStoreId() : null;
    }
    
    /**
     * 判断当前登录用户是否为平台管理员（可以管理所有店铺）
     * @param token Authorization header中的token
     * @return true表示是平台管理员，false表示是店铺管理员
     */
    public boolean isPlatformAdmin(String token) {
        Admin admin = getCurrentAdmin(token);
        if (admin == null) {
            return false;
        }
        // 超级管理员或storeId为null的管理员是平台管理员
        return admin.isSuperAdmin() || admin.getStoreId() == null;
    }
    
    /**
     * 判断当前登录用户是否为店铺管理员（只能管理自己店铺）
     * @param token Authorization header中的token
     * @return true表示是店铺管理员，false表示是平台管理员
     */
    public boolean isStoreAdmin(String token) {
        Admin admin = getCurrentAdmin(token);
        return admin != null && admin.getStoreId() != null && !admin.isSuperAdmin();
    }
}
