package com.pethome.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pethome.entity.AdminPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 管理员权限Mapper接口
 */
@Mapper
public interface AdminPermissionMapper extends BaseMapper<AdminPermission> {
    
    /**
     * 根据管理员ID查询所有权限
     */
    List<AdminPermission> selectByAdminId(@Param("adminId") Long adminId);
    
    /**
     * 根据管理员ID和权限代码查询权限
     */
    AdminPermission selectByAdminIdAndCode(@Param("adminId") Long adminId, @Param("permissionCode") String permissionCode);
    
    /**
     * 删除管理员的所有权限
     */
    int deleteByAdminId(@Param("adminId") Long adminId);
    
    /**
     * 批量插入权限
     */
    int batchInsert(@Param("permissions") List<AdminPermission> permissions);
}
