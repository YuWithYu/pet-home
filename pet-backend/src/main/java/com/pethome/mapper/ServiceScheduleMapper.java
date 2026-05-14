package com.pethome.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pethome.entity.ServiceSchedule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 服务人员可预约时间表Mapper接口
 */
@Mapper
public interface ServiceScheduleMapper extends BaseMapper<ServiceSchedule> {
    
    /**
     * 查询指定日期和时间段的可预约记录
     * @param storeId 门店ID，不为 null 时仅返回该门店的排班；null 时返回所有门店
     */
    List<ServiceSchedule> selectAvailableSchedules(
            @Param("date") LocalDate date,
            @Param("timeSlot") String timeSlot,
            @Param("serviceType") String serviceType,
            @Param("storeId") Long storeId
    );

    /**
     * 占用指定时间段，并递增已预约数量
     */
    int occupyTimeSlot(
            @Param("id") Long id,
            @Param("taskId") Long taskId
    );
    
    /**
     * 查询服务人员在指定日期的所有时间段
     */
    List<ServiceSchedule> selectByMemberAndDate(
            @Param("memberId") Long memberId,
            @Param("date") LocalDate date
    );
    
    /**
     * 查询指定日期、时间段和状态的所有记录
     */
    List<ServiceSchedule> selectByDateAndTimeSlotAndStatus(
            @Param("date") LocalDate date,
            @Param("timeSlot") String timeSlot,
            @Param("status") String status
    );
    
    /**
     * 更新时间段状态和关联任务ID
     */
    int updateScheduleStatus(
            @Param("id") Long id,
            @Param("status") String status,
            @Param("taskId") Long taskId
    );
    
    /**
     * 根据服务类型和日期查询所有可用时间段
     */
    List<ServiceSchedule> selectByServiceTypeAndDate(
            @Param("serviceType") String serviceType,
            @Param("date") LocalDate date
    );

    /**
     * 根据服务类型、日期、门店查询排班（仅该门店员工）
     */
    List<ServiceSchedule> selectByServiceTypeAndDateAndStore(
            @Param("serviceType") String serviceType,
            @Param("date") LocalDate date,
            @Param("storeId") Long storeId
    );

    /**
     * 根据服务类型、日期范围、门店查询排班（用于日历）
     */
    List<ServiceSchedule> selectByServiceTypeAndDateRangeAndStore(
            @Param("serviceType") String serviceType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("storeId") Long storeId
    );
}

