package com.pethome.controller.appointment;


import com.pethome.common.BusinessException;
import com.pethome.common.SafeCall;
import com.pethome.common.Result;
import com.pethome.entity.Admin;
import com.pethome.entity.ServiceMember;
import com.pethome.service.admin.AdminService;
import com.pethome.service.appointment.ServiceMemberService;
import com.pethome.security.RequestIdentity;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 服务人员管理Controller
 */
@RestController
@RequestMapping("/api/service-member")
@Tag(name = "服务人员管理")
public class ServiceMemberController {

    private static final Logger log = LoggerFactory.getLogger(ServiceMemberController.class);

    @Autowired
    private ServiceMemberService memberService;
    @Autowired
    private AdminService adminService;

    @GetMapping("/list")
    @Operation(summary = "根据服务类型查询启用的服务人员；可选 storeId 按门店过滤（仅返回该门店或平台级成员）")
    public Result<List<ServiceMember>> getMembersByServiceType(
            @RequestParam String serviceType,
            @RequestParam(required = false) Long storeId,
            HttpServletRequest request) {
        return SafeCall.of(() -> {
            List<ServiceMember> members = storeId == null
                    ? memberService.getMembersByServiceType(serviceType)
                    : memberService.getMembersByServiceTypeAndStore(serviceType, storeId);
            // 安全修复：该接口在公开白名单中（匿名可读），此前会返回全部员工手机号。
            // 对 C 端与匿名访问统一脱敏，仅保留展示所需的姓名/头像等信息。
            if (!RequestIdentity.isAdminOrStaff(request)) {
                maskSensitive(members);
            }
            return Result.success(members);
        }, "查询服务人员列表失败，请稍后重试", log);
    }

    @GetMapping("/all")
    @Operation(summary = "获取所有服务人员（不区分服务类型）")
    public Result<List<ServiceMember>> getAllMembers(HttpServletRequest request) {
        // 安全修复：全量员工主数据（含手机号、账号绑定）仅限管理端使用，C 端用户无需此接口
        if (!RequestIdentity.isAdminOrStaff(request)) {
            throw new BusinessException(403, "无权查询服务人员列表");
        }
        return SafeCall.of(() -> {
            List<ServiceMember> members = memberService.getAllMembers();
            return Result.success(members);
        }, "查询所有服务人员失败，请稍后重试", log);
    }

