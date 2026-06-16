package com.pethome.controller;

import com.pethome.common.Result;
import com.pethome.entity.Admin;
import com.pethome.entity.AdminPermission;
import com.pethome.service.AdminService;
import com.pethome.service.AdminPermissionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 管理员权限控制器
 */
@Api(tags = "管理员权限管理")
@RestController
@RequestMapping("/api/admin/permissions")
public class AdminPermissionController {
    
    @Autowired
    private AdminPermissionService adminPermissionService;

    @Autowired
    private AdminService adminService;
    
    private String normalizeRole(String role) {
        if (role == null) return "staff";
        String r = role.trim().toLowerCase();
        if ("super_admin".equals(r)) return "admin";
        return r;
    }

    private Admin resolveCurrentAdmin(HttpServletRequest request) {
        Object adminIdObj = request.getAttribute("adminId");
        if (!(adminIdObj instanceof Long)) {
            return null;
        }
        return adminService.getById((Long) adminIdObj);
    }

    private boolean canManageTarget(Admin currentAdmin, Admin targetAdmin) {
        if (currentAdmin == null || targetAdmin == null) return false;
        String currentRole = normalizeRole(currentAdmin.getRole());
        String targetRole = normalizeRole(targetAdmin.getRole());

        // 平台管理员可管理所有账号权限
        if ("admin".equals(currentRole)) return true;

        // 分店管理员仅可管理本门店的非平台管理员账号
        if ("store_admin".equals(currentRole)) {
            if (currentAdmin.getServiceStoreId() == null) return false;
            if ("admin".equals(targetRole)) return false;
            return currentAdmin.getServiceStoreId().equals(targetAdmin.getServiceStoreId());
        }

        // 其他角色不可进入权限管理
        return false;
    }
    
