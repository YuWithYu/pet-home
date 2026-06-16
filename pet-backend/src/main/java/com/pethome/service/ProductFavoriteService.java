package com.pethome.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.Product;

/**
 * 商品收藏服务接口
 */
public interface ProductFavoriteService {
    
    /**
     * 收藏商品
     */
    boolean collectProduct(Long productId, Long userId);
    
    /**
     * 取消收藏商品
     */
    boolean uncollectProduct(Long productId, Long userId);
    
    /**
     * 检查用户是否已收藏商品
     */
    boolean isCollected(Long productId, Long userId);
    
    /**
     * 获取用户收藏的商品列表
     */
    Page<Product> getCollectedProducts(Long userId, Page<Product> page);
}
