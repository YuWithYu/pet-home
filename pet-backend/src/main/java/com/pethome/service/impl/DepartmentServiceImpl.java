package com.pethome.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.pethome.entity.Admin;
import com.pethome.entity.Department;
import com.pethome.mapper.AdminMapper;
import com.pethome.mapper.DepartmentMapper;
import com.pethome.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 部门Service实现类
 */
@Service
public class DepartmentServiceImpl implements DepartmentService {
    
    @Autowired
    private DepartmentMapper departmentMapper;
    
    @Autowired
    private AdminMapper adminMapper;
    
    @Override
    public List<Department> getAllDepartments() {
        QueryWrapper<Department> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1);
        wrapper.orderByAsc("create_time");
        return departmentMapper.selectList(wrapper);
    }

    @Override
    public List<Department> getDepartmentsByStoreId(Long storeId) {
        if (storeId == null) {
            return getAllDepartments();
        }
        // 返回平台级部门(store_id IS NULL) + 该门店专属部门(store_id = storeId)
        QueryWrapper<Department> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1);
        wrapper.and(w -> w.isNull("store_id").or().eq("store_id", storeId));
        wrapper.orderByAsc("create_time");
        return departmentMapper.selectList(wrapper);
    }
    
    @Override
    public Department getDepartmentById(Long id) {
        return departmentMapper.selectById(id);
    }
    
    @Override
    @Transactional
    public Department createDepartment(Department department) {
        // 设置默认值
        if (department.getStatus() == null) {
            department.setStatus(1); // 默认启用
        }
        departmentMapper.insert(department);
        return department;
    }
    
    @Override
    @Transactional
    public boolean updateDepartment(Department department) {
        return departmentMapper.updateById(department) > 0;
    }
    
    @Override
    @Transactional
    public boolean deleteDepartment(Long id, boolean forceDelete, Long storeId) {
        Department department = departmentMapper.selectById(id);
        if (department == null) {
            return false;
        }
        // 若传了 storeId，则只允许删除该门店下的部门，保证门店之间互不影响
        if (storeId != null) {
            if (department.getStoreId() == null) {
                throw new IllegalArgumentException("该部门为平台级部门，不能从门店视角删除，请从平台侧操作。");
            }
            if (!department.getStoreId().equals(storeId)) {
                throw new IllegalArgumentException("无权删除其他门店的部门。");
            }
        }
        
        int memberCount = countMembersByDepartment(id);
        if (memberCount > 0) {
            UpdateWrapper<Admin> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("department", department.getName());
            updateWrapper.set("department", null);
            if (department.getStoreId() != null) {
                updateWrapper.eq("service_store_id", department.getStoreId());
            }
            adminMapper.update(null, updateWrapper);
        }
        
        return departmentMapper.deleteById(id) > 0;
    }
    
    @Override
    public int countMembersByDepartment(Long departmentId) {
        Department department = departmentMapper.selectById(departmentId);
        if (department == null) {
            return 0;
        }
        
        QueryWrapper<Admin> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("department", department.getName());
        queryWrapper.eq("role", "staff"); // 只统计服务人员
        if (department.getStoreId() != null) {
            queryWrapper.eq("service_store_id", department.getStoreId());
        }
        return Math.toIntExact(adminMapper.selectCount(queryWrapper));
    }
}

