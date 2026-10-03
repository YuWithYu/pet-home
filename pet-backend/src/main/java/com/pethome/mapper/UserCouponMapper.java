package com.pethome.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pethome.entity.UserCoupon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

/**
 * 用户持券 Mapper
 */
@Mapper
public interface UserCouponMapper extends BaseMapper<UserCoupon> {

    /**
     * 条件核销：仅当券属于该用户、状态为 unused 且未过期时置为 used。
     * 影响行数=0 表示券不可用（已用/过期/越权），用于防重复核销。
     */
    @Update("UPDATE user_coupon SET status = 'used', order_id = #{orderId}, use_time = #{useTime}, " +
            "update_time = NOW() " +
            "WHERE id = #{id} AND user_id = #{userId} AND status = 'unused' " +
            "AND valid_end_time > #{useTime}")
    int markUsed(@Param("id") Long id,
                 @Param("userId") Long userId,
                 @Param("orderId") Long orderId,
                 @Param("useTime") LocalDateTime useTime);

    /**
     * 返还：订单取消时将已核销的券恢复为 unused（按订单维度定位，防止误还他人券）。
     */
    @Update("UPDATE user_coupon SET status = 'unused', order_id = NULL, use_time = NULL, " +
            "update_time = NOW() " +
            "WHERE order_id = #{orderId} AND status = 'used' AND user_id = #{userId}")
    int returnByOrder(@Param("orderId") Long orderId, @Param("userId") Long userId);
}
