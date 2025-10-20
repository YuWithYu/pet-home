package com.pethome.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.Product;

public interface ProductService {
    java.util.List<Product> getAllProducts();
    java.util.List<Product> getProductsByCategoryId(Integer categoryId);
    IPage<Product> getProductPage(Page<Product> page);
    Product createProduct(Product product);
    Product updateProduct(Product product);
    boolean deleteProduct(Long id);
    Product getProductById(Long id);
    java.util.List<Product> getHotProducts(Integer limit);
    java.util.List<Product> getRecommendProducts(Integer limit);
    Integer getFavoriteCount(Long userId);
}
