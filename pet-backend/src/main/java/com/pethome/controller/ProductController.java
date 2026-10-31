package com.pethome.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.common.Result;
import com.pethome.entity.Product;
import com.pethome.entity.ProductReview;
import com.pethome.service.ProductService;
import com.pethome.service.ProductFavoriteService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.pethome.util.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import javax.servlet.http.HttpServletRequest;

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

    @Autowired
    private ProductService productService;
    
    @Autowired
    private ProductFavoriteService productFavoriteService;
    
    @Autowired(required = false)
    private com.pethome.security.AdminContext adminContext;
    
    @Value("${host:http://localhost}")
    private String serverHost;
    
    @Value("${server.port:8080}")
    private String serverPort;
    
    /**
     * 将相对路径转换为完整的图片URL
     */
    private String convertToFullUrl(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return "";
        }
        
        // 如果已经是完整URL，直接返回
        if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
            return imagePath;
        }
        
        // 确保路径格式正确
        // 如果路径以 /upload/ 开头，直接拼接
        if (imagePath.startsWith("/upload/")) {
            return serverHost + ":" + serverPort + imagePath;
        }
        
        // 如果路径以 / 开头但不是 /upload/，添加 /upload 前缀
        if (imagePath.startsWith("/")) {
            return serverHost + ":" + serverPort + "/upload" + imagePath;
        }
        
        // 其他情况（如 product/xxx.jpg），添加 /upload/ 前缀
        return serverHost + ":" + serverPort + "/upload/" + imagePath;
    }

    /** 将可能是完整 URL 的图片字段转为相对路径，供前端用与接口同源 base 拼接，避免 8443/证书导致不显示 */
    private String toRelativeImagePath(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) return imagePath;
        String s = imagePath.trim();
        if (s.startsWith("http://") || s.startsWith("https://")) {
            int pathStart = s.indexOf('/', s.indexOf("://") + 3);
            if (pathStart > 0) return s.substring(pathStart);
            return s;
        }
        if (!s.startsWith("/")) {
            if (s.startsWith("upload/") || s.startsWith("upload\\")) return "/" + s.replace('\\', '/');
            return "/upload/" + s;
        }
        return s;
    }

    /** 统一把商品的 image / imageUrl / detailImages 里的 localhost 等完整 URL 转为相对路径 */
    private void normalizeProductImagePaths(Product p) {
        if (p == null) return;
        if (p.getImage() != null) p.setImage(toRelativeImagePath(p.getImage()));
        if (p.getImageUrl() != null) p.setImageUrl(toRelativeImagePath(p.getImageUrl()));
        if (p.getDetailImages() != null && !p.getDetailImages().isEmpty()) {
            try {
                JsonNode parsed = JSONUtil.parseTree(p.getDetailImages());
                if (parsed != null && parsed.isArray()) {
                    List<String> out = new java.util.ArrayList<>();
                    for (JsonNode node : parsed) {
                        String url = node.isTextual() ? node.asText() : node.toString();
                        if (url != null && !url.isEmpty()) out.add(toRelativeImagePath(url));
                        else out.add(url);
                    }
                    p.setDetailImages(JSONUtil.toJSONString(out));
                }
            } catch (Exception ignored) { }
        }
    }

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
        
        List<Product> products;
        
        // 获取当前登录用户的店铺ID（数据隔离）
        Long currentStoreId = null;
        if (adminContext != null && token != null) {
            currentStoreId = adminContext.getCurrentStoreId(token);
        }
        
        // 如果指定了分类ID，按分类查询
        if (categoryId != null) {
            products = productService.getProductsByCategoryId(categoryId);
        } else {
            products = productService.getAllProducts();
        }
        
        // 排除指定分类（如热门推荐管理页排除积分商城）
        if (excludeCategory != null && !excludeCategory.isEmpty()) {
            final String exclude = excludeCategory;
            products = products.stream()
                .filter(p -> p.getCategory() == null || !exclude.equals(p.getCategory()))
                .collect(java.util.stream.Collectors.toList());
        }
        
        // 数据隔离：如果是店铺管理员，只返回自己店铺的商品
        if (currentStoreId != null) {
            final Long storeId = currentStoreId; // 创建final变量用于lambda
            products = products.stream()
                .filter(p -> p.getStoreId() != null && p.getStoreId().equals(storeId))
                .collect(java.util.stream.Collectors.toList());
        }
        
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("goods", products);  // 小程序需要goods字段
        result.put("records", products);
        result.put("total", products.size());
        result.put("size", size);
        result.put("current", page);
        result.put("pages", (products.size() + size - 1) / size);
        
        return Result.success(result);
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询商品")
    public Result<IPage<Product>> getProductPage(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long storeId,
            @RequestHeader(value = "Authorization", required = false) String token) {
        Page<Product> page = new Page<>(pageNo, pageSize);
        
        // 数据隔离：获取当前登录用户的店铺ID
        Long currentStoreId = null;
        if (adminContext != null && token != null) {
            currentStoreId = adminContext.getCurrentStoreId(token);
        }
        
        // 如果是店铺管理员，只能查看自己店铺的商品
        Long finalStoreId = storeId;
        if (currentStoreId != null) {
            // 店铺管理员只能查看自己店铺的商品，忽略传入的storeId参数
            finalStoreId = currentStoreId;
        }
        
        IPage<Product> result = productService.getProductPage(page, finalStoreId);
        return Result.success(result);
    }

    @PostMapping("/create")
    @Operation(summary = "创建商品")
    public Result<Product> createProduct(
            @RequestBody Product product,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            // 数据隔离：如果是店铺管理员，自动设置店铺ID
            Long currentStoreId = null;
            if (adminContext != null && token != null) {
                currentStoreId = adminContext.getCurrentStoreId(token);
                if (currentStoreId != null) {
                    // 店铺管理员只能创建自己店铺的商品
                    product.setStoreId(currentStoreId);
                }
            }
            
            // 验证：如果用户是店铺管理员，但传入的storeId不匹配，拒绝创建
            if (currentStoreId != null && product.getStoreId() != null && !product.getStoreId().equals(currentStoreId)) {
                return Result.error("无权创建其他店铺的商品");
            }
            
            // 保存商品到数据库
            Product savedProduct = productService.createProduct(product);
            if (savedProduct != null) {
                return Result.success(savedProduct);
            } else {
                return Result.error("创建商品失败");
            }
        } catch (Exception e) {
            log.error("创建商品失败", e);
            return Result.error("创建商品失败，请稍后重试");
        }
    }

    @PutMapping("/update")
    @Operation(summary = "更新商品")
    public Result<Product> updateProduct(
            @RequestBody Product product,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            // 数据隔离：如果是店铺管理员，验证商品是否属于其店铺
            Long currentStoreId = null;
            if (adminContext != null && token != null) {
                currentStoreId = adminContext.getCurrentStoreId(token);
                if (currentStoreId != null) {
                    // 验证商品是否属于该店铺
                    Product existingProduct = productService.getProductById(product.getId());
                    if (existingProduct == null || !currentStoreId.equals(existingProduct.getStoreId())) {
                        return Result.error("无权修改此商品");
                    }
                    // 确保更新后的商品仍然属于该店铺
                    product.setStoreId(currentStoreId);
                }
            }

            log.debug("收到更新商品请求 - id: {}, specs: {}, services: {}", product.getId(), product.getSpecs(), product.getServices());

            // 更新商品信息
            Product updatedProduct = productService.updateProduct(product);
            if (updatedProduct != null) {
                log.debug("商品更新成功 - id: {}, specs: {}, services: {}", updatedProduct.getId(), updatedProduct.getSpecs(), updatedProduct.getServices());
                return Result.success(updatedProduct);
            } else {
                return Result.error("更新商品失败");
            }
        } catch (Exception e) {
            log.error("更新商品失败", e);
            return Result.error("更新商品失败，请稍后重试");
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除商品")
    public Result<Boolean> deleteProduct(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            // 数据隔离：如果是店铺管理员，验证商品是否属于其店铺
            Long currentStoreId = null;
            if (adminContext != null && token != null) {
                currentStoreId = adminContext.getCurrentStoreId(token);
                if (currentStoreId != null) {
                    // 验证商品是否属于该店铺
                    Product product = productService.getProductById(id);
                    if (product == null || !currentStoreId.equals(product.getStoreId())) {
                        return Result.error("无权删除此商品");
                    }
                }
            }
            
            boolean success = productService.deleteProduct(id);
            return Result.success(success);
        } catch (Exception e) {
            log.error("删除商品失败", e);
            return Result.error("删除商品失败，请稍后重试");
        }
    }

    @GetMapping("/hot")
    @Operation(summary = "获取热门商品")
    public Result<java.util.List<Product>> getHotProducts(@RequestParam(defaultValue = "10") Integer limit) {
        List<Product> products = productService.getHotProducts(limit);
        return Result.success(products);
    }

    @GetMapping("/recommend")
    @Operation(summary = "获取推荐商品")
    public Result<java.util.List<Product>> getRecommendProducts(@RequestParam(defaultValue = "4") Integer limit) {
        List<Product> products = productService.getRecommendProducts(limit);
        return Result.success(products);
    }

    @GetMapping("/favorites/count")
    @Operation(summary = "获取用户商品收藏数量")
    public Result<Integer> getFavoriteCount(@RequestParam Long userId) {

        return Result.success(0);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取商品详情")
    public Result<Product> getProductDetail(@PathVariable Long id) {
        try {
            Product product = productService.getProductById(id);
            if (product == null) {
                return Result.error("商品不存在");
            }
            // 根据 specs 生成 skuStockList，使前端规格选择时价格/库存/图随规格变化（京东/拼多多逻辑）
            product.setSkuStockList(buildSkuStockList(product));
            return Result.success(product);
        } catch (Exception e) {
            log.error("获取商品详情失败", e);
            return Result.error("获取商品详情失败，请稍后重试");
        }
    }

    /**
     * 根据 product.specs 生成 skuStockList，供前端规格弹窗匹配当前选中规格的价格、库存、图片。
     * 支持格式：1) specs 为 JSON 对象且含 "skus" 数组，直接使用；2) specs 为规格组数组，按笛卡尔积生成 SKU。
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> buildSkuStockList(Product product) {
        List<Map<String, Object>> list = new ArrayList<>();
        BigDecimal defaultPrice = product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO;
        Integer defaultStock = product.getStock() != null ? product.getStock() : 0;
        String defaultPic = product.getImage();

        String specsStr = product.getSpecs();
        if (specsStr == null || specsStr.trim().isEmpty()) {
            Map<String, Object> one = new LinkedHashMap<>();
            one.put("id", 1);
            one.put("spData", "[]");
            one.put("price", defaultPrice);
            one.put("stock", defaultStock);
            one.put("pic", defaultPic);
            list.add(one);
            return list;
        }

        try {
            JsonNode parsed = JSONUtil.parseTree(specsStr);
            if (parsed != null && parsed.isObject()) {
                JsonNode skusNode = parsed.get("skus");
                if (skusNode != null && skusNode.isArray()) {
                    for (int i = 0; i < skusNode.size(); i++) {
                        JsonNode s = skusNode.get(i);
                        Map<String, Object> item = new LinkedHashMap<>();
                        if (s.isObject()) {
                            Map<String, Object> sm = JSONUtil.getMapper().convertValue(s, Map.class);
                            item.put("id", sm.get("id") != null ? sm.get("id") : (i + 1));
                            item.put("spData", sm.get("spData") != null ? sm.get("spData") : "[]");
                            item.put("price", sm.get("price") != null ? sm.get("price") : defaultPrice);
                            item.put("stock", sm.get("stock") != null ? sm.get("stock") : defaultStock);
                            item.put("pic", sm.get("pic") != null ? sm.get("pic") : defaultPic);
                            if (sm.containsKey("promotionPrice")) item.put("promotionPrice", sm.get("promotionPrice"));
                        } else {
                            item.put("id", i + 1);
                            item.put("spData", "[]");
                            item.put("price", defaultPrice);
                            item.put("stock", defaultStock);
                            item.put("pic", defaultPic);
                        }
                        list.add(item);
                    }
                    return list;
                }
            }

            if (parsed != null && parsed.isArray()) {
                ArrayNode groups = (ArrayNode) parsed;
                if (groups.size() == 0) {
                    Map<String, Object> one = new LinkedHashMap<>();
                    one.put("id", 1);
                    one.put("spData", "[]");
                    one.put("price", defaultPrice);
                    one.put("stock", defaultStock);
                    one.put("pic", defaultPic);
                    list.add(one);
                    return list;
                }
                // 笛卡尔积：每组选一个 value，生成所有组合
                List<List<Map<String, String>>> combinations = new ArrayList<>();
                for (int g = 0; g < groups.size(); g++) {
                    JsonNode go = groups.get(g);
                    String gName = JSONUtil.getText(go, "name");
                    if (gName == null) gName = "规格";
                    JsonNode vals = go.isObject() ? go.get("values") : null;
                    List<Map<String, String>> row = new ArrayList<>();
                    if (vals != null && vals.isArray()) {
                        ArrayNode arr = (ArrayNode) vals;
                        for (int v = 0; v < arr.size(); v++) {
                            JsonNode vo = arr.get(v);
                            Map<String, String> entry = new LinkedHashMap<>();
                            String name; BigDecimal price = null; String pic = null;
                            if (vo.isObject()) {
                                name = JSONUtil.getText(vo, "name");
                                JsonNode priceNode = vo.get("price");
                                if (priceNode != null && !priceNode.isNull()) {
                                    price = priceNode.isNumber() ? priceNode.decimalValue()
                                            : new BigDecimal(priceNode.asText());
                                }
                                if (JSONUtil.getText(vo, "image") != null) pic = JSONUtil.getText(vo, "image");
                                else if (JSONUtil.getText(vo, "pic") != null) pic = JSONUtil.getText(vo, "pic");
                            } else {
                                name = vo != null ? vo.asText() : "";
                            }
                            entry.put("key", gName);
                            entry.put("value", name);
                            if (price != null) entry.put("_price", price.toString());
                            if (pic != null) entry.put("_pic", pic);
                            row.add(entry);
                        }
                    } else if (vals != null && vals.isTextual()) {
                        // 兼容 values 被存成「逗号/顿号/空格」分隔的单个字符串
                        String s = vals.asText().trim();
                        if (!s.isEmpty()) {
                            for (String part : s.split("[,，、\\s]+")) {
                                if (part == null) {
                                    continue;
                                }
                                String name = part.trim();
                                if (name.isEmpty()) {
                                    continue;
                                }
                                Map<String, String> entry = new LinkedHashMap<>();
                                entry.put("key", gName);
                                entry.put("value", name);
                                row.add(entry);
                            }
                        }
                    }
                    if (!row.isEmpty()) combinations.add(row);
                }
                if (combinations.isEmpty()) {
                    Map<String, Object> one = new LinkedHashMap<>();
                    one.put("id", 1);
                    one.put("spData", "[]");
                    one.put("price", defaultPrice);
                    one.put("stock", defaultStock);
                    one.put("pic", defaultPic);
                    list.add(one);
                    return list;
                }
                // 笛卡尔积展开
                List<List<Map<String, String>>> result = new ArrayList<>();
                result.add(new ArrayList<>());
                for (List<Map<String, String>> row : combinations) {
                    List<List<Map<String, String>>> next = new ArrayList<>();
                    for (List<Map<String, String>> prefix : result) {
                        for (Map<String, String> choice : row) {
                            List<Map<String, String>> newCombo = new ArrayList<>(prefix);
                            newCombo.add(choice);
                            next.add(newCombo);
                        }
                    }
                    result = next;
                }
                for (int i = 0; i < result.size(); i++) {
                    List<Map<String, String>> combo = result.get(i);
                    List<Map<String, Object>> spDataList = new ArrayList<>();
                    BigDecimal price = defaultPrice;
                    String pic = defaultPic;
                    for (Map<String, String> e : combo) {
                        Map<String, Object> kv = new LinkedHashMap<>();
                        kv.put("key", e.get("key"));
                        kv.put("value", e.get("value"));
                        spDataList.add(kv);
                        if (e.containsKey("_price")) {
                            try { price = new BigDecimal(e.get("_price")); } catch (Exception ignore) {}
                        }
                        if (e.containsKey("_pic") && e.get("_pic") != null && !e.get("_pic").isEmpty()) {
                            pic = e.get("_pic");
                        }
                    }
                    Map<String, Object> sku = new LinkedHashMap<>();
                    sku.put("id", i + 1);
                    sku.put("spData", JSONUtil.toJSONString(spDataList));
                    sku.put("price", price);
                    sku.put("stock", defaultStock);
                    sku.put("pic", pic);
                    list.add(sku);
                }
                return list;
            }
        } catch (Exception e) {
            // 解析失败时返回默认单 SKU
        }
        Map<String, Object> one = new LinkedHashMap<>();
        one.put("id", 1);
        one.put("spData", "[]");
        one.put("price", defaultPrice);
        one.put("stock", defaultStock);
        one.put("pic", defaultPic);
        list.add(one);
        return list;
    }

    @GetMapping("/{id}/reviews")
    @Operation(summary = "获取商品评价")
    public Result<java.util.List<ProductReview>> getProductReviews(
            @PathVariable Long id,
            @RequestParam(required = false) Long userId) {
        try {
            List<ProductReview> reviews = productService.getProductReviews(id);
            
            // 如果传入了userId，设置每个评论的点赞状态
            if (userId != null && reviews != null) {
                for (ProductReview review : reviews) {
                    review.setIsLiked(productService.isReviewLiked(review.getId(), userId));
                }
            }
            
            return Result.success(reviews);
        } catch (Exception e) {
            log.error("获取商品评价失败", e);
            return Result.error("获取商品评价失败，请稍后重试");
        }
    }

    @GetMapping("/{id}/sales")
    @Operation(summary = "获取商品销售记录")
    public Result<java.util.List<java.util.Map<String, Object>>> getProductSalesRecords(@PathVariable Long id) {
        try {
            java.util.List<java.util.Map<String, Object>> salesRecords = productService.getProductSalesRecords(id);
            return Result.success(salesRecords);
        } catch (Exception e) {
            log.error("获取商品销售记录失败", e);
            return Result.error("获取商品销售记录失败，请稍后重试");
        }
    }

    @PostMapping("/{id}/reviews")
    @Operation(summary = "创建商品评价")
    public Result<ProductReview> createProductReview(@PathVariable Long id, @RequestBody ProductReview review,
                                                     HttpServletRequest request) {
        try {
            // 安全修复：评价人以登录态为准，忽略请求体中的 userId（防止替他人发评价）
            Long userId = currentUserId(request);
            if (userId == null) {
                return Result.error(401, "用户未登录或token无效");
            }
            review.setUserId(userId);
            log.debug("收到创建评价请求 - 商品ID: {}, 用户ID: {}, 评分: {}", id, userId, review.getRating());

            // 设置商品ID
            review.setProductId(id);

            // 设置默认状态为正常（1）
            if (review.getStatus() == null) {
                review.setStatus(1);
            }

            // 验证必填字段
            if (review.getRating() == null || review.getRating() < 1 || review.getRating() > 5) {
                return Result.error("评分必须在1-5之间");
            }
            if (review.getComment() == null || review.getComment().trim().isEmpty()) {
                return Result.error("评价内容不能为空");
            }

            // 创建评价
            ProductReview createdReview = productService.createProductReview(review);
            if (createdReview != null) {
                log.debug("创建评价成功 - 评价ID: {}", createdReview.getId());
                return Result.success(createdReview);
            } else {
                log.warn("创建评价失败 - 返回null");
                return Result.error("创建评价失败");
            }
        } catch (Exception e) {
            log.error("创建评价失败", e);
            return Result.error("创建评价失败，请稍后重试");
        }
    }
    
    // ========== 评论点赞相关接口 ==========
    
    @PostMapping("/review/{reviewId}/like")
    @Operation(summary = "点赞/取消点赞评论")
    public Result<java.util.Map<String, Object>> toggleReviewLike(
            @PathVariable Long reviewId,
            HttpServletRequest request) {
        try {
            // 安全修复：点赞人以登录态为准（原从 body 取 userId，可替他人点赞）
            Long userId = currentUserId(request);
            if (userId == null) {
                return Result.error(401, "用户未登录或token无效");
            }

            boolean isLiked = productService.toggleReviewLike(reviewId, userId);
            Integer likeCount = productService.getReviewLikeCount(reviewId);
            
            java.util.Map<String, Object> result = new java.util.HashMap<>();
            result.put("isLiked", isLiked);
            result.put("likeCount", likeCount);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("操作失败", e);
            return Result.error("操作失败，请稍后重试");
        }
    }
    
    @GetMapping("/review/{reviewId}/like/status")
    @Operation(summary = "获取评论点赞状态和数量")
    public Result<java.util.Map<String, Object>> getReviewLikeStatus(
            @PathVariable Long reviewId,
            @RequestParam(required = false) Long userId) {
        try {
            boolean isLiked = userId != null ? productService.isReviewLiked(reviewId, userId) : false;
            Integer likeCount = productService.getReviewLikeCount(reviewId);
            
            java.util.Map<String, Object> result = new java.util.HashMap<>();
            result.put("isLiked", isLiked);
            result.put("likeCount", likeCount);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取失败", e);
            return Result.error("获取失败，请稍后重试");
        }
    }
    
    // ========== 评论回复相关接口 ==========
    
    @PostMapping("/review/{reviewId}/reply")
    @Operation(summary = "回复评论")
    public Result<com.pethome.entity.ReviewReply> createReviewReply(
            @PathVariable Long reviewId,
            @RequestBody java.util.Map<String, Object> params,
            HttpServletRequest request) {
        try {
            // 安全修复：回复人以登录态为准（原从 body 取 userId，可替他人回复）
            Long userId = currentUserId(request);
            if (userId == null) {
                return Result.error(401, "用户未登录或token无效");
            }
            String content = params != null && params.get("content") != null ? params.get("content").toString() : null;

            if (content == null || content.trim().isEmpty()) {
                return Result.error("回复内容不能为空");
            }
            
            com.pethome.entity.ReviewReply reply = productService.createReviewReply(reviewId, userId, content);
            return Result.success(reply);
        } catch (Exception e) {
            log.error("回复失败", e);
            return Result.error("回复失败，请稍后重试");
        }
    }
    
    @GetMapping("/review/{reviewId}/replies")
    @Operation(summary = "获取评论的回复列表")
    public Result<java.util.List<com.pethome.entity.ReviewReply>> getReviewReplies(
            @PathVariable Long reviewId) {
        try {
            java.util.List<com.pethome.entity.ReviewReply> replies = productService.getReviewReplies(reviewId);
            return Result.success(replies);
        } catch (Exception e) {
            log.error("获取回复列表失败", e);
            return Result.error("获取回复列表失败，请稍后重试");
        }
    }

    @PostMapping("/{id}/collect")
    @Operation(summary = "收藏商品")
    public Result<Boolean> collectProduct(@PathVariable Long id, HttpServletRequest request) {
        try {
            // 安全修复：收藏人以登录态为准（原从 query 取 userId，可操作他人收藏）
            Long userId = currentUserId(request);
            if (userId == null) {
                return Result.error(401, "用户未登录或token无效");
            }
            boolean success = productFavoriteService.collectProduct(id, userId);
            if (success) {
                return Result.success(true);
            } else {
                return Result.error("已经收藏过了");
            }
        } catch (Exception e) {
            log.error("收藏失败", e);
            return Result.error("收藏失败，请稍后重试");
        }
    }

    @DeleteMapping("/{id}/collect")
    @Operation(summary = "取消收藏商品")
    public Result<Boolean> uncollectProduct(@PathVariable Long id, HttpServletRequest request) {
        try {
            // 安全修复：操作人以登录态为准
            Long userId = currentUserId(request);
            if (userId == null) {
                return Result.error(401, "用户未登录或token无效");
            }
            boolean success = productFavoriteService.uncollectProduct(id, userId);
            if (success) {
                return Result.success(true);
            } else {
                return Result.error("没有收藏记录");
            }
        } catch (Exception e) {
            log.error("取消收藏失败", e);
            return Result.error("取消收藏失败，请稍后重试");
        }
    }

    @GetMapping("/{id}/collect/status")
    @Operation(summary = "检查商品收藏状态")
    public Result<Boolean> checkProductCollectStatus(@PathVariable Long id, HttpServletRequest request) {
        try {
            // 安全修复：收藏状态属个人数据，以登录态为准
            Long userId = currentUserId(request);
            if (userId == null) {
                return Result.error(401, "用户未登录或token无效");
            }
            boolean isCollected = productFavoriteService.isCollected(id, userId);
            return Result.success(isCollected);
        } catch (Exception e) {
            log.error("检查收藏状态失败", e);
            return Result.error("检查收藏状态失败，请稍后重试");
        }
    }

    @GetMapping("/collected")
    @Operation(summary = "获取用户收藏的商品列表")
    public Result<java.util.Map<String, Object>> getCollectedProducts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        try {
            // 安全修复：收藏列表属个人数据，以登录态为准（原从 query 取 userId，可越权查看他人收藏）
            Long userId = currentUserId(request);
            if (userId == null) {
                return Result.error(401, "用户未登录或token无效");
            }
            Page<Product> productPage = new Page<>(page, size);
            Page<Product> result = productFavoriteService.getCollectedProducts(userId, productPage);
            
            // 构建返回格式
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("products", result.getRecords());
            response.put("list", result.getRecords());
            response.put("data", result.getRecords());
            response.put("total", result.getTotal());
            response.put("size", result.getSize());
            response.put("current", result.getCurrent());
            response.put("pages", result.getPages());
            
            return Result.success(response);
        } catch (Exception e) {
            log.error("获取收藏列表失败", e);
            return Result.error("获取收藏列表失败，请稍后重试");
        }
    }
}

