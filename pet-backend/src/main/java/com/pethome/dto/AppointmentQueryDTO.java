package com.pethome.dto;

import java.time.LocalDate;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "预约查询条件")
public class AppointmentQueryDTO {
    
    @ApiModelProperty(value = "用户ID")
    private Integer userId;
    
    @ApiModelProperty(value = "宠物ID")
    private Integer petId;
    
    @ApiModelProperty(value = "服务类型")
    private String serviceType;
    
    @ApiModelProperty(value = "预约日期")
    private LocalDate date;
    
    @ApiModelProperty(value = "时间段")
    private String timeSlot;
    
    @ApiModelProperty(value = "预约状态")
    private String status;
    
    @ApiModelProperty(value = "开始日期")
    private LocalDate startDate;
    
    @ApiModelProperty(value = "结束日期")
    private LocalDate endDate;
    
    @ApiModelProperty(value = "页码", example = "1")
    private Integer page = 1;
    
    @ApiModelProperty(value = "每页大小", example = "10")
    private Integer size = 10;
    
    // Getters
    public Integer getUserId() { return userId; }
    public Integer getPetId() { return petId; }
    public String getServiceType() { return serviceType; }
    public LocalDate getDate() { return date; }
    public String getTimeSlot() { return timeSlot; }
    public String getStatus() { return status; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public Integer getPage() { return page; }
    public Integer getSize() { return size; }
    
    // Setters
    public void setUserId(Integer userId) { this.userId = userId; }
    public void setPetId(Integer petId) { this.petId = petId; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }
    public void setDate(LocalDate date) { this.date = date; }
    public void setTimeSlot(String timeSlot) { this.timeSlot = timeSlot; }
    public void setStatus(String status) { this.status = status; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public void setPage(Integer page) { this.page = page; }
    public void setSize(Integer size) { this.size = size; }
}
