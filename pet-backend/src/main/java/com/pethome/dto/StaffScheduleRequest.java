package com.pethome.dto;

import lombok.Data;

/**
 * 员工排班请求对象
 */
@Data
public class StaffScheduleRequest {

    /**
     * 排班ID（更新时使用）
     */
    private Long id;

    /**
     * 员工ID
     */
    private Long adminId;

    /**
     * 部门ID
     */
    private Long departmentId;

    /**
     * 排班日期（yyyy-MM-dd）
     */
    private String date;

    /**
     * 开始时间（HH:mm:ss）
     */
    private String startTime;

    /**
     * 结束时间（HH:mm:ss）
     */
    private String endTime;

    /**
     * 可接单容量
     */
    private Integer capacity;
}