    @PostMapping("/auto-assign")
    @Operation(summary = "自动分配服务人员；可选 storeId 时仅从该门店或平台级成员中分配")
    public Result<ServiceMember> autoAssignMember(
            @RequestParam String serviceType,
            @RequestParam String date,
            @RequestParam String timeSlot,
            @RequestParam(required = false) Long storeId,
            HttpServletRequest request) {
        // 安全修复：自动分配属门店运营操作，仅限 staff/admin，且分店账号只能在自己门店范围内分配
        if (!RequestIdentity.isAdminOrStaff(request)) {
            throw new BusinessException(403, "无权自动分配服务人员");
        }
        Long requestedStoreId = storeId;
        return SafeCall.of(() -> {
            Long scopedStoreId = scopedStoreId(request);
            Long effectiveStoreId = scopedStoreId != null ? scopedStoreId : requestedStoreId;
            LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ISO_DATE);
            ServiceMember member = memberService.autoAssignMember(serviceType, localDate, timeSlot, effectiveStoreId);
            if (member == null) {
                throw new BusinessException("没有可用的服务人员");
            }
            return Result.success(member);
        }, "自动分配服务人员失败，请稍后重试", log);
    }

    @GetMapping("/workload/{memberId}")
    @Operation(summary = "获取服务人员在指定日期的工作量")
    public Result<Integer> getMemberWorkload(
            @PathVariable Long memberId,
            @RequestParam String date,
            HttpServletRequest request) {
        // 安全修复：工作量属运营数据，仅限 staff/admin，且分店账号不得查看他店员工
        if (!RequestIdentity.isAdminOrStaff(request)) {
            throw new BusinessException(403, "无权查询服务人员工作量");
        }
        Long scope = scopedStoreId(request);
        if (scope != null) {
            ServiceMember member = memberService.getMemberById(memberId);
            if (member == null) {
                throw new BusinessException(404, "服务人员不存在");
            }
            if (!scope.equals(member.getStoreId())) {
                throw new BusinessException(403, "无权查询其他门店的服务人员");
            }
        }
        return SafeCall.of(() -> {
            LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ISO_DATE);
            int workload = memberService.getMemberWorkload(memberId, localDate);
            return Result.success(workload);
        }, "查询工作量失败，请稍后重试", log);
    }

    @PostMapping("/create")
    @Operation(summary = "创建服务人员")
    public Result<ServiceMember> createMember(@RequestBody ServiceMember member, HttpServletRequest request) {
        // 安全修复：员工主数据属门店运营数据，任意登录用户此前可直接创建/覆盖
        if (!RequestIdentity.isAdmin(request)) {
            throw new BusinessException(403, "无权创建服务人员");
        }
        Long scope = scopedStoreId(request);
        if (scope != null && (member.getStoreId() == null || !scope.equals(member.getStoreId()))) {
            throw new BusinessException(403, "无权在其他门店创建服务人员");
        }
        return SafeCall.of(() -> {
            ServiceMember created = memberService.createMember(member);
            return Result.success(created);
        }, "创建服务人员失败，请稍后重试", log);
    }

    @PutMapping("/update")
    @Operation(summary = "更新服务人员信息")
    public Result<Boolean> updateMember(@RequestBody ServiceMember member, HttpServletRequest request) {
        // 安全修复：先校验原记录与目标门店归属，防止改写他店员工（含手机号/账号绑定）
        if (!RequestIdentity.isAdmin(request)) {
            throw new BusinessException(403, "无权更新服务人员");
        }
        if (member.getId() == null) {
            throw new BusinessException("服务人员ID不能为空");
        }
        ServiceMember existing = memberService.getMemberById(member.getId());
        if (existing == null) {
            throw new BusinessException("服务人员不存在");
        }
        Long scope = scopedStoreId(request);
        if (scope != null && (!scope.equals(existing.getStoreId())
                || (member.getStoreId() != null && !scope.equals(member.getStoreId()))) ) {
            throw new BusinessException(403, "无权更新其他门店的服务人员");
        }
        return SafeCall.of(() -> {
            boolean success = memberService.updateMember(member);
            if (!success) {
                throw new BusinessException("更新失败");
            }
            return Result.success(true);
        }, "更新服务人员信息失败，请稍后重试", log);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询服务人员")
    public Result<ServiceMember> getMemberById(@PathVariable Long id, HttpServletRequest request) {
        return SafeCall.of(() -> {
            ServiceMember member = memberService.getMemberById(id);
            if (member != null) {
                // 安全修复：C 端用户只应看到展示信息，不返回员工手机号/账号绑定
                if (!RequestIdentity.isAdminOrStaff(request)) {
                    member.setPhone(null);
                    member.setUserId(null);
                }
                return Result.success(member);
            }
            throw new BusinessException("服务人员不存在");
        }, "查询服务人员失败，请稍后重试", log);
    }

    @GetMapping("/by-user/{userId}")
    @Operation(summary = "根据用户ID查询服务人员")
    public Result<ServiceMember> getMemberByUserId(@PathVariable Long userId, HttpServletRequest request) {
        // 安全修复：该接口会返回员工手机号并触发 admin→member 同步写库，
        // 仅允许平台管理员、本人、或同门店店长查询，防止按 userId 枚举他人员工资料
        Long currentAdminId = RequestIdentity.adminId(request);
        boolean self = currentAdminId != null && currentAdminId.equals(userId);
        Long scope = scopedStoreId(request);
        if (!RequestIdentity.isPlatformAdmin(request) && !self) {
            ServiceMember target = memberService.getMemberByUserId(userId);
            if (scope == null || target == null || target.getStoreId() == null
                    || !scope.equals(target.getStoreId())) {
                throw new BusinessException(403, "无权查询该服务人员");
            }
        }
        return SafeCall.of(() -> {
            ServiceMember member = memberService.getMemberByUserId(userId);
            if (member != null) {
                return Result.success(member);
            }

            // 若未找到映射，尝试从 admin 账号自动同步（平台管理员给分店管理员排班时依赖此映射）
            if (!RequestIdentity.isPlatformAdmin(request)) {
                throw new BusinessException("未找到对应的服务人员");
            }
            Admin admin = adminService != null ? adminService.getById(userId) : null;
            if (admin != null) {
                String role = admin.getRole() == null ? "" : admin.getRole().trim().toLowerCase();
                if ("staff".equals(role) || "store_admin".equals(role) || "store-admin".equals(role)) {
                    ServiceMember synced = memberService.syncMemberWithAdmin(admin);
                    if (synced != null) {
                        return Result.success(synced);
                    }
                }
            }
            throw new BusinessException("未找到对应的服务人员");
        }, "查询服务人员失败，请稍后重试", log);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除服务人员")
    public Result<Boolean> deleteMember(@PathVariable Long id, HttpServletRequest request) {
        // 安全修复：删除员工仅限管理员，且店长只能删除本门店员工
        if (!RequestIdentity.isAdmin(request)) {
            throw new BusinessException(403, "无权删除服务人员");
        }
        ServiceMember existing = memberService.getMemberById(id);
        if (existing == null) {
            throw new BusinessException("服务人员不存在");
        }
        Long scope = scopedStoreId(request);
        if (scope != null && !scope.equals(existing.getStoreId())) {
            throw new BusinessException(403, "无权删除其他门店的服务人员");
        }
        return SafeCall.of(() -> {
            boolean success = memberService.deleteMember(id);
            if (!success) {
                throw new BusinessException("删除失败");
            }
            return Result.success(true);
        }, "删除服务人员失败，请稍后重试", log);
    }

    /**
     * 门店维度：平台管理员返回 null（不限门店）；
     * 店长/店员返回其服务门店，无门店归属返回 -1（拒绝一切跨店操作）。
     */
    private Long scopedStoreId(HttpServletRequest request) {
        if (RequestIdentity.isPlatformAdmin(request)) {
            return null;
        }
        Long storeId = RequestIdentity.serviceStoreId(request);
        return storeId != null ? storeId : -1L;
    }

    /**
     * 脱敏：C 端与匿名访问不返回员工手机号与账号绑定
     */
    private void maskSensitive(List<ServiceMember> members) {
        if (members == null) {
            return;
        }
        for (ServiceMember member : members) {
            if (member != null) {
                member.setPhone(null);
                member.setUserId(null);
            }
        }
    }
}
