package com.pethome.controller;

import com.pethome.common.Result;
import com.pethome.entity.Department;
import com.pethome.service.DepartmentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 部门管理Controller
 */
@RestController
@RequestMapping("/api/department")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
@Api(tags = "部门管理")
public class DepartmentController {
    
    @Autowired
    private DepartmentService departmentService;
    
    @GetMapping("/list")
    @ApiOperation("查询部门列表（可选按门店筛选）")
    public Result<List<Department>> getAllDepartments(@RequestParam(required = false) Long storeId) {
        try {
            List<Department> departments = storeId != null
                ? departmentService.getDepartmentsByStoreId(storeId)
                : departmentService.getAllDepartments();
            for (Department d : departments) {
                if ("宠物医疗".equals(d.getName())) {
                    d.setName("宠物医院部门");
                }
            }
            return Result.success(departments);
        } catch (Exception e) {
            return Result.error("查询部门列表失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/{id}")
    @ApiOperation("根据ID查询部门")
    public Result<Department> getDepartmentById(@PathVariable Long id) {
        try {
            Department department = departmentService.getDepartmentById(id);
            if (department != null) {
                return Result.success(department);
            } else {
                return Result.error("部门不存在");
            }
        } catch (Exception e) {
            return Result.error("查询部门失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/create")
    @ApiOperation("创建部门")
    public Result<Department> createDepartment(@RequestBody Department department) {
        try {
            Department created = departmentService.createDepartment(department);
            return Result.success(created);
        } catch (Exception e) {
            return Result.error("创建部门失败: " + e.getMessage());
        }
    }
    
    @PutMapping("/update")
    @ApiOperation("更新部门")
    public Result<Boolean> updateDepartment(@RequestBody Department department) {
        try {
            boolean success = departmentService.updateDepartment(department);
            return success ? Result.success(true) : Result.error("更新失败");
        } catch (Exception e) {
            return Result.error("更新部门失败: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    @ApiOperation("删除部门（自动处理成员）；传 storeId 时仅允许删除该门店下的部门，保证门店间互不影响")
    public Result<Boolean> deleteDepartment(
            @PathVariable Long id,
            @RequestParam(required = false) Long storeId) {
        try {
            boolean success = departmentService.deleteDepartment(id, false, storeId);
            return success ? Result.success(true) : Result.error("删除失败");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("删除部门失败: " + e.getMessage());
        }
    }
}

