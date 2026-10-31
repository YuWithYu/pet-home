package com.pethome.controller.appointment;

import com.pethome.common.Result;
import com.pethome.entity.ServiceMember;
import com.pethome.security.RequestIdentity;
import com.pethome.service.appointment.ServiceMemberService;
import com.pethome.service.appointment.ServiceScheduleService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 排班配置：批量生成排班（前端「批量生成排班」调用）
 */
@RestController
@RequestMapping("/api/schedule-config")
@Tag(name = "排班配置")
public class ScheduleConfigController {

    @Autowired
    private ServiceScheduleService scheduleService;

    @Autowired
    private ServiceMemberService serviceMemberService;

    @PostMapping("/generate-batch")
    @Operation(summary = "批量生成排班")
    public Result<Integer> generateBatch(
            @RequestParam String serviceType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "1") Integer maxCapacity,
            @RequestParam List<Long> memberIds,
            @RequestParam List<String> timeSlots,
            @RequestParam(required = false) Long storeId,
            HttpServletRequest request) {
        // 安全修复：此前任意登录用户即可批量生成排班，污染全平台排班数据
        if (!RequestIdentity.isAdminOrStaff(request)) {
            return Result.error(403, "无权生成排班");
        }
        // 安全修复：店长/店员只能为本门店服务人员生成排班，且 storeId 强制归一到本门店，
        // 防止跨门店写入排班（原实现 allow 任意 memberIds + 任意 storeId）
        Long scope = scopedStoreId(request);
        List<Long> targets = memberIds;
        if (scope != null) {
            if (targets == null || targets.isEmpty()) {
                return Result.error("请选择服务人员");
            }
            for (Long memberId : targets) {
                ServiceMember member = memberId == null ? null : serviceMemberService.getMemberById(memberId);
                if (member == null || scope.equals(-1L) || !scope.equals(member.getStoreId())) {
                    return Result.error(403, "无权为其他门店的服务人员生成排班");
                }
            }
            storeId = scope;
        }
        if (serviceType == null || serviceType.trim().isEmpty()) {
            return Result.error("服务类型不能为空");
        }
        if (memberIds == null || memberIds.isEmpty()) {
            return Result.error("请选择服务人员");
        }
        if (timeSlots == null || timeSlots.isEmpty()) {
            return Result.error("请选择时间段");
        }
        if (startDate == null || endDate == null || endDate.isBefore(startDate)) {
            return Result.error("日期范围无效");
        }
        List<String> slots = timeSlots.stream().filter(s -> s != null && !s.trim().isEmpty()).collect(Collectors.toList());
        if (slots.isEmpty()) {
            return Result.error("请选择时间段");
        }
        int count = scheduleService.batchGenerateSchedules(
                serviceType.trim(),
                startDate,
                endDate,
                maxCapacity == null || maxCapacity < 1 ? 1 : maxCapacity,
                memberIds,
                slots,
                storeId);
        return Result.success(count);
    }

    /**
     * 门店维度：平台管理员返回 null（不限门店）；
     * 店长/店员返回其服务门店（serviceStoreId，缺失时回落到员工档案的门店），无归属返回 -1。
     */
    private Long scopedStoreId(HttpServletRequest request) {
        if (RequestIdentity.isPlatformAdmin(request)) {
            return null;
        }
        Long scoped = RequestIdentity.serviceStoreId(request);
        if (scoped != null) {
            return scoped;
        }
        Long adminId = RequestIdentity.adminId(request);
        if (adminId != null && serviceMemberService != null) {
            ServiceMember member = serviceMemberService.getMemberByUserId(adminId);
            if (member != null && member.getStoreId() != null) {
                return member.getStoreId();
            }
        }
        return -1L;
    }
}
