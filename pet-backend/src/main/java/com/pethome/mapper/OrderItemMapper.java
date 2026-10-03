package com.pethome.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pethome.entity.OrderItem;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {

    @Select("SELECT * FROM order_item WHERE order_id = #{orderId}")
    List<OrderItem> selectByOrderId(Long orderId);

    @Select("<script>SELECT * FROM order_item WHERE order_id IN "
            + "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>"
            + "</script>")
    List<OrderItem> selectByOrderIds(@Param("ids") List<Long> ids);

    /**
     * 批量插入订单项（单条 foreach INSERT 替代循环单条 insert，消除写入 N+1）
     * create_time/update_time 依赖数据库默认值（DEFAULT CURRENT_TIMESTAMP / ON UPDATE）
     */
    @Insert("<script>"
            + "INSERT INTO order_item (order_id, product_id, product_name, product_image, spec_name, spec_price, quantity, price) VALUES "
            + "<foreach collection='items' item='item' separator=','>"
            + "(#{item.orderId}, #{item.productId}, #{item.productName}, #{item.productImage}, #{item.specName}, #{item.specPrice}, #{item.quantity}, #{item.price})"
            + "</foreach>"
            + "</script>")
    int batchInsert(@Param("items") List<OrderItem> items);

    /**
     * 批量统计商品销量（单次 GROUP BY 替代逐商品 selectCount，消除列表页 N+1）
     * 返回行结构：productId -> 商品ID，cnt -> 订单项数
     */
    @Select("<script>SELECT product_id AS productId, COUNT(*) AS cnt FROM order_item "
            + "WHERE product_id IN "
            + "<foreach collection='productIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>"
            + " GROUP BY product_id</script>")
    List<Map<String, Object>> countByProductIds(@Param("productIds") List<Long> productIds);

    @Select("SELECT oi.* FROM order_item oi " +
            "INNER JOIN pet_order o ON oi.order_id = o.id " +
            "WHERE oi.product_id = #{productId} " +
            "AND o.status IN (2, 3, 4) " +  // 2=已支付, 3=已发货, 4=已完成
            "ORDER BY o.create_time DESC")
    List<OrderItem> selectByProductId(Long productId);
}
