package com.pethome.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pethome.entity.ProductFavorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 商品收藏Mapper
 */
@Mapper
public interface ProductFavoriteMapper extends BaseMapper<ProductFavorite> {
    
    /**
     * 检查用户是否已收藏商品
     */
    ProductFavorite checkCollectStatus(@Param("productId") Long productId, @Param("userId") Long userId);
    
    /**
     * 获取用户今日收藏的商品数量
     */
    int getTodayCollectCount(@Param("userId") Long userId);
}
