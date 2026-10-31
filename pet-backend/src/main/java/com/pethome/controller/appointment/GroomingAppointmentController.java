package com.pethome.controller.appointment;


import com.pethome.common.SafeCall;
import com.pethome.common.BusinessException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.common.Result;
import com.pethome.entity.GroomingAppointment;
import com.pethome.service.appointment.GroomingAppointmentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/grooming-appointments")
@Tag(name = "宠物洗护预约管理")
public class GroomingAppointmentController {

    private static final Logger log = LoggerFactory.getLogger(GroomingAppointmentController.class);

    @Autowired
    private GroomingAppointmentService groomingAppointmentService;

    @Autowired
    private com.pethome.service.admin.AdminService adminService;

    @Autowired
    private com.pethome.service.appointment.ServiceMemberService serviceMemberService;

    @GetMapping("/page")
    @Operation(summary = "分页查询宠物洗护预约")
    public Result<IPage<GroomingAppointment>> getGroomingAppointmentPage(
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "状态") @RequestParam(required = false) String status,
            @Parameter(description = "关键字") @RequestParam(required = false) String keyword,
            @Parameter(description = "服务类型") @RequestParam(required = false) String serviceType,
            @Parameter(description = "门店ID") @RequestParam(required = false) Long storeId,
            HttpServletRequest request) {
        // 安全修复：分页结果含客户姓名、手机号与地址，属门店运营数据；
        // 此前任意登录用户（含 C 端 user）都可调用并按 storeId 读取全平台预约
        if (!isStaffOrAdmin(request)) {
            return Result.error(403, "无权查看洗护预约列表");
        }

        Long adminId = (Long) request.getAttribute("adminId");
        if (adminId != null && !"admin".equalsIgnoreCase((String) request.getAttribute("role"))) {
            com.pethome.entity.Admin admin = adminService.getById(adminId);
            if (admin != null) {
                Long effectiveStoreId = admin.getServiceStoreId();
                if (effectiveStoreId == null && serviceMemberService != null) {
                    com.pethome.entity.ServiceMember sm = serviceMemberService.getMemberByUserId(adminId);
                    if (sm != null && sm.getStoreId() != null) {
                        effectiveStoreId = sm.getStoreId();
                    }
                }
                if (effectiveStoreId != null) {
                    storeId = effectiveStoreId;
                } else {
                    storeId = -1L;
                }
            }
        }

        Page<GroomingAppointment> page = new Page<>(current, size);
        IPage<GroomingAppointment> result = groomingAppointmentService.getAppointmentPage(page, status, keyword, serviceType, storeId);
        return Result.success(result);
    }

    @GetMapping("/user/list/{userId}")
    @Operation(summary = "获取用户宠物洗护预约列表")
    public Result<List<GroomingAppointment>> getUserGroomingAppointments(@PathVariable Long userId, HttpServletRequest request) {
        // 安全修复：归属校验——userId 必须以登录态为准（拦截器写入的 userId attribute），
        // 路径参数仅兼容旧前端；未登录或与登录身份不一致时视为越权，返回空列表。
        Object attr = request.getAttribute("userId");
        Long currentUserId = attr instanceof Number ? ((Number) attr).longValue() : null;
        if (currentUserId == null || !currentUserId.equals(userId)) {
            return Result.success(java.util.Collections.emptyList());
        }
        List<GroomingAppointment> appointments = groomingAppointmentService.getAppointmentsByUserId(userId);
        return Result.success(appointments);
    }

    @GetMapping("/member/list")
    @Operation(summary = "获取服务人员的洗护预约列表")
    public Result<List<GroomingAppointment>> getMemberGroomingAppointments(@RequestParam Long memberId,
                                                                         HttpServletRequest request) {
        // 安全修复：服务人员维度查询会返回客户手机号/地址等敏感信息，仅限 staff/admin
        if (!isStaffOrAdmin(request)) {
            return Result.error(403, "无权查询服务人员预约列表");
        }
        // 安全修复：分店账号不得查看其他门店服务人员的预约
        Long scope = scopedStoreId(request);
        if (scope != null) {
            com.pethome.entity.ServiceMember member = serviceMemberService.getMemberById(memberId);
            if (member == null) {
                return Result.error(404, "服务人员不存在");
            }
            if (!scope.equals(member.getStoreId())) {
                log.warn("[GroomingAppointment] member/list 跨门店拒绝: scope={}, memberStore={}", scope, member.getStoreId());
                return Result.error(403, "无权查询其他门店的服务人员预约");
            }
        }
        List<GroomingAppointment> appointments = groomingAppointmentService.getAppointmentsByMemberId(memberId);
        return Result.success(appointments);
    }

    @PostMapping("/create")
    @Operation(summary = "创建宠物洗护预约")
    public Result<GroomingAppointment> createGroomingAppointment(@RequestBody GroomingAppointment appointment,
                                                                 HttpServletRequest request) {
        return SafeCall.of(() -> {
            // 安全修复：归属强制以登录态为准（拦截器解析 token 后写入的 userId attribute），
            // body 中的 userId 一律覆盖，杜绝伪造他人归属创建预约
            Long currentUserId = com.pethome.security.RequestIdentity.userId(request);
            if (currentUserId == null) {
                throw new BusinessException(401, "未登录或登录已过期");
            }
            appointment.setUserId(currentUserId);
            if (!groomingAppointmentService.createGroomingAppointment(appointment)) {
                throw new BusinessException("预约创建失败");
            }
            return Result.success(appointment);
        }, "预约创建失败，请稍后重试", log);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取宠物洗护预约详情")
    public Result<GroomingAppointment> getGroomingAppointmentDetail(@PathVariable Long id,
                                                                    HttpServletRequest request) {
        GroomingAppointment appointment = groomingAppointmentService.getAppointmentById(id);
        if (appointment == null) {
            return Result.error("预约不存在");
        }
        // 安全修复：详情含手机号/地址等个人信息——staff/admin 仅限本门店，普通用户仅能查看本人预约
        if (isStaffOrAdmin(request)) {
            if (!canAccessStore(request, appointment.getStoreId())) {
                return Result.error(403, "无权查看此预约");
            }
        } else {
            Object attr = request.getAttribute("userId");
            Long currentUserId = attr instanceof Number ? ((Number) attr).longValue() : null;
            if (currentUserId == null || !currentUserId.equals(appointment.getUserId())) {
                return Result.error(403, "无权查看此预约");
            }
        }
        return Result.success(appointment);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新宠物洗护预约状态（拒绝时可传 rejectReason，用户端会展示）")
    public Result<String> updateGroomingAppointmentStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) String rejectReason,
            @RequestParam(required = false, defaultValue = "false") boolean forceCancelAfterServiceStart,
            HttpServletRequest request) {
        return SafeCall.of(() -> {
            // 安全修复：状态接口越权防护——
            // 普通用户仅允许取消自己的预约（status=cancelled 且归属本人）；
            // confirmed/rejected/completed 等运营态变更仅限 staff/admin，防止用户绕过审核自审自发
            String denial = checkStatusPermission(id, status, request);
            if (denial != null) {
                throw new BusinessException(403, denial);
            }
            String reason = (rejectReason != null && !rejectReason.trim().isEmpty()) ? rejectReason.trim() : null;
            if (!groomingAppointmentService.updateAppointmentStatus(id, status, reason, forceCancelAfterServiceStart)) {
                throw new BusinessException("状态更新失败");
            }
            return Result.success("状态更新成功");
        }, "状态更新失败，请稍后重试", log);
    }

    /** staff/admin 判定：RoleInterceptor 在管理端登录后写入 adminId attribute */
    private boolean isStaffOrAdmin(HttpServletRequest request) {
        return request.getAttribute("adminId") instanceof Number;
    }

    /**
     * 当前管理端账号的可见门店：平台管理员返回 null（不限门店），
     * 店长/店员返回其服务门店（无门店归属返回 -1，保证查不到任何数据）。
     */
    private Long scopedStoreId(HttpServletRequest request) {
        if (!isStaffOrAdmin(request)) {
            return null;
        }
        Object roleAttr = request.getAttribute("role");
        if (roleAttr != null && "admin".equalsIgnoreCase(String.valueOf(roleAttr))) {
            return null;
        }
        Object storeAttr = request.getAttribute("serviceStoreId");
        if (storeAttr instanceof Number) {
            return ((Number) storeAttr).longValue();
        }
        Object adminIdAttr = request.getAttribute("adminId");
        if (adminIdAttr instanceof Number) {
            com.pethome.entity.ServiceMember sm =
                    serviceMemberService.getMemberByUserId(((Number) adminIdAttr).longValue());
            if (sm != null && sm.getStoreId() != null) {
                return sm.getStoreId();
            }
        }
        return -1L;
    }

    /** 门店归属校验：返回 true 表示可访问 */
    private boolean canAccessStore(HttpServletRequest request, Long storeId) {
        Long scope = scopedStoreId(request);
        return scope == null || (storeId != null && scope.equals(storeId));
    }

    /**
     * 预约状态变更权限校验。
     * @return null 表示放行；非 null 为拒绝原因
     */
    private String checkStatusPermission(Long id, String status, HttpServletRequest request) {
        if (isStaffOrAdmin(request)) {
            // 安全修复：分店账号只能操作本门店的预约
            GroomingAppointment appointment = groomingAppointmentService.getAppointmentById(id);
            if (appointment == null) {
                return "预约不存在";
            }
            if (!canAccessStore(request, appointment.getStoreId())) {
                return "无权操作此预约";
            }
            return null;
        }
        // 普通用户：仅允许取消本人预约
        if (!"cancelled".equals(status)) {
            return "无权变更预约状态";
        }
        Object attr = request.getAttribute("userId");
        Long currentUserId = attr instanceof Number ? ((Number) attr).longValue() : null;
        if (currentUserId == null) {
            return "未登录或登录已过期";
        }
        GroomingAppointment appointment = groomingAppointmentService.getAppointmentById(id);
        if (appointment == null || !currentUserId.equals(appointment.getUserId())) {
            return "无权操作此预约";
        }
        return null;
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新宠物洗护预约")
    public Result<GroomingAppointment> updateGroomingAppointment(@PathVariable Long id, @RequestBody GroomingAppointment appointment,
                                                                   HttpServletRequest request) {
        return SafeCall.of(() -> {
            // 安全修复：全字段更新仅限 staff/admin（普通用户可借此改价格/归属等敏感字段）
            if (!isStaffOrAdmin(request)) {
                throw new BusinessException(403, "无权修改此预约");
            }
            GroomingAppointment existing = groomingAppointmentService.getAppointmentById(id);
            if (existing == null) {
                throw new BusinessException(404, "预约不存在");
            }
            // 安全修复：分店账号不得跨门店修改
            if (!canAccessStore(request, existing.getStoreId())) {
                throw new BusinessException(403, "无权修改此预约");
            }
            Long scope = scopedStoreId(request);
            if (scope != null && appointment.getStoreId() != null && !scope.equals(appointment.getStoreId())) {
                throw new BusinessException(403, "无权将预约转移到其他门店");
            }
            appointment.setId(id);
            // 安全修复：归属用户以库中记录为准，禁止通过更新接口改派/伪造客户归属
            appointment.setUserId(null);
            if (!groomingAppointmentService.updateById(appointment)) {
                throw new BusinessException("预约更新失败");
            }
            return Result.success("预约更新成功", appointment);
        }, "预约更新失败，请稍后重试", log);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除宠物洗护预约")
    public Result<Boolean> deleteGroomingAppointment(@PathVariable Long id, HttpServletRequest request) {
        return com.pethome.common.SafeCall.of(() -> {
            // 安全修复：物理删除仅限 staff/admin
            if (!isStaffOrAdmin(request)) {
                throw new BusinessException(403, "无权删除此预约");
            }
            GroomingAppointment existing = groomingAppointmentService.getAppointmentById(id);
            if (existing == null) {
                throw new BusinessException(404, "预约不存在");
            }
            // 安全修复：分店账号不得跨门店删除
            if (!canAccessStore(request, existing.getStoreId())) {
                throw new BusinessException(403, "无权删除此预约");
            }
            if (!groomingAppointmentService.removeById(id)) {
                throw new BusinessException("预约删除失败");
            }
            return Result.success("预约删除成功", true);
        }, "预约删除失败，请稍后重试", log);
    }
}

