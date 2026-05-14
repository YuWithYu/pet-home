package com.pethome.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.Product;
import com.pethome.entity.ProductReview;

public interface ProductService {
    java.util.List<Product> getAllProducts();
    java.util.List<Product> getAllProducts(Long storeId); // 按店铺ID过滤
    java.util.List<Product> getProductsByCategoryId(Integer categoryId);
    IPage<Product> getProductPage(Page<Product> page);
    IPage<Product> getProductPage(Page<Product> page, Long storeId); // 按店铺ID过滤
    Product createProduct(Product product);
    Product updateProduct(Product product);
    boolean deleteProduct(Long id);
    Product getProductById(Long id);
    java.util.List<Product> listProductsByIds(java.util.Collection<Long> productIds);
    java.util.List<Product> getHotProducts(Integer limit);
    java.util.List<Product> getRecommendProducts(Integer limit);
    boolean setProductHot(Long productId, Boolean isHot);
    boolean batchSetHotProducts(java.util.List<Long> productIds, Boolean isHot);
    Integer getFavoriteCount(Long userId);
    java.util.List<ProductReview> getProductReviews(Long productId);
    ProductReview createProductReview(ProductReview review);
    java.util.List<java.util.Map<String, Object>> getProductSalesRecords(Long productId);
    
    // 评论点赞相关
    boolean toggleReviewLike(Long reviewId, Long userId);
    boolean isReviewLiked(Long reviewId, Long userId);
    Integer getReviewLikeCount(Long reviewId);
    
    // 评论回复相关
    com.pethome.entity.ReviewReply createReviewReply(Long reviewId, Long userId, String content);
    java.util.List<com.pethome.entity.ReviewReply> getReviewReplies(Long reviewId);
    Integer getReviewReplyCount(Long reviewId);
}
