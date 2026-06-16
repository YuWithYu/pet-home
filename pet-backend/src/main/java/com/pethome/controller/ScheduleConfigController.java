package com.pethome.controller;

import com.pethome.common.Result;
import com.pethome.service.ServiceScheduleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 排班配置：批量生成排班（前端「批量生成排班」调用）
 */
@RestController
@RequestMapping("/api/schedule-config")
@Api(tags = "排班配置")
public class ScheduleConfigController {

    @Autowired
    private ServiceScheduleService scheduleService;

    @PostMapping("/generate-batch")
    @ApiOperation("批量生成排班")
    public Result<Integer> generateBatch(
            @RequestParam String serviceType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "1") Integer maxCapacity,
            @RequestParam List<Long> memberIds,
            @RequestParam List<String> timeSlots,
            @RequestParam(required = false) Long storeId) {
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
}
