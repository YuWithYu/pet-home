package com.pethome.service.admin.impl;

import com.pethome.common.BusinessException;
import com.pethome.entity.AdminPermission;
import com.pethome.mapper.AdminPermissionMapper;
import com.pethome.service.admin.AdminPermissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 管理员权限服务实现类
 */
@Service
public class AdminPermissionServiceImpl implements AdminPermissionService {

    private static final Logger log = LoggerFactory.getLogger(AdminPermissionServiceImpl.class);

    @Autowired
    private AdminPermissionMapper adminPermissionMapper;
    
    // 定义所有可用的权限列表（对应路由路径）
    private static final List<AdminPermission> AVAILABLE_PERMISSIONS = new ArrayList<>();
    
    static {
        // 与 vue3-admin-better/src/router/index.js 中 asyncRoutes 保持一致；无对应路由的项不列出，避免勾选后无法访问
        
        // ========== 内容管理（与侧栏顺序一致）==========
        AVAILABLE_PERMISSIONS.add(createPermission(null, "/content/banner", "轮播图管理"));
        AVAILABLE_PERMISSIONS.add(createPermission(null, "/content/notice", "公告管理"));
        AVAILABLE_PERMISSIONS.add(createPermission(null, "/content/community", "社区管理"));
        AVAILABLE_PERMISSIONS.add(createPermission(null, "/content/complaint", "投诉举报"));
        
        // ========== 商城管理 ==========
        AVAILABLE_PERMISSIONS.add(createPermission(null, "/mall", "商城管理"));
        AVAILABLE_PERMISSIONS.add(createPermission(null, "/points-mall", "积分商城管理"));
        
        // ========== 服务管理 ==========
        AVAILABLE_PERMISSIONS.add(createPermission(null, "/service-platform/litter-service", "上门铲屎服务管理"));
        AVAILABLE_PERMISSIONS.add(createPermission(null, "/service-platform/grooming-services", "洗护服务管理"));
        AVAILABLE_PERMISSIONS.add(createPermission(null, "/service-platform/hospital-service", "宠物医院服务管理"));
        AVAILABLE_PERMISSIONS.add(createPermission(null, "/service-staff", "服务人员管理"));
        AVAILABLE_PERMISSIONS.add(createPermission(null, "/my-schedule", "我的排班"));
        AVAILABLE_PERMISSIONS.add(createPermission(null, "/service-stores", "服务门店管理"));
        // 聚合后的预约订单（与前端 /service-orders/appointment-orders 一致）
        AVAILABLE_PERMISSIONS.add(createPermission(null, "/service-orders/appointment-orders", "预约订单管理"));
        // 旧版子路径：仍保留在「可用权限」中，方便兼容数据库里已保存的旧 code
        AVAILABLE_PERMISSIONS.add(createPermission(null, "/service-orders/litter-service-orders", "预约订单管理（旧路径）"));
        AVAILABLE_PERMISSIONS.add(createPermission(null, "/service-orders/grooming-service-orders", "宠物洗护预约订单（旧路径）"));
        AVAILABLE_PERMISSIONS.add(createPermission(null, "/service-orders/hospital-service-orders", "宠物医院预约订单（旧路径）"));
        
        // ========== 订单核销 / 门店客服（已从 service-workbench 提升为一级路由）==========
        AVAILABLE_PERMISSIONS.add(createPermission(null, "/verify", "订单核销"));
        AVAILABLE_PERMISSIONS.add(createPermission(null, "/outlet-customer-chat", "门店客服"));
        AVAILABLE_PERMISSIONS.add(createPermission(null, "/service-workbench/verify", "订单核销（旧路径）"));
        AVAILABLE_PERMISSIONS.add(createPermission(null, "/service-workbench/outlet-customer-chat", "门店客服（旧路径）"));
        
        // ========== 用户管理 ==========
        AVAILABLE_PERMISSIONS.add(createPermission(null, "/users/list", "用户列表"));
        AVAILABLE_PERMISSIONS.add(createPermission(null, "/users/permissions", "账号管理"));
        AVAILABLE_PERMISSIONS.add(createPermission(null, "/users/platform-customer-chat", "平台客服"));
        
        // ========== 统计报表 ==========
        AVAILABLE_PERMISSIONS.add(createPermission(null, "/reports/dashboard", "数据大屏"));
    }
    
    private static AdminPermission createPermission(Long adminId, String code, String name) {
        AdminPermission permission = new AdminPermission();
        permission.setAdminId(adminId);
        permission.setPermissionCode(code);
        permission.setPermissionName(name);
        permission.setStatus(1);
        return permission;
    }
    
    @Override
    public List<AdminPermission> getPermissionsByAdminId(Long adminId) {
        if (adminId == null) {
            return new ArrayList<>();
        }
        List<AdminPermission> permissions = adminPermissionMapper.selectByAdminId(adminId);
        log.debug("查询权限: adminId={}, count={}", adminId, permissions != null ? permissions.size() : 0);
        return permissions != null ? permissions : new ArrayList<>();
    }

    @Override
    public boolean hasPermission(Long adminId, String permissionCode) {
        AdminPermission permission = adminPermissionMapper.selectByAdminIdAndCode(adminId, permissionCode);
        return permission != null && permission.getStatus() == 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setPermissions(Long adminId, List<AdminPermission> permissions) {
        if (adminId == null) {
            throw new BusinessException("管理员ID不能为空");
        }

        log.debug("设置权限: adminId={}, count={}", adminId, permissions != null ? permissions.size() : 0);

        // 先删除该管理员的所有权限
        int deleted = adminPermissionMapper.deleteByAdminId(adminId);
        log.debug("删除旧权限数量: {}", deleted);

        // 如果有新权限，批量插入
        if (permissions != null && !permissions.isEmpty()) {
            // 设置管理员ID和创建时间
            for (AdminPermission permission : permissions) {
                if (permission == null) continue;

                permission.setAdminId(adminId);
                if (permission.getStatus() == null) {
                    permission.setStatus(1);
                }
                if (permission.getCreateTime() == null) {
                    permission.setCreateTime(LocalDateTime.now());
                }
                if (permission.getUpdateTime() == null) {
                    permission.setUpdateTime(LocalDateTime.now());
                }
            }

            // 过滤掉无效的权限
            List<AdminPermission> validPermissions = permissions.stream()
                .filter(p -> p != null && p.getPermissionCode() != null && !p.getPermissionCode().trim().isEmpty())
                .collect(java.util.stream.Collectors.toList());

            if (!validPermissions.isEmpty()) {
                adminPermissionMapper.batchInsert(validPermissions);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePermissionsByAdminId(Long adminId) {
        adminPermissionMapper.deleteByAdminId(adminId);
    }
    
    @Override
    public List<AdminPermission> getAllAvailablePermissions() {
        return new ArrayList<>(AVAILABLE_PERMISSIONS);
    }
}
