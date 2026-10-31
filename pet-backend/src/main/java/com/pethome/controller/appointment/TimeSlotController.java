package com.pethome.controller.appointment;

import com.pethome.common.BusinessException;
import com.pethome.common.Result;
import com.pethome.common.SafeCall;
import com.pethome.entity.TimeSlot;
import com.pethome.security.RequestIdentity;
import com.pethome.service.appointment.TimeSlotService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/time-slots")
@Tag(name = "时间段管理")
public class TimeSlotController {

    private static final Logger log = LoggerFactory.getLogger(TimeSlotController.class);

    @Autowired
    private TimeSlotService timeSlotService;

    /** 时间段属门店运营配置，仅 staff/admin 可维护（只读接口仍按公开白名单放行） */
    private void requireAdminOrStaff(HttpServletRequest request) {
        if (!RequestIdentity.isAdminOrStaff(request)) {
            throw new BusinessException(403, "无权操作时间段配置");
        }
    }

    /** 门店归属：平台管理员不限门店；店长/店员只能维护本门店（无门店归属视为越权） */
    private void requireSameStore(HttpServletRequest request, Long storeId) {
        if (RequestIdentity.isPlatformAdmin(request)) {
            return;
        }
        Long scope = RequestIdentity.serviceStoreId(request);
        if (scope != null && storeId != null && scope.equals(storeId)) {
            return;
        }
        throw new BusinessException(403, "无权操作其他门店的时间段配置");
    }

    @GetMapping("/list")
    @Operation(summary = "获取时间段列表")
    public Result<List<TimeSlot>> getTimeSlotList(@RequestParam String serviceType,
                                                  @RequestParam(required = false) Long storeId) {
        return SafeCall.of(() -> Result.success(timeSlotService.listTimeSlots(serviceType, storeId)),
                "查询失败，请稍后重试", log);
    }

    @PostMapping("/create")
    @Operation(summary = "创建时间段")
    public Result<TimeSlot> createTimeSlot(@RequestBody TimeSlot timeSlot,
                                           HttpServletRequest request) {
        requireAdminOrStaff(request);
        requireSameStore(request, timeSlot.getStoreId());
        return SafeCall.of(() -> Result.success(timeSlotService.createTimeSlot(timeSlot)),
                "添加失败，请稍后重试", log);
    }

    @PostMapping("/update")
    @Operation(summary = "更新时间段")
    public Result<TimeSlot> updateTimeSlot(@RequestBody TimeSlot timeSlot,
                                           HttpServletRequest request) {
        requireAdminOrStaff(request);
        TimeSlot existing = timeSlotService.getById(timeSlot.getId());
        if (existing == null) {
            throw new BusinessException("时间段不存在");
        }
        // 安全修复：店长/店员不得修改其他门店（含把本店记录改到他店）的时间段
        requireSameStore(request, existing.getStoreId());
        requireSameStore(request, timeSlot.getStoreId());
        return SafeCall.of(() -> Result.success(timeSlotService.updateTimeSlot(timeSlot)),
                "更新失败，请稍后重试", log);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新时间段状态")
    public Result<Boolean> updateTimeSlotStatus(
            @PathVariable Long id,
            @RequestParam Boolean isActive,
            HttpServletRequest request) {
        requireAdminOrStaff(request);
        TimeSlot timeSlot = timeSlotService.getById(id);
        if (timeSlot == null) {
            throw new BusinessException("时间段不存在");
        }
        // 安全修复：店长/店员不得修改其他门店时间段的状态
        requireSameStore(request, timeSlot.getStoreId());
        return SafeCall.of(() -> Result.success(timeSlotService.updateTimeSlotStatus(timeSlot, isActive)),
                "更新失败，请稍后重试", log);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除时间段")
    public Result<Boolean> deleteTimeSlot(@PathVariable Long id, HttpServletRequest request) {
        requireAdminOrStaff(request);
        TimeSlot existing = timeSlotService.getById(id);
        if (existing == null) {
            throw new BusinessException("时间段不存在");
        }
        // 安全修复：店长/店员不得删除其他门店的时间段
        requireSameStore(request, existing.getStoreId());
        return SafeCall.of(() -> Result.success(timeSlotService.deleteTimeSlot(id)),
                "删除失败，请稍后重试", log);
    }
}
