package com.pethome.service.order.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.pethome.common.BusinessException;
import com.pethome.entity.Product;
import com.pethome.entity.ProductReview;
import com.pethome.entity.ReviewReply;
import com.pethome.service.order.ProductFavoriteService;
import com.pethome.service.order.ProductQueryAppService;
import com.pethome.service.order.ProductService;
import com.pethome.util.JSONUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商品查询应用服务实现。
 * <p>
 * 原 ProductController 的列表过滤、店铺隔离、评价/收藏视图组装与 skuStockList 生成逻辑下沉至此。
 */
@Service
public class ProductQueryAppServiceImpl implements ProductQueryAppService {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductFavoriteService productFavoriteService;

    @Override
    public Map<String, Object> getProductList(Integer categoryId, String excludeCategory, Integer page,
                                              Integer size, Long currentStoreId) {
        List<Product> products;
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
                    .collect(Collectors.toList());
        }

        // 数据隔离：如果是店铺管理员，只返回自己店铺的商品
        if (currentStoreId != null) {
            final Long storeId = currentStoreId;
            products = products.stream()
                    .filter(p -> p.getStoreId() != null && p.getStoreId().equals(storeId))
                    .collect(Collectors.toList());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("goods", products);  // 小程序需要goods字段
        result.put("records", products);
        result.put("total", products.size());
        result.put("size", size);
        result.put("current", page);
        result.put("pages", (products.size() + size - 1) / size);
        return result;
    }

    @Override
    public IPage<Product> getProductPage(Integer pageNo, Integer pageSize, Long storeId, Long currentStoreId) {
        Page<Product> page = new Page<>(pageNo, pageSize);
        // 店铺管理员只能查看自己店铺的商品，忽略传入的storeId参数
        Long finalStoreId = currentStoreId != null ? currentStoreId : storeId;
        return productService.getProductPage(page, finalStoreId);
    }

    @Override
    public List<Product> getHotProducts(Integer limit) {
        return productService.getHotProducts(limit);
    }

    @Override
    public List<Product> getRecommendProducts(Integer limit) {
        return productService.getRecommendProducts(limit);
    }

    @Override
    public Product getProductDetail(Long productId) {
        Product product = productService.getProductById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        // 根据 specs 生成 skuStockList，使前端规格选择时价格/库存/图随规格变化（京东/拼多多逻辑）
        product.setSkuStockList(buildSkuStockList(product));
        return product;
    }

    @Override
    public List<ProductReview> getProductReviews(Long productId, Long userId) {
        List<ProductReview> reviews = productService.getProductReviews(productId);
        // 如果传入了userId，设置每个评论的点赞状态
        if (userId != null && reviews != null) {
            for (ProductReview review : reviews) {
                review.setIsLiked(productService.isReviewLiked(review.getId(), userId));
            }
        }
        return reviews;
    }

    @Override
    public List<Map<String, Object>> getProductSalesRecords(Long productId) {
        return productService.getProductSalesRecords(productId);
    }

    @Override
    public Map<String, Object> getReviewLikeStatus(Long reviewId, Long userId) {
        boolean isLiked = userId != null && productService.isReviewLiked(reviewId, userId);
        Integer likeCount = productService.getReviewLikeCount(reviewId);

        Map<String, Object> result = new HashMap<>();
        result.put("isLiked", isLiked);
        result.put("likeCount", likeCount);
        return result;
    }

    @Override
    public List<ReviewReply> getReviewReplies(Long reviewId) {
        return productService.getReviewReplies(reviewId);
    }

    @Override
    public boolean isCollected(Long productId, Long userId) {
        // 安全修复：收藏状态属个人数据，以登录态为准
        if (userId == null) {
            throw new BusinessException(401, "用户未登录或token无效");
        }
        return productFavoriteService.isCollected(productId, userId);
    }

    @Override
    public Map<String, Object> getCollectedProducts(Long userId, Integer page, Integer size) {
        // 安全修复：收藏列表属个人数据，以登录态为准
        if (userId == null) {
            throw new BusinessException(401, "用户未登录或token无效");
        }
        Page<Product> productPage = new Page<>(page, size);
        Page<Product> result = productFavoriteService.getCollectedProducts(userId, productPage);

        Map<String, Object> response = new HashMap<>();
        response.put("products", result.getRecords());
        response.put("list", result.getRecords());
        response.put("data", result.getRecords());
        response.put("total", result.getTotal());
        response.put("size", result.getSize());
        response.put("current", result.getCurrent());
        response.put("pages", result.getPages());
        return response;
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
                            if (sm.containsKey("promotionPrice")) {
                                item.put("promotionPrice", sm.get("promotionPrice"));
                            }
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
                    if (gName == null) {
                        gName = "规格";
                    }
                    JsonNode vals = go.isObject() ? go.get("values") : null;
                    List<Map<String, String>> row = new ArrayList<>();
                    if (vals != null && vals.isArray()) {
                        ArrayNode arr = (ArrayNode) vals;
                        for (int v = 0; v < arr.size(); v++) {
                            JsonNode vo = arr.get(v);
                            Map<String, String> entry = new LinkedHashMap<>();
                            String name;
                            BigDecimal price = null;
                            String pic = null;
                            if (vo.isObject()) {
                                name = JSONUtil.getText(vo, "name");
                                JsonNode priceNode = vo.get("price");
                                if (priceNode != null && !priceNode.isNull()) {
                                    price = priceNode.isNumber() ? priceNode.decimalValue()
                                            : new BigDecimal(priceNode.asText());
                                }
                                if (JSONUtil.getText(vo, "image") != null) {
                                    pic = JSONUtil.getText(vo, "image");
                                } else if (JSONUtil.getText(vo, "pic") != null) {
                                    pic = JSONUtil.getText(vo, "pic");
                                }
                            } else {
                                name = vo != null ? vo.asText() : "";
                            }
                            entry.put("key", gName);
                            entry.put("value", name);
                            if (price != null) {
                                entry.put("_price", price.toString());
                            }
                            if (pic != null) {
                                entry.put("_pic", pic);
                            }
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
                    if (!row.isEmpty()) {
                        combinations.add(row);
                    }
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
                            try {
                                price = new BigDecimal(e.get("_price"));
                            } catch (Exception ignore) {
                                // 单个规格价格解析失败时沿用默认价
                            }
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
}
