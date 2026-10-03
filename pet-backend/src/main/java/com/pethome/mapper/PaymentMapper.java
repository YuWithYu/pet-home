package com.pethome.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pethome.entity.Payment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface PaymentMapper extends BaseMapper<Payment> {

    /**
     * 支付成功回填（条件更新：仅待支付状态生效，回调/查单补偿并发下只生效一次）
     */
    @Update("UPDATE payment SET status = 1, transaction_id = #{transactionId}, " +
            "pay_time = NOW(), update_time = NOW() " +
            "WHERE id = #{paymentId} AND status = 0")
    int markPaid(@Param("paymentId") Long paymentId, @Param("transactionId") String transactionId);
}
