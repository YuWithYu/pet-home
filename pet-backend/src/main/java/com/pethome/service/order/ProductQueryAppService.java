package com.pethome.service.order;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pethome.entity.Product;
import com.pethome.entity.ProductReview;
import com.pethome.entity.ReviewReply;

import java.util.List;
import java.util.Map;

/**
 * 商品查询应用服务：商品列表/分页/详情、评价与销售记录、收藏状态与收藏列表。
 * <p>
 * 店铺隔离所需的身份（是否管理员、当前店铺 ID）由 Controller 解析后以参数传入，
 * 服务层只负责过滤、视图字段组装（如 skuStockList）。
 */
public interface ProductQueryAppService {

    /** 商品列表（含分类筛选、排除分类与店铺隔离） */
    Map<String, Object> getProductList(Integer categoryId, String excludeCategory, Integer page, Integer size,
                                       Long currentStoreId);

    /** 商品分页查询（店铺管理员强制只查自己店铺） */
    IPage<Product> getProductPage(Integer pageNo, Integer pageSize, Long storeId, Long currentStoreId);

    /** 热门商品 */
    List<Product> getHotProducts(Integer limit);

    /** 推荐商品 */
    List<Product> getRecommendProducts(Integer limit);

    /** 商品详情（根据 specs 生成 skuStockList） */
    Product getProductDetail(Long productId);

    /** 商品评价列表（可选填充当前用户点赞状态） */
    List<ProductReview> getProductReviews(Long productId, Long userId);

    /** 商品销售记录 */
    List<Map<String, Object>> getProductSalesRecords(Long productId);

    /** 评论点赞状态与数量 */
    Map<String, Object> getReviewLikeStatus(Long reviewId, Long userId);

    /** 评论回复列表 */
    List<ReviewReply> getReviewReplies(Long reviewId);

    /** 当前用户是否已收藏该商品 */
    boolean isCollected(Long productId, Long userId);

    /** 当前用户收藏的商品列表 */
    Map<String, Object> getCollectedProducts(Long userId, Integer page, Integer size);
}
