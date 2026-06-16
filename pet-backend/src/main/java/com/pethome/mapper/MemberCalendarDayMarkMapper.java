package com.pethome.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pethome.entity.MemberCalendarDayMark;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface MemberCalendarDayMarkMapper extends BaseMapper<MemberCalendarDayMark> {

    @Select("SELECT mark_date FROM member_calendar_day_mark WHERE member_id = #{memberId} "
            + "AND mark_type = 'leave' AND mark_date >= #{startDate} AND mark_date <= #{endDate} "
            + "ORDER BY mark_date")
    List<LocalDate> selectLeaveDates(
            @Param("memberId") Long memberId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
