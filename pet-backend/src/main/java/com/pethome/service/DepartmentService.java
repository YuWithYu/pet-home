package com.pethome.service;

import com.pethome.entity.Department;

import java.util.List;

/**
 * 部门Service接口
 */
public interface DepartmentService {
    
    /**
     * 查询所有启用的部门
     */
    List<Department> getAllDepartments();

    /**
     * 按门店查询部门（store_id = storeId）
     */
    List<Department> getDepartmentsByStoreId(Long storeId);
    
    /**
     * 根据ID查询部门
     */
    Department getDepartmentById(Long id);
    
    /**
     * 创建部门
     */
    Department createDepartment(Department department);
    
    /**
     * 更新部门
     */
    boolean updateDepartment(Department department);
    
    /**
     * 删除部门（自动处理成员）
     * @param id 部门ID
     * @param forceDelete 保留参数以兼容旧代码，已不再使用
     * @param storeId 若不为 null，则仅当部门属于该门店（department.storeId == storeId）时才允许删除，避免删到平台部门或其他门店部门
     * @return 是否删除成功
     */
    boolean deleteDepartment(Long id, boolean forceDelete, Long storeId);
    
    /**
     * 检查部门是否有成员
     */
    int countMembersByDepartment(Long departmentId);
}

