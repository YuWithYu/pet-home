package com.pethome.service;

import com.pethome.entity.AdminPermission;

import java.util.List;

/**
 * 管理员权限服务接口
 */
public interface AdminPermissionService {
    
    /**
     * 根据管理员ID查询所有权限
     */
    List<AdminPermission> getPermissionsByAdminId(Long adminId);
    
    /**
     * 检查管理员是否有某个权限
     */
    boolean hasPermission(Long adminId, String permissionCode);
    
    /**
     * 为管理员设置权限（批量）
     */
    void setPermissions(Long adminId, List<AdminPermission> permissions);
    
    /**
     * 删除管理员的所有权限
     */
    void deletePermissionsByAdminId(Long adminId);
    
    /**
     * 获取所有可用的权限列表（用于权限选择）
     */
    List<AdminPermission> getAllAvailablePermissions();
}
