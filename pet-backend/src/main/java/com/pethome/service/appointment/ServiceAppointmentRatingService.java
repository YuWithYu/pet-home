package com.pethome.service.appointment;

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

    /**
     * 提交服务评价：校验预约已完成、归属本人、未重复评价，写入后回写服务人员平均分。
     * 业务失败以 BusinessException 抛出，表现层不再自行判断。
     */
    ServiceAppointmentRating submitRating(Long userId, String appointmentType, Long appointmentId,
                                          int rating, String comment);

    /** 检查是否已评价；查询异常时按“未评价”降级（轻量探测接口的历史行为） */
    Map<String, Object> checkRatedQuietly(String appointmentType, Long appointmentId, Long userId);
}
