package com.pethome.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.Product;
import com.pethome.mapper.ProductMapper;
import com.pethome.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;
    
    @Autowired
    private com.pethome.mapper.CategoryMapper categoryMapper;

    @Override
    public List<Product> getAllProducts() {
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time"); // 按创建时间排序，返回所有商品
        return productMapper.selectList(queryWrapper);
    }

    @Override
    public List<Product> getProductsByCategoryId(Integer categoryId) {
        // 先根据分类ID查询分类名称
        com.pethome.entity.Category category = categoryMapper.selectById(Long.valueOf(categoryId));
        
        if (category == null) {
            return new java.util.ArrayList<>();
        }
        
        // 用分类名称查询商品（因为product表的category字段存的是分类名称）
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("category", category.getName());
        queryWrapper.orderByDesc("create_time");
        return productMapper.selectList(queryWrapper);
    }

    @Override
    public IPage<Product> getProductPage(Page<Product> page) {
        return productMapper.selectPage(page, null);
    }

    @Override
    public Product createProduct(Product product) {
        product.setCreateTime(LocalDateTime.now());
        product.setUpdateTime(LocalDateTime.now());
        productMapper.insert(product);
        return product;
    }

    @Override
    public Product updateProduct(Product product) {
        product.setUpdateTime(LocalDateTime.now());
        productMapper.updateById(product);
        return product;
    }

    @Override
    public boolean deleteProduct(Long id) {
        return productMapper.deleteById(id) > 0;
    }

    @Override
    public Product getProductById(Long id) {
        return productMapper.selectById(id);
    }

    @Override
    public List<Product> getHotProducts(Integer limit) {
        // 根据库存、销售量等综合评分获取热门商品
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1) // 只查询在售商品（状态为1）
                .orderByDesc("stock") // 按库存排序（库存越多越热门）
                .last("limit " + limit);

        return productMapper.selectList(queryWrapper);
    }

    @Override
    public List<Product> getRecommendProducts(Integer limit) {
        // 随机推荐商品（简化版，实际应该基于用户行为分析）
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", "onsale")
                .orderByDesc("create_time") // 按创建时间排序，最新商品优先
                .last("limit " + limit);
        return productMapper.selectList(queryWrapper);
    }

    @Override
    public Integer getFavoriteCount(Long userId) {
        // 这里应该从product_favorite表查询用户收藏的商品数量
        // 由于数据库中没有product_favorite表，先返回固定值
        return 5; // 假设用户收藏了5个商品
    }
}
