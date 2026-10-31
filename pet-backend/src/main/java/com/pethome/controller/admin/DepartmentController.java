package com.pethome.controller.admin;


import com.pethome.common.BusinessException;
import com.pethome.common.SafeCall;
import com.pethome.common.Result;
import com.pethome.entity.Department;
import com.pethome.security.RequestIdentity;
import com.pethome.service.admin.DepartmentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 部门管理Controller
 */
@RestController
@RequestMapping("/api/department")
@Tag(name = "部门管理")
public class DepartmentController {

    private static final Logger log = LoggerFactory.getLogger(DepartmentController.class);
    
    @Autowired
    private DepartmentService departmentService;
    
    @GetMapping("/list")
    @Operation(summary = "查询部门列表（可选按门店筛选）")
    public Result<List<Department>> getAllDepartments(@RequestParam(required = false) Long storeId) {
        return com.pethome.common.SafeCall.of(() -> {
            List<Department> departments = storeId != null
                ? departmentService.getDepartmentsByStoreId(storeId)
                : departmentService.getAllDepartments();
            for (Department d : departments) {
                if ("宠物医疗".equals(d.getName())) {
                    d.setName("宠物医院部门");
                }
            }
            return Result.success(departments);
        }, "查询部门列表失败，请稍后重试", log);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询部门")
    public Result<Department> getDepartmentById(@PathVariable Long id) {
        return com.pethome.common.SafeCall.of(() -> {
            Department department = departmentService.getDepartmentById(id);
            if (department != null) {
                return Result.success(department);
            } else {
                throw new BusinessException("部门不存在");
            }
        }, "查询部门失败，请稍后重试", log);
    }
    
    @PostMapping("/create")
    @Operation(summary = "创建部门")
    public Result<Department> createDepartment(@RequestBody Department department, HttpServletRequest request) {
        // 安全修复：部门为主数据，此前任意登录用户即可增删改；分店账号仅限本门店
        if (!RequestIdentity.isAdmin(request)) {
            throw new BusinessException(403, "无权创建部门");
        }
        Long scope = scopedStoreId(request);
        if (scope != null && (department.getStoreId() == null || !scope.equals(department.getStoreId()))) {
            throw new BusinessException(403, "无权在其他门店创建部门");
        }
        return com.pethome.common.SafeCall.of(() -> {
            Department created = departmentService.createDepartment(department);
            return Result.success(created);
        }, "创建部门失败，请稍后重试", log);
    }
    
    @PutMapping("/update")
    @Operation(summary = "更新部门")
    public Result<Boolean> updateDepartment(@RequestBody Department department, HttpServletRequest request) {
        // 安全修复：先校验原记录与目标门店归属，防止改写他店部门
        if (!RequestIdentity.isAdmin(request)) {
            throw new BusinessException(403, "无权更新部门");
        }
        if (department.getId() == null) {
            throw new BusinessException("部门ID不能为空");
        }
        Department existing = departmentService.getDepartmentById(department.getId());
        if (existing == null) {
            throw new BusinessException("部门不存在");
        }
        Long scope = scopedStoreId(request);
        if (scope != null && (!scope.equals(existing.getStoreId())
                || (department.getStoreId() != null && !scope.equals(department.getStoreId()))) ) {
            throw new BusinessException(403, "无权更新其他门店的部门");
        }
        return com.pethome.common.SafeCall.of(() -> {
            boolean success = departmentService.updateDepartment(department);
            if (!success) {
                throw new BusinessException("更新失败");
            }
            return Result.success(true);
        }, "更新部门失败，请稍后重试", log);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "删除部门（自动处理成员）；传 storeId 时仅允许删除该门店下的部门，保证门店间互不影响")
    public Result<Boolean> deleteDepartment(
            @PathVariable Long id,
            @RequestParam(required = false) Long storeId,
            HttpServletRequest request) {
        // 安全修复：删除部门会解绑成员，仅限管理端，且分店账号仅限本门店部门
        if (!RequestIdentity.isAdmin(request)) {
            throw new BusinessException(403, "无权删除部门");
        }
        Department existing = departmentService.getDepartmentById(id);
        if (existing == null) {
            throw new BusinessException("部门不存在");
        }
        Long scope = scopedStoreId(request);
        if (scope != null) {
            if (!scope.equals(existing.getStoreId())) {
                throw new BusinessException(403, "无权删除其他门店的部门");
            }
            storeId = scope;
        }
        final Long finalStoreId = storeId;
        return SafeCall.of(() -> {
            boolean success;
            try {
                success = departmentService.deleteDepartment(id, false, finalStoreId);
            } catch (IllegalArgumentException e) {
                // 预期内的业务校验失败：原始提示与 code(500) 保持不变
                log.error("操作失败", e);
                throw new BusinessException(e.getMessage());
            }
            if (!success) {
                throw new BusinessException("删除失败");
            }
            return Result.success(true);
        }, "删除部门失败，请稍后重试", log);
    }

    /**
     * 门店维度：平台管理员返回 null（不限门店）；店长/分店管理员返回其门店，无归属返回 -1（拒绝跨店操作）。
     */
    private Long scopedStoreId(HttpServletRequest request) {
        if (RequestIdentity.isPlatformAdmin(request)) {
            return null;
        }
        Long storeId = RequestIdentity.serviceStoreId(request);
        return storeId != null ? storeId : -1L;
    }
}

