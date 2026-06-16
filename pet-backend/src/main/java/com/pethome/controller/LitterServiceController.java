package com.pethome.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.common.Result;
import com.pethome.entity.Admin;
import com.pethome.entity.LitterService;
import com.pethome.service.AdminService;
import com.pethome.service.LitterServiceService;
import com.pethome.util.FileUploadUtil;
import com.pethome.util.JwtUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/litter-services")
@Api(tags = "铲屎服务管理")
public class LitterServiceController {

    @Autowired
    private LitterServiceService litterServiceService;
    
    @Autowired
    private FileUploadUtil fileUploadUtil;
    
    @Autowired
    private AdminService adminService;
    
    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 从 token 中获取当前登录的管理员信息
     */
    private Admin getCurrentAdminFromToken(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return null;
        }
        try {
            token = token.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            if (username != null) {
                Admin admin = adminService.getByUsername(username);
                if (admin != null && admin.isEnabled()) {
                    return admin;
                }
            }
        } catch (Exception e) {
            System.err.println("解析 token 失败: " + e.getMessage());
        }
        return null;
    }

    @GetMapping("/page")
    @ApiOperation("分页查询铲屎服务（服务人员只能查看自己部门的服务）")
    public Result<IPage<LitterService>> getLitterServicePage(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String status,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            Page<LitterService> page = new Page<>(pageNo, pageSize);
            
            // 从 token 获取当前登录用户信息
            Admin currentAdmin = getCurrentAdminFromToken(token);
            
            // 构建查询条件
            QueryWrapper<LitterService> queryWrapper = new QueryWrapper<>();
            
            // 如果是服务人员，确保只能看到铲屎服务（实际上所有 litter_services 都是铲屎服务）
            // 但这里添加额外的安全检查
            if (currentAdmin != null && "staff".equals(currentAdmin.getRole())) {
                // 服务人员只能查看"上门铲屎"部门的服务
                // 由于 litter_services 表只包含铲屎服务，所以这里主要是权限控制
                // 如果将来需要区分不同部门的服务，可以添加 department 字段
                String department = currentAdmin.getDepartment();
                if (department != null && !"上门铲屎".equals(department)) {
                    // 如果不是"上门铲屎"部门，返回空列表
                    return Result.success(new Page<>(pageNo, pageSize));
                }
            }
            
            // 添加搜索条件
            if (name != null && !name.trim().isEmpty()) {
                queryWrapper.like("name", name.trim());
            }
            if (status != null && !status.trim().isEmpty()) {
                queryWrapper.eq("status", status.trim());
            }
            
            // 只查询未删除的服务
            queryWrapper.eq("is_deleted", false);
            
            // 按创建时间倒序
            queryWrapper.orderByDesc("created_at");
            
            IPage<LitterService> result = litterServiceService.getLitterServiceList(page, queryWrapper);
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询服务列表失败: " + e.getMessage());
        }
    }

    @PostMapping("/create")
    @ApiOperation("创建铲屎服务")
    public Result<LitterService> createLitterService(@RequestBody LitterService litterService) {
        return Result.success(litterServiceService.createLitterService(litterService));
    }

    @PutMapping("/update")
    @ApiOperation("更新铲屎服务")
    public Result<LitterService> updateLitterService(@RequestBody LitterService litterService) {
        return Result.success(litterServiceService.updateLitterService(litterService));
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除铲屎服务")
    public Result<Boolean> deleteLitterService(@PathVariable Long id) {
        return Result.success(litterServiceService.deleteLitterService(id));
    }

    @GetMapping("/{id}")
    @ApiOperation("获取铲屎服务详情")
    public Result<LitterService> getLitterServiceDetail(@PathVariable Long id) {
        return Result.success(litterServiceService.getLitterServiceById(id));
    }

    @PostMapping("/upload")
    @ApiOperation("上传服务图片")
    public Result<String> uploadServiceImage(@RequestParam("file") MultipartFile file, @RequestParam("id") Long id) {
        return updateServiceImage(id, file);
    }

    @PutMapping("/{id}/image")
    @ApiOperation("更新服务图片")
    public Result<String> updateServiceImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        try {
            // 使用FileUploadUtil上传文件
            String imageUrl = fileUploadUtil.uploadImage(file, "files");
            
            // 更新服务记录
            LitterService service = litterServiceService.getLitterServiceById(id);
            if (service != null) {
                service.setImageUrl(imageUrl);
                litterServiceService.updateLitterService(service);
                return Result.success(imageUrl);
            } else {
                return Result.error("服务不存在");
            }
        } catch (Exception e) {
            return Result.error("图片上传失败: " + e.getMessage());
        }
    }
}
