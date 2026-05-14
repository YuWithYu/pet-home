package com.pethome.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pethome.entity.TimeSlot;
import com.pethome.service.TimeSlotService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/time-slots")
@Api(tags = "时间段管理")
public class TimeSlotController {

    @Autowired
    private TimeSlotService timeSlotService;

    @GetMapping("/list")
    @ApiOperation("获取时间段列表")
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
            result.put("code", -1);
            result.put("msg", "查询失败: " + e.getMessage());
        }
        
        return result;
    }

    @PostMapping("/create")
    @ApiOperation("创建时间段")
    public Map<String, Object> createTimeSlot(@RequestBody TimeSlot timeSlot) {
        Map<String, Object> result = new HashMap<>();
        
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
            result.put("msg", "添加失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }

    @PostMapping("/update")
    @ApiOperation("更新时间段")
    public Map<String, Object> updateTimeSlot(@RequestBody TimeSlot timeSlot) {
        Map<String, Object> result = new HashMap<>();
        
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
            result.put("code", -1);
            result.put("msg", "更新失败: " + e.getMessage());
        }
        
        return result;
    }

    @PutMapping("/{id}/status")
    @ApiOperation("更新时间段状态")
    public Map<String, Object> updateTimeSlotStatus(
            @PathVariable Long id,
            @RequestParam Boolean isActive) {
        Map<String, Object> result = new HashMap<>();
        
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
            result.put("code", -1);
            result.put("msg", "更新失败: " + e.getMessage());
        }
        
        return result;
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除时间段")
    public Map<String, Object> deleteTimeSlot(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        
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
            result.put("code", -1);
            result.put("msg", "删除失败: " + e.getMessage());
        }
        
        return result;
    }
}

