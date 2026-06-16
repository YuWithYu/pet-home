package com.pethome.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pethome.entity.Admin;
import com.pethome.mapper.AdminMapper;
import com.pethome.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理员服务实现类
 */
@Service
public class AdminServiceImpl implements AdminService {
    
    @Autowired
    private AdminMapper adminMapper;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public Admin getByUsername(String username) {
        return adminMapper.selectByUsername(username);
    }
    
    @Override
    public Admin getById(Long id) {
        return adminMapper.selectById(id);
    }
    
    @Override
    @Transactional
    public Admin createAdmin(Admin admin) {
        // 验证必填字段
        if (admin.getUsername() == null || admin.getUsername().trim().isEmpty()) {
            throw new RuntimeException("用户名不能为空");
        }
        if (admin.getPassword() == null || admin.getPassword().trim().isEmpty()) {
            throw new RuntimeException("密码不能为空");
        }
        if (admin.getName() == null || admin.getName().trim().isEmpty()) {
            throw new RuntimeException("姓名不能为空");
        }
        
        // 检查用户名是否已存在
        Admin existing = adminMapper.selectByUsername(admin.getUsername());
        if (existing != null) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 加密密码
        if (admin.getPassword() != null && !admin.getPassword().startsWith("$2a$")) {
            // 如果不是已经加密的密码，则加密
            admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        }
        
        // 设置默认值
        if (admin.getRole() == null || admin.getRole().trim().isEmpty()) {
            admin.setRole("staff"); // 默认为普通员工
        }
        if (admin.getStatus() == null) {
            admin.setStatus(1); // 默认启用
        }
        if (admin.getCreateTime() == null) {
            admin.setCreateTime(LocalDateTime.now());
        }
        
        adminMapper.insert(admin);
        return admin;
    }
    
    @Override
    @Transactional
    public boolean updateAdmin(Admin admin) {
        if (admin.getId() == null) {
            throw new RuntimeException("管理员ID不能为空");
        }
        
        // 如果提供了新密码，需要加密
        if (admin.getPassword() != null && !admin.getPassword().trim().isEmpty()) {
            if (!admin.getPassword().startsWith("$2a$")) {
                admin.setPassword(passwordEncoder.encode(admin.getPassword()));
            }
        } else {
            // 如果没有提供密码，保持原密码不变（需要从数据库获取）
            Admin existing = adminMapper.selectById(admin.getId());
            if (existing != null) {
                admin.setPassword(existing.getPassword());
            }
        }
        
        return adminMapper.updateById(admin) > 0;
    }
    
    @Override
    @Transactional
    public boolean deleteAdmin(Long id) {
        return adminMapper.deleteById(id) > 0;
    }
    
    @Override
    public List<Admin> listStaffByDepartment(String department) {
        if (department == null || department.trim().isEmpty()) {
            return adminMapper.selectAllStaff();
        }
        return adminMapper.selectStaffByDepartment(department);
    }
    
    @Override
    public List<Admin> listAllStaff() {
        return adminMapper.selectAllStaff();
    }

    @Override
    public List<Admin> listAllStaffWithDisabled() {
        return adminMapper.selectAllStaffWithDisabled();
    }

    @Override
    public List<Admin> listStaffByStoreId(Long storeId) {
        if (storeId == null) {
            return listAllStaff();
        }
        QueryWrapper<Admin> wrapper = new QueryWrapper<>();
        wrapper.ne("role", "admin");
        wrapper.eq("status", 1);
        wrapper.eq("service_store_id", storeId);
        wrapper.orderByDesc("create_time");
        return adminMapper.selectList(wrapper);
    }

    @Override
    public List<Admin> listStaffByStoreIdWithDisabled(Long storeId) {
        if (storeId == null) {
            return listAllStaffWithDisabled();
        }
        QueryWrapper<Admin> wrapper = new QueryWrapper<>();
        wrapper.ne("role", "admin");
        wrapper.eq("service_store_id", storeId);
        wrapper.orderByDesc("create_time");
        return adminMapper.selectList(wrapper);
    }
    
    @Override
    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        
        // 判断是否是 BCrypt 加密的密码（以 $2a$ 开头，长度约60）
        if (encodedPassword.startsWith("$2a$") && encodedPassword.length() == 60) {
            // 使用 BCrypt 验证
            try {
                return passwordEncoder.matches(rawPassword, encodedPassword);
            } catch (Exception e) {
                System.err.println("BCrypt 密码验证失败: " + e.getMessage());
                return false;
            }
        } else {
            // 明文密码，直接比较（兼容旧密码，迁移后应全部加密）
            return rawPassword.equals(encodedPassword);
        }
    }
    
    @Override
    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
}

