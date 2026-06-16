package com.pethome.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pethome.entity.ServiceMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 服务人员Mapper接口
 */
@Mapper
public interface ServiceMemberMapper extends BaseMapper<ServiceMember> {
    
    /**
     * 根据服务类型查询启用的服务人员
     */
    List<ServiceMember> selectByServiceType(@Param("serviceType") String serviceType);
    
    /**
     * 根据服务类型和门店查询启用的服务人员（storeId 为 null 时返回该类型所有人；否则返回 store_id = storeId 或 store_id is null 的成员）
     */
    List<ServiceMember> selectByServiceTypeAndStore(@Param("serviceType") String serviceType, @Param("storeId") Long storeId);
    
    /**
     * 查询服务人员在指定日期的任务数（今日待完成）
     */
    Integer countTasksByMemberAndDate(@Param("memberId") Long memberId, @Param("date") String date);

    /**
     * 查询服务人员总任务数（所有非取消的预约）
     */
    Integer countTotalTasksByMember(@Param("memberId") Long memberId);

    /** 上门铲屎-指定日期任务数 */
    Integer countDoorCleaningTasksByMemberAndDate(@Param("memberId") Long memberId, @Param("date") String date);
    /** 洗护-指定日期任务数 */
    Integer countGroomingTasksByMemberAndDate(@Param("memberId") Long memberId, @Param("date") String date);
    /** 医院-指定日期任务数 */
    Integer countHospitalTasksByMemberAndDate(@Param("memberId") Long memberId, @Param("date") String date);
    /** 上门铲屎-总任务数 */
    Integer countDoorCleaningTotalByMember(@Param("memberId") Long memberId);
    /** 洗护-总任务数 */
    Integer countGroomingTotalByMember(@Param("memberId") Long memberId);
    /** 医院-总任务数 */
    Integer countHospitalTotalByMember(@Param("memberId") Long memberId);
    
    /**
     * 查询所有服务人员（不区分状态和服务类型）
     */
    List<ServiceMember> selectAllMembers();

    /**
     * 根据 userId 查询服务人员
     */
    ServiceMember selectByUserId(@Param("userId") Long userId);
}

