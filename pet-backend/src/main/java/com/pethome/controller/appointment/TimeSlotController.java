package com.pethome.controller.appointment;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/time-slots")
@Tag(name = "时间段管理")
public class TimeSlotController {

    private static final Logger log = LoggerFactory.getLogger(TimeSlotController.class);

    @Autowired
    private TimeSlotService timeSlotService;

    /** 时间段属门店运营配置，仅 staff/admin 可维护（只读接口仍按公开白名单放行） */
    private boolean denyIfNotAdminOrStaff(HttpServletRequest request, Map<String, Object> result) {
        if (RequestIdentity.isAdminOrStaff(request)) {
            return false;
        }
        result.put("code", 403);
        result.put("msg", "无权操作时间段配置");
        return true;
    }

    @GetMapping("/list")
    @Operation(summary = "获取时间段列表")
    public Map<String, Object> getTimeSlotList(@RequestParam String serviceType,
                                               @RequestParam(required = false) Long storeId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            QueryWrapper<TimeSlot> wrapper = new QueryWrapper<>();
            wrapper.eq("service_type", serviceType);
            if (storeId != null) {
                wrapper.eq("store_id", storeId);
            } else {
                wrapper.isNull("store_id");
            }
            wrapper.orderByAsc("time_slot");
            
            List<TimeSlot> timeSlots = timeSlotService.list(wrapper);
            
            result.put("code", 0);
            result.put("msg", "success");
            result.put("data", timeSlots);
        } catch (Exception e) {
            log.error("查询失败", e);
            result.put("code", -1);
            result.put("msg", "查询失败，请稍后重试");
        }
        
        return result;
    }

    @PostMapping("/create")
    @Operation(summary = "创建时间段")
    public Map<String, Object> createTimeSlot(@RequestBody TimeSlot timeSlot,
                                              HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        if (denyIfNotAdminOrStaff(request, result)) {
            return result;
        }
        
        try {
            // 检查是否已存在
            QueryWrapper<TimeSlot> wrapper = new QueryWrapper<>();
            wrapper.eq("service_type", timeSlot.getServiceType());
            wrapper.eq("time_slot", timeSlot.getTimeSlot());
            if (timeSlot.getStoreId() != null) {
                wrapper.eq("store_id", timeSlot.getStoreId());
            } else {
                wrapper.isNull("store_id");
            }
            
            if (timeSlotService.count(wrapper) > 0) {
                result.put("code", -1);
                result.put("msg", "该时间段已存在");
                return result;
            }
            
            boolean success = timeSlotService.save(timeSlot);
            
            if (success) {
                result.put("code", 0);
                result.put("msg", "添加成功");
                result.put("data", timeSlot);
            } else {
                result.put("code", -1);
                result.put("msg", "添加失败");
            }
        } catch (Exception e) {
            result.put("code", -1);
            result.put("msg", "添加失败，请稍后重试");
            log.error("添加时间段失败", e);
        }
        
        return result;
    }

    @PostMapping("/update")
    @Operation(summary = "更新时间段")
    public Map<String, Object> updateTimeSlot(@RequestBody TimeSlot timeSlot,
                                             HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        if (denyIfNotAdminOrStaff(request, result)) {
            return result;
        }
        
        try {
            boolean success = timeSlotService.updateById(timeSlot);
            
            if (success) {
                result.put("code", 0);
                result.put("msg", "更新成功");
                result.put("data", timeSlot);
            } else {
                result.put("code", -1);
                result.put("msg", "更新失败");
            }
        } catch (Exception e) {
            log.error("更新失败", e);
            result.put("code", -1);
            result.put("msg", "更新失败，请稍后重试");
        }
        
        return result;
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新时间段状态")
    public Map<String, Object> updateTimeSlotStatus(
            @PathVariable Long id,
            @RequestParam Boolean isActive,
            HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        if (denyIfNotAdminOrStaff(request, result)) {
            return result;
        }
        
        try {
            TimeSlot timeSlot = timeSlotService.getById(id);
            if (timeSlot == null) {
                result.put("code", -1);
                result.put("msg", "时间段不存在");
                return result;
            }
            
            timeSlot.setIsActive(isActive);
            boolean success = timeSlotService.updateById(timeSlot);
            
            if (success) {
                result.put("code", 0);
                result.put("msg", "更新成功");
            } else {
                result.put("code", -1);
                result.put("msg", "更新失败");
            }
        } catch (Exception e) {
            log.error("更新失败", e);
            result.put("code", -1);
            result.put("msg", "更新失败，请稍后重试");
        }
        
        return result;
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除时间段")
    public Map<String, Object> deleteTimeSlot(@PathVariable Long id, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        if (denyIfNotAdminOrStaff(request, result)) {
            return result;
        }
        
        try {
            boolean success = timeSlotService.removeById(id);
            
            if (success) {
                result.put("code", 0);
                result.put("msg", "删除成功");
            } else {
                result.put("code", -1);
                result.put("msg", "删除失败");
            }
        } catch (Exception e) {
            log.error("删除失败", e);
            result.put("code", -1);
            result.put("msg", "删除失败，请稍后重试");
        }
        
        return result;
    }
}

