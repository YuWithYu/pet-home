package com.pethome.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pethome.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    @Update("UPDATE product SET stock = stock - #{quantity} WHERE id = #{productId} AND stock >= #{quantity}")
    int decrementStock(@Param("productId") Long productId, @Param("quantity") int quantity);

    /**
     * 原子恢复库存（订单取消时回补，避免读-改-写在并发下丢失更新）
     */
    @Update("UPDATE product SET stock = stock + #{quantity} WHERE id = #{productId}")
    int incrementStock(@Param("productId") Long productId, @Param("quantity") int quantity);
}
