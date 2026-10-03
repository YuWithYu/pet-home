package com.pethome.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pethome.entity.Admin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 管理员Mapper接口
 */
@Mapper
public interface AdminMapper extends BaseMapper<Admin> {
    
    /**
     * 根据用户名查询管理员
     */
    Admin selectByUsername(@Param("username") String username);
    
    /**
     * 根据部门查询员工列表
     */
    List<Admin> selectStaffByDepartment(@Param("department") String department);
    
    /**
     * 查询所有员工（不包括超级管理员）
     */
    List<Admin> selectAllStaff();

    /**
     * 查询所有账号（不包括超级管理员，包含禁用）
     */
    List<Admin> selectAllStaffWithDisabled();
    
    /**
     * 根据角色查询管理员列表
     */
    List<Admin> selectByRole(@Param("role") String role);
}

