package com.pethome.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.common.Result;
import com.pethome.common.SafeCall;
import com.pethome.entity.Admin;
import com.pethome.entity.Complaint;
import com.pethome.security.AdminContext;
import com.pethome.security.RequestIdentity;
import com.pethome.service.admin.AdminAuthAppService;
import com.pethome.service.admin.AdminStaffAppService;
import com.pethome.service.admin.AdminUserAppService;
import com.pethome.service.admin.AdminWorkOrderAppService;
import com.pethome.service.community.ComplaintService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员控制器
 *
 * <p>仅做身份解析与响应包装，具体业务编排由 {@code service.admin} 下的应用服务承担。</p>
 */
@RestController
@RequestMapping("/api/admin")
@Tag(name = "管理员接口")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private AdminAuthAppService adminAuthAppService;

    @Autowired
    private AdminStaffAppService adminStaffAppService;

    @Autowired
    private AdminUserAppService adminUserAppService;

    @Autowired
    private AdminWorkOrderAppService adminWorkOrderAppService;

    @Autowired(required = false)
    private AdminContext adminContext;

    @Autowired(required = false)
    private ComplaintService complaintService;

    /**
     * 管理员登录
     */
    @PostMapping("/login")
    @Operation(summary = "管理员登录")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> request) {
        return SafeCall.of(() -> {
            String username = request == null ? null : request.get("username");
            String password = request == null ? null : request.get("password");
            return Result.success(adminAuthAppService.login(username, password));
        }, "登录失败，请稍后重试", log);
    }

    /**
     * 获取当前登录管理员信息
     */
    @GetMapping("/info")
    @Operation(summary = "获取当前登录管理员信息")
    public Result<Map<String, Object>> getCurrentAdmin(
            @RequestHeader(value = "Authorization", required = false) String token) {
        return SafeCall.of(() -> Result.success(adminAuthAppService.getCurrentAdminInfo(token)),
                "获取管理员信息失败，请稍后重试", log);
    }

    /**
     * 获取员工列表（根据部门过滤，包括商家账号）
     */
    @GetMapping({"/staff/list", "/listStaff"})
    @Operation(summary = "获取员工列表（包括商家账号，可选按门店筛选）")
    public Result<List<Admin>> listStaff(@RequestParam(required = false) String department,
                                         @RequestParam(required = false) Long storeId,
                                         @RequestParam(required = false) Boolean accountsOnly,
                                         @RequestHeader(value = "Authorization", required = false) String token) {
        return SafeCall.of(() -> {
            Admin currentAdmin = adminContext != null ? adminContext.getCurrentAdmin(token) : null;
            Long currentAdminId = currentAdmin != null ? currentAdmin.getId() : null;
            return Result.success(adminStaffAppService.listStaff(department, storeId, accountsOnly, currentAdminId));
        }, "查询员工列表失败，请稍后重试", log);
    }

    /**
     * 当前登录用户更新头像（任意后台用户均可调用，只更新自己的头像）
     */
    @PutMapping("/profile/avatar")
    @Operation(summary = "当前用户更新头像")
    public Result<Map<String, Object>> updateMyAvatar(@RequestBody Map<String, String> body,
                                                      @RequestHeader(value = "Authorization", required = false) String token) {
        return SafeCall.of(() -> {
            String avatar = body == null ? null : body.get("avatar");
            Map<String, Object> data = new HashMap<>();
            data.put("avatar", adminAuthAppService.updateMyAvatar(token, avatar));
            return Result.success(data);
        }, "更新头像失败，请稍后重试", log);
    }

    /**
     * 添加员工或商家账号
     */
    @PostMapping("/staff/add")
    @Operation(summary = "添加员工或商家账号（仅超级管理员可操作）")
    public Result<Admin> addStaff(@RequestBody Admin staff, HttpServletRequest request) {
        return SafeCall.of(() -> Result.success(adminStaffAppService.addStaff(staff, RequestIdentity.role(request))),
                "添加账号失败，请稍后重试", log);
    }

    /**
     * 更新员工或商家信息
     */
    @PutMapping("/staff/update")
    @Operation(summary = "更新员工或商家信息")
    public Result<Boolean> updateStaff(@RequestBody Admin staff, HttpServletRequest request) {
        return SafeCall.of(() -> Result.success(
                adminStaffAppService.updateStaff(staff, RequestIdentity.adminId(request))),
                "更新账号信息失败，请稍后重试", log);
    }

    /**
     * 管理员创建 C 端用户（免短信验证码）
     */
    @PostMapping("/user/create")
    @Operation(summary = "管理员创建用户")
    public Result<Map<String, Object>> createUserByAdmin(@RequestBody Map<String, String> body) {
        return SafeCall.of(() -> {
            String phone = body == null ? null : body.get("phone");
            String password = body == null ? null : body.get("password");
            String nickname = body == null ? null : body.get("nickname");
            Map<String, Object> data = new HashMap<>();
            data.put("userId", adminUserAppService.createUser(phone, password, nickname));
            return Result.success(data);
        }, "创建用户失败，请稍后重试", log);
    }

    /**
     * 管理员删除用户（C端用户）
     */
    @DeleteMapping("/user/{id}")
    @Operation(summary = "管理员删除用户")
    public Result<Boolean> deleteUser(@PathVariable Long id) {
        return SafeCall.of(() -> {
            if (adminUserAppService.deleteUser(id)) {
                return Result.success(true);
            }
            return Result.error("用户不存在或删除失败");
        }, "删除用户失败，请稍后重试", log);
    }

    /**
     * 删除员工
     */
    @DeleteMapping("/staff/{id}")
    @Operation(summary = "删除员工")
    public Result<Boolean> deleteStaff(@PathVariable Long id, HttpServletRequest request) {
        return SafeCall.of(() -> Result.success(
                adminStaffAppService.deleteStaff(id, RequestIdentity.adminId(request))),
                "删除员工失败，请稍后重试", log);
    }

    /**
     * 根据ID查询员工
     */
    @GetMapping("/staff/{id}")
    @Operation(summary = "根据ID查询员工")
    public Result<Admin> getStaffById(@PathVariable Long id) {
        return SafeCall.of(() -> Result.success(adminStaffAppService.getStaffById(id)),
                "查询员工失败，请稍后重试", log);
    }

    /**
     * 投诉举报分页列表（超级管理员可见）
     */
    @GetMapping("/complaints")
    @Operation(summary = "投诉举报分页列表")
    public Result<IPage<Complaint>> getComplaintPage(@RequestParam(defaultValue = "1") Integer pageNo,
                                                     @RequestParam(defaultValue = "10") Integer pageSize,
                                                     @RequestParam(required = false) String status,
                                                     @RequestParam(required = false) String type) {
        if (complaintService == null) {
            return Result.error("投诉服务未配置");
        }
        return Result.success(complaintService.page(new Page<>(pageNo, pageSize), status, type));
    }

    /**
     * 投诉举报详情
     */
    @GetMapping("/complaints/{id}")
    @Operation(summary = "投诉举报详情")
    public Result<Complaint> getComplaintById(@PathVariable Long id) {
        if (complaintService == null) {
            return Result.error("投诉服务未配置");
        }
        Complaint complaint = complaintService.getById(id);
        if (complaint == null) {
            return Result.error("记录不存在");
        }
        return Result.success(complaint);
    }

    /**
     * 更新投诉举报状态：pending-待处理，processing-处理中，resolved-已解决
     */
    @PutMapping("/complaints/{id}/status")
    @Operation(summary = "更新投诉状态")
    public Result<Boolean> updateComplaintStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        if (complaintService == null) {
            return Result.error("投诉服务未配置");
        }
        String status = body != null ? body.get("status") : null;
        if (status == null || status.trim().isEmpty()) {
            return Result.error("状态不能为空");
        }
        String normalized = status.trim();
        // 白名单校验：防止任意字符串写入状态字段
        if (!"pending".equals(normalized) && !"processing".equals(normalized) && !"resolved".equals(normalized)) {
            return Result.error("非法的投诉状态: " + normalized);
        }
        if (!complaintService.updateStatus(id, normalized)) {
            return Result.error("投诉状态更新失败，记录可能不存在");
        }
        return Result.success(true);
    }

    /**
     * 工作人员小程序端：获取当前登录账号的工单列表（仅自己的工单）
     */
    @GetMapping("/self/work-orders")
    @Operation(summary = "工作人员获取自己的工单列表（小程序端）")
    public Result<List<Map<String, Object>>> getMyWorkOrders(HttpServletRequest request) {
        return SafeCall.of(() -> Result.success(
                adminWorkOrderAppService.getMyWorkOrders(RequestIdentity.adminId(request))),
                "获取工单列表失败，请稍后重试", log);
    }

    /**
     * 工作人员小程序端：获取当前登录账号的真实评价列表
     */
    @GetMapping("/self/reviews")
    @Operation(summary = "工作人员获取自己的真实评价列表（小程序端）")
    public Result<List<Map<String, Object>>> getMyReviews(HttpServletRequest request) {
        return SafeCall.of(() -> Result.success(
                adminWorkOrderAppService.getMyReviews(RequestIdentity.adminId(request))),
                "获取评价列表失败，请稍后重试", log);
    }

    /**
     * 工作人员小程序端：更新自己的工单状态（确认/完成/取消）
     */
    @PostMapping("/self/work-orders/status")
    @Operation(summary = "工作人员更新工单状态（小程序端）")
    public Result<String> updateMyWorkOrderStatus(HttpServletRequest request,
                                                  @RequestBody Map<String, Object> body) {
        return SafeCall.of(() -> Result.success(
                adminWorkOrderAppService.updateMyWorkOrderStatus(RequestIdentity.adminId(request), body)),
                "更新失败，请稍后重试", log);
    }

    /**
     * 管理后台/工作人员统一更新工单状态（按角色做数据隔离）
     */
    @PostMapping("/work-orders/status")
    @Operation(summary = "统一更新工单状态（支持自动识别服务类型）")
    public Result<String> updateWorkOrderStatus(HttpServletRequest request,
                                                @RequestBody Map<String, Object> body) {
        return SafeCall.of(() -> Result.success(
                adminWorkOrderAppService.updateWorkOrderStatus(RequestIdentity.adminId(request), body)),
                "更新失败，请稍后重试", log);
    }

    /**
     * 获取员工的预约/任务列表（用于团队管理-成员详情）
     */
    @GetMapping("/staff/{adminId}/appointments")
    @Operation(summary = "获取员工的预约任务列表")
    public Result<Map<String, Object>> getStaffAppointments(@PathVariable Long adminId) {
        return SafeCall.of(() -> Result.success(adminWorkOrderAppService.getStaffAppointments(adminId)),
                "获取预约列表失败，请稍后重试", log);
    }

    /**
     * 密码加密测试接口（用于迁移时加密密码）
     */
    @GetMapping("/test/encrypt")
    @Operation(summary = "密码加密测试接口")
    public Result<String> encryptPassword(@RequestParam String password) {
        return SafeCall.of(() -> Result.success(adminAuthAppService.encryptPassword(password)),
                "加密失败，请稍后重试", log);
    }
}
