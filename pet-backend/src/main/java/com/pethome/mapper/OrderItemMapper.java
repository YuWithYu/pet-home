package com.pethome.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pethome.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {
    
    @Select("SELECT * FROM order_item WHERE order_id = #{orderId}")
    List<OrderItem> selectByOrderId(Long orderId);

    @Select("<script>SELECT * FROM order_item WHERE order_id IN "
            + "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>"
            + "</script>")
    List<OrderItem> selectByOrderIds(@Param("ids") List<Long> ids);
    
    @Select("SELECT oi.* FROM order_item oi " +
            "INNER JOIN pet_order o ON oi.order_id = o.id " +
            "WHERE oi.product_id = #{productId} " +
            "AND o.status IN (2, 3, 4) " +  // 2=已支付, 3=已发货, 4=已完成
            "ORDER BY o.create_time DESC")
    List<OrderItem> selectByProductId(Long productId);
}
