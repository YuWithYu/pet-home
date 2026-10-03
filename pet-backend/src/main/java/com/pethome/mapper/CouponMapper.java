package com.pethome.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pethome.entity.Coupon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 优惠券模板 Mapper
 */
@Mapper
public interface CouponMapper extends BaseMapper<Coupon> {

    /**
     * 乐观扣减领取额度（防超发）：
     * total_count=0 表示不限量；否则仅当 received_count &lt; total_count 时 +1。
     * UPDATE 行锁持续到事务提交，天然串行化同一券的并发领取。
     *
     * @return 影响行数：0 表示已领完或券不存在
     */
    @Update("UPDATE coupon SET received_count = received_count + 1, update_time = NOW() " +
            "WHERE id = #{couponId} AND status = 'active' " +
            "AND (total_count = 0 OR received_count < total_count)")
    int incrementReceivedCount(@Param("couponId") Long couponId);
}
