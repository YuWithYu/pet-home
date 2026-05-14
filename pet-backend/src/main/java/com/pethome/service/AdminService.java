package com.pethome.service;

import com.pethome.entity.Admin;

import java.util.List;

/**
 * 管理员服务接口
 */
public interface AdminService {
    
    /**
     * 根据用户名查询管理员
     */
    Admin getByUsername(String username);
    
    /**
     * 根据ID查询管理员
     */
    Admin getById(Long id);
    
    /**
     * 创建管理员
     */
    Admin createAdmin(Admin admin);
    
    /**
     * 更新管理员信息
     */
    boolean updateAdmin(Admin admin);
    
    /**
     * 删除管理员
     */
    boolean deleteAdmin(Long id);
    
    /**
     * 根据部门查询员工列表
     */
    List<Admin> listStaffByDepartment(String department);
    
    /**
     * 查询所有员工（不包括超级管理员）
     */
    List<Admin> listAllStaff();

    /**
     * 查询所有账号（不包括超级管理员，包含禁用）
     */
    List<Admin> listAllStaffWithDisabled();

    /**
     * 按所属服务门店查询员工（service_store_id = storeId）
     */
    List<Admin> listStaffByStoreId(Long storeId);

    /**
     * 按所属服务门店查询账号（包含禁用）
     */
    List<Admin> listStaffByStoreIdWithDisabled(Long storeId);
    
    /**
     * 验证密码
     */
    boolean verifyPassword(String rawPassword, String encodedPassword);
    
    /**
     * 加密密码
     */
    String encodePassword(String rawPassword);
}

