package com.pethome.controller;

import com.pethome.common.Result;
import com.pethome.entity.Admin;
import com.pethome.entity.ServiceMember;
import com.pethome.service.AdminService;
import com.pethome.service.ServiceMemberService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 服务人员管理Controller
 */
@RestController
@RequestMapping("/api/service-member")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
@Api(tags = "服务人员管理")
public class ServiceMemberController {

    @Autowired
    private ServiceMemberService memberService;
    @Autowired
    private AdminService adminService;

    @GetMapping("/list")
    @ApiOperation("根据服务类型查询启用的服务人员；可选 storeId 按门店过滤（仅返回该门店或平台级成员）")
    public Result<List<ServiceMember>> getMembersByServiceType(
            @RequestParam String serviceType,
            @RequestParam(required = false) Long storeId) {
        try {
            List<ServiceMember> members = storeId == null
                    ? memberService.getMembersByServiceType(serviceType)
                    : memberService.getMembersByServiceTypeAndStore(serviceType, storeId);
            return Result.success(members);
        } catch (Exception e) {
            return Result.error("查询服务人员列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/all")
    @ApiOperation("获取所有服务人员（不区分服务类型）")
    public Result<List<ServiceMember>> getAllMembers() {
        try {
            List<ServiceMember> members = memberService.getAllMembers();
            return Result.success(members);
        } catch (Exception e) {
            return Result.error("查询所有服务人员失败: " + e.getMessage());
        }
    }

    @PostMapping("/auto-assign")
    @ApiOperation("自动分配服务人员；可选 storeId 时仅从该门店或平台级成员中分配")
    public Result<ServiceMember> autoAssignMember(
            @RequestParam String serviceType,
            @RequestParam String date,
            @RequestParam String timeSlot,
            @RequestParam(required = false) Long storeId) {
        try {
            LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ISO_DATE);
            ServiceMember member = memberService.autoAssignMember(serviceType, localDate, timeSlot, storeId);
            if (member != null) {
                return Result.success(member);
            } else {
                return Result.error("没有可用的服务人员");
            }
        } catch (Exception e) {
            return Result.error("自动分配服务人员失败: " + e.getMessage());
        }
    }

    @GetMapping("/workload/{memberId}")
    @ApiOperation("获取服务人员在指定日期的工作量")
    public Result<Integer> getMemberWorkload(
            @PathVariable Long memberId,
            @RequestParam String date) {
        try {
            LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ISO_DATE);
            int workload = memberService.getMemberWorkload(memberId, localDate);
            return Result.success(workload);
        } catch (Exception e) {
            return Result.error("查询工作量失败: " + e.getMessage());
        }
    }

    @PostMapping("/create")
    @ApiOperation("创建服务人员")
    public Result<ServiceMember> createMember(@RequestBody ServiceMember member) {
        try {
            ServiceMember created = memberService.createMember(member);
            return Result.success(created);
        } catch (Exception e) {
            return Result.error("创建服务人员失败: " + e.getMessage());
        }
    }

    @PutMapping("/update")
    @ApiOperation("更新服务人员信息")
    public Result<Boolean> updateMember(@RequestBody ServiceMember member) {
        try {
            boolean success = memberService.updateMember(member);
            return success ? Result.success(true) : Result.error("更新失败");
        } catch (Exception e) {
            return Result.error("更新服务人员信息失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @ApiOperation("根据ID查询服务人员")
    public Result<ServiceMember> getMemberById(@PathVariable Long id) {
        try {
            ServiceMember member = memberService.getMemberById(id);
            if (member != null) {
                return Result.success(member);
            } else {
                return Result.error("服务人员不存在");
            }
        } catch (Exception e) {
            return Result.error("查询服务人员失败: " + e.getMessage());
        }
    }

    @GetMapping("/by-user/{userId}")
    @ApiOperation("根据用户ID查询服务人员")
    public Result<ServiceMember> getMemberByUserId(@PathVariable Long userId) {
        try {
            ServiceMember member = memberService.getMemberByUserId(userId);
            if (member != null) {
                return Result.success(member);
            }

            // 若未找到映射，尝试从 admin 账号自动同步（平台管理员给分店管理员排班时依赖此映射）
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
            return Result.error("未找到对应的服务人员");
        } catch (Exception e) {
            return Result.error("查询服务人员失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除服务人员")
    public Result<Boolean> deleteMember(@PathVariable Long id) {
        try {
            boolean success = memberService.deleteMember(id);
            return success ? Result.success(true) : Result.error("删除失败");
        } catch (Exception e) {
            return Result.error("删除服务人员失败: " + e.getMessage());
        }
    }
}

