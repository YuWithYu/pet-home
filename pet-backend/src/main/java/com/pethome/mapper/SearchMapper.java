package com.pethome.mapper;

import com.pethome.entity.Product;
import com.pethome.entity.Post;
import com.pethome.entity.ServiceStore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 搜索Mapper
 */
@Mapper
public interface SearchMapper {
    
    /**
     * 搜索商品
     * @param keyword 关键词
     * @return 商品列表
     */
    List<Product> searchProducts(@Param("keyword") String keyword);
    
    /**
     * 搜索帖子
     * @param keyword 关键词
     * @return 帖子列表
     */
    List<Post> searchPosts(@Param("keyword") String keyword);
    
    /**
     * 搜索服务门店
     * @param keyword 关键词
     * @return 服务门店列表
     */
    List<ServiceStore> searchServices(@Param("keyword") String keyword);
}