    /**
     * 获取指定账号的权限列表
     */
    @GetMapping("/{adminId}")
    @ApiOperation("获取指定账号的权限列表")
    public Result<List<AdminPermission>> getPermissions(@PathVariable Long adminId, HttpServletRequest request) {
        try {
            Admin currentAdmin = resolveCurrentAdmin(request);
            Admin targetAdmin = adminService.getById(adminId);
            if (!canManageTarget(currentAdmin, targetAdmin)) {
                return Result.error("无权查看该账号权限");
            }
            
            List<AdminPermission> permissions = adminPermissionService.getPermissionsByAdminId(adminId);
            return Result.success(permissions);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取权限列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有可用的权限列表（用于权限选择）
     */
    @GetMapping("/available")
    @ApiOperation("获取所有可用的权限列表")
    public Result<List<AdminPermission>> getAvailablePermissions(HttpServletRequest request) {
        try {
            Admin currentAdmin = resolveCurrentAdmin(request);
            if (currentAdmin == null) {
                return Result.error("未登录或无权限");
            }
            List<AdminPermission> permissions = adminPermissionService.getAllAvailablePermissions();
            return Result.success(permissions);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取可用权限列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 为指定账号设置权限
     */
    @PostMapping("/{adminId}")
    @ApiOperation("为指定账号设置权限")
    public Result<String> setPermissions(
            @PathVariable Long adminId,
            @RequestBody Object requestBody,
            HttpServletRequest request) {
        try {
            Admin currentAdmin = resolveCurrentAdmin(request);
            Admin targetAdmin = adminService.getById(adminId);
            if (!canManageTarget(currentAdmin, targetAdmin)) {
                return Result.error("无权设置该账号权限");
            }
            
            // 添加调试日志
            System.out.println("=== 接收权限设置请求 ===");
            System.out.println("AdminId: " + adminId);
            System.out.println("RequestBody 类型: " + (requestBody != null ? requestBody.getClass().getName() : "null"));
            System.out.println("RequestBody 内容: " + requestBody);
            
            // 处理权限数据
            List<AdminPermission> permissions = new java.util.ArrayList<>();
            
            // 处理不同的数据格式
            if (requestBody instanceof List) {
                // 如果是列表，直接处理
                System.out.println("检测到 List 类型");
                @SuppressWarnings("unchecked")
                List<Object> list = (List<Object>) requestBody;
                System.out.println("List 大小: " + list.size());
                for (Object item : list) {
                    System.out.println("处理列表项: " + item);
                    AdminPermission perm = convertToAdminPermission(item);
                    if (perm != null && perm.getPermissionCode() != null && !perm.getPermissionCode().trim().isEmpty()) {
                        permissions.add(perm);
                        System.out.println("成功添加权限: " + perm.getPermissionCode());
                    } else {
                        System.err.println("跳过无效权限项: " + item);
                    }
                }
            } else if (requestBody instanceof java.util.Map) {
                // 如果是 Map，可能是包装的对象，尝试从 data 字段获取数组
                System.out.println("检测到 Map 类型");
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> map = (java.util.Map<String, Object>) requestBody;
                System.out.println("Map 键: " + map.keySet());
                
                // 尝试多个可能的字段名
                Object data = map.get("data");
                if (data == null) {
                    data = map.get("permissions");
                }
                if (data == null) {
                    data = map.get("list");
                }
                
                if (data instanceof List) {
                    System.out.println("从 Map 中找到 List 数据");
                    @SuppressWarnings("unchecked")
                    List<Object> list = (List<Object>) data;
                    System.out.println("List 大小: " + list.size());
                    for (Object item : list) {
                        AdminPermission perm = convertToAdminPermission(item);
                        if (perm != null && perm.getPermissionCode() != null && !perm.getPermissionCode().trim().isEmpty()) {
                            permissions.add(perm);
                            System.out.println("成功添加权限: " + perm.getPermissionCode());
                        } else {
                            System.err.println("跳过无效权限项: " + item);
                        }
                    }
                } else {
                    // 如果 data 不是数组，尝试直接转换整个对象
                    System.out.println("尝试直接转换 Map 对象");
                    AdminPermission perm = convertToAdminPermission(requestBody);
                    if (perm != null && perm.getPermissionCode() != null && !perm.getPermissionCode().trim().isEmpty()) {
                        permissions.add(perm);
                        System.out.println("成功添加权限: " + perm.getPermissionCode());
                    } else {
                        System.err.println("无法转换 Map 对象为权限");
                    }
                }
            } else {
                System.err.println("未知的请求体类型: " + (requestBody != null ? requestBody.getClass().getName() : "null"));
            }
            
            System.out.println("最终权限列表大小: " + permissions.size());
            
            // 允许空列表：表示清空该账号的全部细粒度权限
            adminPermissionService.setPermissions(adminId, permissions);
            return Result.success(permissions.isEmpty() ? "权限已清空" : "权限设置成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("设置权限失败: " + e.getMessage());
        }
    }
    
    /**
     * 将对象转换为 AdminPermission
     */
    private AdminPermission convertToAdminPermission(Object item) {
        if (item == null) {
            return null;
        }
        
        if (item instanceof AdminPermission) {
            return (AdminPermission) item;
        }
        
        if (item instanceof java.util.Map) {
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> map = (java.util.Map<String, Object>) item;
            System.out.println("转换 Map 对象，键: " + map.keySet());
            System.out.println("Map 内容: " + map);
            
            AdminPermission perm = new AdminPermission();
            
            // 尝试多种可能的字段名（支持驼峰和下划线）
            Object code = map.get("permissionCode");
            if (code == null) {
                code = map.get("permission_code");
            }
            if (code == null) {
                code = map.get("code");
            }
            if (code == null) {
                // 尝试所有键，看看是否有类似的
                for (String key : map.keySet()) {
                    if (key != null && (key.toLowerCase().contains("code") || key.toLowerCase().contains("permission"))) {
                        code = map.get(key);
                        System.out.println("找到可能的权限代码字段: " + key + " = " + code);
                        break;
                    }
                }
            }
            
            System.out.println("提取的权限代码: " + code);
            
            if (code != null && !code.toString().trim().isEmpty()) {
                perm.setPermissionCode(code.toString().trim());
            } else {
                // 如果 permissionCode 为空，跳过这个权限项
                System.err.println("警告：权限代码为空，跳过该权限项");
                System.err.println("Map 的所有键: " + map.keySet());
                System.err.println("Map 的所有值: " + map.values());
                return null;
            }
            
            // 尝试多种可能的字段名
            Object name = map.get("permissionName");
            if (name == null) {
                name = map.get("permission_name");
            }
            if (name == null) {
                name = map.get("name");
            }
            
            if (name != null && !name.toString().trim().isEmpty()) {
                perm.setPermissionName(name.toString().trim());
            } else {
                // 如果没有名称，使用代码作为名称
                perm.setPermissionName(perm.getPermissionCode());
            }
            
            Object status = map.get("status");
            if (status != null) {
                if (status instanceof Integer) {
                    perm.setStatus((Integer) status);
                } else {
                    try {
                        perm.setStatus(Integer.valueOf(status.toString()));
                    } catch (NumberFormatException e) {
                        perm.setStatus(1);
                    }
                }
            } else {
                perm.setStatus(1);
            }
            
            // 验证必要字段
            if (perm.getPermissionCode() == null || perm.getPermissionCode().trim().isEmpty()) {
                System.err.println("错误：转换后的权限代码仍为空");
                return null;
            }
            
            System.out.println("转换成功: " + perm.getPermissionCode() + " -> " + perm.getPermissionName());
            return perm;
        }
        
        return null;
    }
    
    /**
     * 检查账号是否有某个权限
     */
    @GetMapping("/check/{adminId}")
    @ApiOperation("检查账号是否有某个权限")
    public Result<Boolean> checkPermission(
            @PathVariable Long adminId,
            @RequestParam String permissionCode,
            HttpServletRequest request) {
        try {
            Admin currentAdmin = resolveCurrentAdmin(request);
            Admin targetAdmin = adminService.getById(adminId);
            if (!canManageTarget(currentAdmin, targetAdmin)) {
                return Result.error("无权检查该账号权限");
            }
            boolean hasPermission = adminPermissionService.hasPermission(adminId, permissionCode);
            return Result.success(hasPermission);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("检查权限失败: " + e.getMessage());
        }
    }
}
