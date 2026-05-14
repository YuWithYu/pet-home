package com.pethome.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pethome.entity.RefundRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RefundRequestMapper extends BaseMapper<RefundRequest> {
    
    @Select("SELECT * FROM refund_request WHERE order_id = #{orderId} ORDER BY create_time DESC LIMIT 1")
    RefundRequest selectByOrderId(Long orderId);

    @Select("<script>SELECT * FROM refund_request WHERE order_id IN "
            + "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach> "
            + "ORDER BY order_id ASC, create_time DESC</script>")
    List<RefundRequest> selectByOrderIds(@Param("ids") List<Long> ids);
    
    @Select("SELECT * FROM refund_request WHERE order_no = #{orderNo} ORDER BY create_time DESC LIMIT 1")
    RefundRequest selectByOrderNo(String orderNo);
}
