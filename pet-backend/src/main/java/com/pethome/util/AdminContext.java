package com.pethome.util;

import com.pethome.entity.Admin;
import com.pethome.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 管理员上下文工具类
 * 用于获取当前登录的管理员信息，实现数据隔离
 */
@Component
public class AdminContext {
    
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
            System.out.println("AdminContext - Token为空或不以Bearer开头: " + (token != null ? token.substring(0, Math.min(20, token.length())) : "null"));
            return null;
        }
        
        try {
            String tokenStr = token.substring(7);
            if (jwtUtil == null) {
                System.out.println("AdminContext - JwtUtil未注入");
                return null;
            }
            String username = jwtUtil.getUsernameFromToken(tokenStr);
            System.out.println("AdminContext - 从Token解析用户名: " + username);
            if (username == null || adminService == null) {
                System.out.println("AdminContext - 用户名或AdminService为空");
                return null;
            }
            
            Admin admin = adminService.getByUsername(username);
            System.out.println("AdminContext - 获取管理员: " + (admin != null ? "username=" + admin.getUsername() + ", storeId=" + admin.getStoreId() : "null"));
            return admin;
        } catch (Exception e) {
            System.out.println("AdminContext - 获取管理员异常: " + e.getMessage());
            e.printStackTrace();
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
