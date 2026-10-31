package com.pethome.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pethome.entity.ServiceAppointmentRating;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 服务预约评价应用服务。
 * <p>
 * 表现层只负责鉴权与参数绑定，评价的查询口径（同一预约同一用户只能评价一次）
 * 与平均分统计口径集中在此，避免各 Controller 自行拼装 SQL。
 */
public interface ServiceAppointmentRatingService extends IService<ServiceAppointmentRating> {

    /** 查询某次预约下该用户的评价（用于幂等校验） */
    ServiceAppointmentRating findByAppointmentAndUser(String appointmentType, Long appointmentId, Long userId);

    /** 单个服务人员的平均评分 */
    BigDecimal avgRatingByMemberId(Long memberId);

    /** 批量平均评分（一条 GROUP BY，替代逐人 AVG） */
    Map<Long, BigDecimal> avgRatingByMemberIds(Collection<Long> memberIds);

    /** 服务人员的全部评价，按创建时间倒序 */
    List<ServiceAppointmentRating> listByMemberId(Long memberId);
}
