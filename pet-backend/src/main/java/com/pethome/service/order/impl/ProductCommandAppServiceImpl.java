package com.pethome.service.order.impl;

import com.pethome.common.BusinessException;
import com.pethome.entity.Product;
import com.pethome.entity.ProductReview;
import com.pethome.entity.ReviewReply;
import com.pethome.service.order.ProductCommandAppService;
import com.pethome.service.order.ProductFavoriteService;
import com.pethome.service.order.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 商品写入应用服务实现。
 * <p>
 * 原 ProductController 的权限/店铺归属校验、评价校验与收藏编排下沉至此，
 * 管理员判定由 Controller 解析后传入，与既有 OrderQueryAppService 约定一致。
 */
@Service
public class ProductCommandAppServiceImpl implements ProductCommandAppService {

    private static final Logger log = LoggerFactory.getLogger(ProductCommandAppServiceImpl.class);

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductFavoriteService productFavoriteService;

    @Override
    public Product createProduct(Product product, boolean admin, Long currentStoreId) {
        // 安全修复：原实现只做店铺隔离，普通用户解析不到店铺时可直接创建全平台商品
        if (!admin) {
            throw new BusinessException(403, "无权创建商品");
        }
        // 数据隔离：如果是店铺管理员，自动设置店铺ID
        if (currentStoreId != null) {
            product.setStoreId(currentStoreId);
        }
        // 验证：如果用户是店铺管理员，但传入的storeId不匹配，拒绝创建
        if (currentStoreId != null && product.getStoreId() != null
                && !product.getStoreId().equals(currentStoreId)) {
            throw new BusinessException("无权创建其他店铺的商品");
        }

        Product savedProduct = productService.createProduct(product);
        if (savedProduct == null) {
            throw new BusinessException("创建商品失败");
        }
        return savedProduct;
    }

    @Override
    public Product updateProduct(Product product, boolean admin, Long currentStoreId) {
        // 安全修复：普通用户可绕过店铺隔离修改任意商品（含价格）
        if (!admin) {
            throw new BusinessException(403, "无权修改商品");
        }
        // 数据隔离：如果是店铺管理员，验证商品是否属于其店铺
        if (currentStoreId != null) {
            Product existingProduct = productService.getProductById(product.getId());
            if (existingProduct == null || !currentStoreId.equals(existingProduct.getStoreId())) {
                throw new BusinessException("无权修改此商品");
            }
            // 确保更新后的商品仍然属于该店铺
            product.setStoreId(currentStoreId);
        }

        log.debug("收到更新商品请求 - id: {}, specs: {}, services: {}",
                product.getId(), product.getSpecs(), product.getServices());

        Product updatedProduct = productService.updateProduct(product);
        if (updatedProduct == null) {
            throw new BusinessException("更新商品失败");
        }
        log.debug("商品更新成功 - id: {}, specs: {}, services: {}",
                updatedProduct.getId(), updatedProduct.getSpecs(), updatedProduct.getServices());
        return updatedProduct;
    }

    @Override
    public boolean deleteProduct(Long productId, boolean admin, Long currentStoreId) {
        // 安全修复：普通用户可绕过店铺隔离删除全平台商品
        if (!admin) {
            throw new BusinessException(403, "无权删除商品");
        }
        // 数据隔离：如果是店铺管理员，验证商品是否属于其店铺
        if (currentStoreId != null) {
            Product product = productService.getProductById(productId);
            if (product == null || !currentStoreId.equals(product.getStoreId())) {
                throw new BusinessException("无权删除此商品");
            }
        }
        return productService.deleteProduct(productId);
    }

    @Override
    public ProductReview createProductReview(Long productId, ProductReview review, Long userId) {
        // 安全修复：评价人以登录态为准，忽略请求体中的 userId（防止替他人发评价）
        if (userId == null) {
            throw new BusinessException(401, "用户未登录或token无效");
        }
        review.setUserId(userId);
        log.debug("收到创建评价请求 - 商品ID: {}, 用户ID: {}, 评分: {}", productId, userId, review.getRating());

        // 设置商品ID
        review.setProductId(productId);

        // 设置默认状态为正常（1）
        if (review.getStatus() == null) {
            review.setStatus(1);
        }

        // 验证必填字段
        if (review.getRating() == null || review.getRating() < 1 || review.getRating() > 5) {
            throw new BusinessException("评分必须在1-5之间");
        }
        if (review.getComment() == null || review.getComment().trim().isEmpty()) {
            throw new BusinessException("评价内容不能为空");
        }

        ProductReview createdReview = productService.createProductReview(review);
        if (createdReview == null) {
            log.warn("创建评价失败 - 返回null");
            throw new BusinessException("创建评价失败");
        }
        log.debug("创建评价成功 - 评价ID: {}", createdReview.getId());
        return createdReview;
    }

    @Override
    public Map<String, Object> toggleReviewLike(Long reviewId, Long userId) {
        // 安全修复：点赞人以登录态为准（原从 body 取 userId，可替他人点赞）
        if (userId == null) {
            throw new BusinessException(401, "用户未登录或token无效");
        }
        boolean isLiked = productService.toggleReviewLike(reviewId, userId);
        Integer likeCount = productService.getReviewLikeCount(reviewId);

        Map<String, Object> result = new HashMap<>();
        result.put("isLiked", isLiked);
        result.put("likeCount", likeCount);
        return result;
    }

    @Override
    public ReviewReply createReviewReply(Long reviewId, Long userId, String content) {
        // 安全修复：回复人以登录态为准（原从 body 取 userId，可替他人回复）
        if (userId == null) {
            throw new BusinessException(401, "用户未登录或token无效");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException("回复内容不能为空");
        }
        return productService.createReviewReply(reviewId, userId, content);
    }

    @Override
    public boolean collectProduct(Long productId, Long userId) {
        // 安全修复：收藏人以登录态为准（原从 query 取 userId，可操作他人收藏）
        if (userId == null) {
            throw new BusinessException(401, "用户未登录或token无效");
        }
        if (!productFavoriteService.collectProduct(productId, userId)) {
            throw new BusinessException("已经收藏过了");
        }
        return true;
    }

    @Override
    public boolean uncollectProduct(Long productId, Long userId) {
        // 安全修复：操作人以登录态为准
        if (userId == null) {
            throw new BusinessException(401, "用户未登录或token无效");
        }
        if (!productFavoriteService.uncollectProduct(productId, userId)) {
            throw new BusinessException("没有收藏记录");
        }
        return true;
    }
}
