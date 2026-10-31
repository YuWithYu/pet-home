package com.pethome.service.order;

import com.pethome.entity.Product;
import com.pethome.entity.ProductReview;
import com.pethome.entity.ReviewReply;

import java.util.Map;

/**
 * 商品写入应用服务：商品增改删、评价/点赞/回复、收藏。
 * <p>
 * 权限（是否管理员）与店铺身份由 Controller 解析后以参数传入；归属校验、必填校验与安全修复行为在服务层完成。
 */
public interface ProductCommandAppService {

    /** 创建商品（仅管理员；店铺管理员强制写入自己店铺） */
    Product createProduct(Product product, boolean admin, Long currentStoreId);

    /** 更新商品（仅管理员；店铺管理员只能改自己店铺商品） */
    Product updateProduct(Product product, boolean admin, Long currentStoreId);

    /** 删除商品（仅管理员；店铺管理员只能删自己店铺商品） */
    boolean deleteProduct(Long productId, boolean admin, Long currentStoreId);

    /** 创建商品评价（评价人以登录态为准） */
    ProductReview createProductReview(Long productId, ProductReview review, Long userId);

    /** 点赞/取消点赞评价，返回最新点赞状态与数量 */
    Map<String, Object> toggleReviewLike(Long reviewId, Long userId);

    /** 回复评价 */
    ReviewReply createReviewReply(Long reviewId, Long userId, String content);

    /** 收藏商品，重复收藏抛出业务异常 */
    boolean collectProduct(Long productId, Long userId);

    /** 取消收藏商品，无收藏记录抛出业务异常 */
    boolean uncollectProduct(Long productId, Long userId);
}
