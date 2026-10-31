package com.pethome.controller.order;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pethome.common.Result;
import com.pethome.common.SafeCall;
import com.pethome.entity.Product;
import com.pethome.entity.ProductReview;
import com.pethome.entity.ReviewReply;
import com.pethome.security.RequestIdentity;
import com.pethome.service.order.ProductCommandAppService;
import com.pethome.service.order.ProductQueryAppService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/product")
@Tag(name = "商品管理")
public class ProductController {

    /** 从登录态解析当前用户 ID（RoleInterceptor 注入）。写操作一律以登录态为准，禁止信任前端传入的 userId */
    private Long currentUserId(HttpServletRequest request) {
        Object attr = request.getAttribute("userId");
        return attr instanceof Number ? ((Number) attr).longValue() : null;
    }

    /** 店铺管理员所属店铺 ID（平台管理员或未登录为空） */
    private Long currentStoreId(String token) {
        return adminContext != null && token != null ? adminContext.getCurrentStoreId(token) : null;
    }

    @Autowired
    private ProductQueryAppService productQueryAppService;

    @Autowired
    private ProductCommandAppService productCommandAppService;

    @Autowired(required = false)
    private com.pethome.security.AdminContext adminContext;

    @GetMapping("/list")
    @Operation(summary = "获取商品列表")
    public Result<Object> getProductList(
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String excludeCategory,
            @RequestParam(required = false, defaultValue = "1") Integer pageNo,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "1000") Integer size,
            @RequestHeader(value = "Authorization", required = false) String token) {
        // 获取当前登录用户的店铺ID（数据隔离）
        return Result.<Object>success(productQueryAppService.getProductList(
                categoryId, excludeCategory, page, size, currentStoreId(token)));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询商品")
    public Result<IPage<Product>> getProductPage(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long storeId,
            @RequestHeader(value = "Authorization", required = false) String token) {
        // 数据隔离：店铺管理员只能查看自己店铺的商品
        return Result.success(productQueryAppService.getProductPage(
                pageNo, pageSize, storeId, currentStoreId(token)));
    }

    @PostMapping("/create")
    @Operation(summary = "创建商品")
    public Result<Product> createProduct(
            @RequestBody Product product,
            @RequestHeader(value = "Authorization", required = false) String token,
            HttpServletRequest request) {
        return SafeCall.of(() -> Result.success(productCommandAppService.createProduct(
                product, RequestIdentity.isAdmin(request), currentStoreId(token))),
                "创建商品失败，请稍后重试", log);
    }

    @PutMapping("/update")
    @Operation(summary = "更新商品")
    public Result<Product> updateProduct(
            @RequestBody Product product,
            @RequestHeader(value = "Authorization", required = false) String token,
            HttpServletRequest request) {
        return SafeCall.of(() -> Result.success(productCommandAppService.updateProduct(
                product, RequestIdentity.isAdmin(request), currentStoreId(token))),
                "更新商品失败，请稍后重试", log);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除商品")
    public Result<Boolean> deleteProduct(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String token,
            HttpServletRequest request) {
        return SafeCall.of(() -> Result.success(productCommandAppService.deleteProduct(
                id, RequestIdentity.isAdmin(request), currentStoreId(token))),
                "删除商品失败，请稍后重试", log);
    }

    @GetMapping("/hot")
    @Operation(summary = "获取热门商品")
    public Result<List<Product>> getHotProducts(@RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(productQueryAppService.getHotProducts(limit));
    }

    @GetMapping("/recommend")
    @Operation(summary = "获取推荐商品")
    public Result<List<Product>> getRecommendProducts(@RequestParam(defaultValue = "4") Integer limit) {
        return Result.success(productQueryAppService.getRecommendProducts(limit));
    }

    @GetMapping("/favorites/count")
    @Operation(summary = "获取用户商品收藏数量")
    public Result<Integer> getFavoriteCount(@RequestParam Long userId) {
        return Result.success(0);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取商品详情")
    public Result<Product> getProductDetail(@PathVariable Long id) {
        return SafeCall.of(() -> Result.success(productQueryAppService.getProductDetail(id)),
                "获取商品详情失败，请稍后重试", log);
    }

    @GetMapping("/{id}/reviews")
    @Operation(summary = "获取商品评价")
    public Result<List<ProductReview>> getProductReviews(
            @PathVariable Long id,
            @RequestParam(required = false) Long userId) {
        return SafeCall.of(() -> Result.success(productQueryAppService.getProductReviews(id, userId)),
                "获取商品评价失败，请稍后重试", log);
    }

    @GetMapping("/{id}/sales")
    @Operation(summary = "获取商品销售记录")
    public Result<List<Map<String, Object>>> getProductSalesRecords(@PathVariable Long id) {
        return SafeCall.of(() -> Result.success(productQueryAppService.getProductSalesRecords(id)),
                "获取商品销售记录失败，请稍后重试", log);
    }

    @PostMapping("/{id}/reviews")
    @Operation(summary = "创建商品评价")
    public Result<ProductReview> createProductReview(@PathVariable Long id, @RequestBody ProductReview review,
                                                     HttpServletRequest request) {
        return SafeCall.of(() -> Result.success(productCommandAppService.createProductReview(
                id, review, currentUserId(request))),
                "创建评价失败，请稍后重试", log);
    }

    // ========== 评论点赞相关接口 ==========

    @PostMapping("/review/{reviewId}/like")
    @Operation(summary = "点赞/取消点赞评论")
    public Result<Map<String, Object>> toggleReviewLike(
            @PathVariable Long reviewId,
            HttpServletRequest request) {
        // 安全修复：点赞人以登录态为准（原从 body 取 userId，可替他人点赞）
        return SafeCall.of(() -> Result.success(
                productCommandAppService.toggleReviewLike(reviewId, currentUserId(request))),
                "操作失败，请稍后重试", log);
    }

    @GetMapping("/review/{reviewId}/like/status")
    @Operation(summary = "获取评论点赞状态和数量")
    public Result<Map<String, Object>> getReviewLikeStatus(
            @PathVariable Long reviewId,
            @RequestParam(required = false) Long userId) {
        return SafeCall.of(() -> Result.success(
                productQueryAppService.getReviewLikeStatus(reviewId, userId)),
                "获取失败，请稍后重试", log);
    }

    // ========== 评论回复相关接口 ==========

    @PostMapping("/review/{reviewId}/reply")
    @Operation(summary = "回复评论")
    public Result<ReviewReply> createReviewReply(
            @PathVariable Long reviewId,
            @RequestBody Map<String, Object> params,
            HttpServletRequest request) {
        // 安全修复：回复人以登录态为准（原从 body 取 userId，可替他人回复）
        String content = params != null && params.get("content") != null ? params.get("content").toString() : null;
        return SafeCall.of(() -> Result.success(productCommandAppService.createReviewReply(
                reviewId, currentUserId(request), content)),
                "回复失败，请稍后重试", log);
    }

    @GetMapping("/review/{reviewId}/replies")
    @Operation(summary = "获取评论的回复列表")
    public Result<List<ReviewReply>> getReviewReplies(
            @PathVariable Long reviewId) {
        return SafeCall.of(() -> Result.success(productQueryAppService.getReviewReplies(reviewId)),
                "获取回复列表失败，请稍后重试", log);
    }

    @PostMapping("/{id}/collect")
    @Operation(summary = "收藏商品")
    public Result<Boolean> collectProduct(@PathVariable Long id, HttpServletRequest request) {
        // 安全修复：收藏人以登录态为准（原从 query 取 userId，可操作他人收藏）
        return SafeCall.of(() -> Result.success(
                productCommandAppService.collectProduct(id, currentUserId(request))),
                "收藏失败，请稍后重试", log);
    }

    @DeleteMapping("/{id}/collect")
    @Operation(summary = "取消收藏商品")
    public Result<Boolean> uncollectProduct(@PathVariable Long id, HttpServletRequest request) {
        // 安全修复：操作人以登录态为准
        return SafeCall.of(() -> Result.success(
                productCommandAppService.uncollectProduct(id, currentUserId(request))),
                "取消收藏失败，请稍后重试", log);
    }

    @GetMapping("/{id}/collect/status")
    @Operation(summary = "检查商品收藏状态")
    public Result<Boolean> checkProductCollectStatus(@PathVariable Long id, HttpServletRequest request) {
        // 安全修复：收藏状态属个人数据，以登录态为准
        return SafeCall.of(() -> Result.success(
                productQueryAppService.isCollected(id, currentUserId(request))),
                "检查收藏状态失败，请稍后重试", log);
    }

    @GetMapping("/collected")
    @Operation(summary = "获取用户收藏的商品列表")
    public Result<Map<String, Object>> getCollectedProducts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        // 安全修复：收藏列表属个人数据，以登录态为准（原从 query 取 userId，可越权查看他人收藏）
        return SafeCall.of(() -> Result.success(
                productQueryAppService.getCollectedProducts(currentUserId(request), page, size)),
                "获取收藏列表失败，请稍后重试", log);
    }
}
