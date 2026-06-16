package com.pethome.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pethome.entity.Product;
import com.pethome.entity.ProductFavorite;
import com.pethome.mapper.ProductFavoriteMapper;
import com.pethome.mapper.ProductMapper;
import com.pethome.service.ProductFavoriteService;
import com.pethome.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductFavoriteServiceImpl extends ServiceImpl<ProductFavoriteMapper, ProductFavorite> implements ProductFavoriteService {
    
    @Autowired
    private ProductFavoriteMapper productFavoriteMapper;
    
    @Autowired
    private ProductMapper productMapper;
    
    @Autowired
    @Lazy
    private TaskService taskService;
    
    @Override
    @Transactional
    public boolean collectProduct(Long productId, Long userId) {
        // 参数验证
        if (productId == null || userId == null) {
            throw new IllegalArgumentException("商品ID和用户ID不能为空");
        }
        
        // 检查是否已收藏
        ProductFavorite existingFavorite = productFavoriteMapper.checkCollectStatus(productId, userId);
        if (existingFavorite != null) {
            return false; // 已经收藏过了
        }
        
        // 添加收藏记录
        ProductFavorite favorite = new ProductFavorite();
        favorite.setProductId(productId);
        favorite.setUserId(userId);
        favorite.setCreateTime(LocalDateTime.now());
        productFavoriteMapper.insert(favorite);
        
        // 更新任务进度：收藏内容任务（商品收藏也算收藏内容）
        try {
            // 获取用户今日已收藏的商品数量
            int todayCollectCount = productFavoriteMapper.getTodayCollectCount(userId);
            taskService.updateTaskProgress(userId, "collect_content", todayCollectCount);
            System.out.println("更新收藏内容任务进度，用户ID: " + userId + ", 今日收藏商品数: " + todayCollectCount);
        } catch (Exception e) {
            // 任务进度更新失败不影响收藏流程
            System.err.println("更新收藏内容任务进度失败: " + e.getMessage());
        }
        
        return true;
    }
    
    @Override
    @Transactional
    public boolean uncollectProduct(Long productId, Long userId) {
        // 参数验证
        if (productId == null || userId == null) {
            throw new IllegalArgumentException("商品ID和用户ID不能为空");
        }
        
        // 查找收藏记录
        ProductFavorite favorite = productFavoriteMapper.checkCollectStatus(productId, userId);
        if (favorite == null) {
            return false; // 没有收藏记录
        }
        
        // 删除收藏记录
        productFavoriteMapper.deleteById(favorite.getId());
        
        return true;
    }
    
    @Override
    public boolean isCollected(Long productId, Long userId) {
        ProductFavorite favorite = productFavoriteMapper.checkCollectStatus(productId, userId);
        return favorite != null;
    }
    
    @Override
    public Page<Product> getCollectedProducts(Long userId, Page<Product> page) {
        // 查询用户收藏的商品ID列表
        QueryWrapper<ProductFavorite> favoriteWrapper = new QueryWrapper<>();
        favoriteWrapper.eq("user_id", userId);
        favoriteWrapper.orderByDesc("create_time");
        
        List<ProductFavorite> favorites = productFavoriteMapper.selectList(favoriteWrapper);
        
        if (favorites == null || favorites.isEmpty()) {
            return page; // 返回空分页
        }
        
        // 提取商品ID列表
        List<Long> productIds = favorites.stream()
            .map(ProductFavorite::getProductId)
            .collect(Collectors.toList());
        
        // 查询商品详情
        QueryWrapper<Product> productWrapper = new QueryWrapper<>();
        productWrapper.in("id", productIds);
        productWrapper.eq("status", 1); // 只查询正常状态的商品
        
        // 按收藏时间排序（需要关联查询，这里简化处理，按商品创建时间倒序）
        productWrapper.orderByDesc("create_time");
        
        // 分页查询
        Page<Product> result = productMapper.selectPage(page, productWrapper);
        
        return result;
    }
}
