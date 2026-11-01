package com.pethome.service.order.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.Product;
import com.pethome.entity.ProductReview;
import com.pethome.mapper.ProductMapper;
import com.pethome.service.order.ProductService;
import com.pethome.service.order.support.ProductReviewCommandService;
import com.pethome.service.order.support.ProductReviewQueryService;
import com.pethome.service.order.support.ProductViewEnricher;
import com.pethome.util.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class ProductServiceImpl implements ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

    // Redis 缓存 key
    private static final String PRODUCT_LIST_KEY = "product:list";
    private static final String PRODUCT_PREFIX = "product:";
    // 缓存过期时间（分钟）
    private static final long PRODUCT_CACHE_EXPIRE_MINUTES = 30;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private com.pethome.mapper.CategoryMapper categoryMapper;

    @Autowired
    private com.pethome.mapper.StoreMapper storeMapper;

    @Autowired(required = false)
    private com.pethome.mapper.OrderItemMapper orderItemMapper;

    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ProductViewEnricher productViewEnricher;

    @Autowired
    private ProductReviewQueryService productReviewQueryService;

    @Autowired
    private ProductReviewCommandService productReviewCommandService;

    @Override
    public List<Product> getAllProducts() {
        return getAllProducts(null);
    }

    @Override
    public List<Product> getAllProducts(Long storeId) {
        List<Product> products = null;

        // 优先从 Redis 缓存获取
        if (stringRedisTemplate != null) {
            try {
                String json = stringRedisTemplate.opsForValue().get(PRODUCT_LIST_KEY);
                if (StringUtils.hasText(json)) {
                    logger.info("从 Redis 缓存获取商品列表，key: {}", PRODUCT_LIST_KEY);
                    products = JSONUtil.parseArray(json, Product.class);
                }
            } catch (Exception e) {
                logger.warn("从 Redis 获取商品列表失败，降级到数据库查询，错误: {}", e.getMessage());
            }
        }

        // 如果缓存中没有，从数据库查询
        if (products == null || products.isEmpty()) {
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        // 如果指定了店铺ID，按店铺过滤
        if (storeId != null) {
            queryWrapper.eq("store_id", storeId);
        }
        queryWrapper.orderByDesc("create_time"); // 按创建时间排序，返回所有商品
            products = productMapper.selectList(queryWrapper);

            // 写入 Redis 缓存（只有在从数据库查询时才写入）
        if (products != null && !products.isEmpty() && stringRedisTemplate != null) {
            try {
                String jsonValue = JSONUtil.toJSONString(products);
                stringRedisTemplate.opsForValue().set(PRODUCT_LIST_KEY, jsonValue, PRODUCT_CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
                logger.info("成功写入商品列表到 Redis 缓存，key: {}, 商品数量: {}, 过期时间: {} 分钟", PRODUCT_LIST_KEY, products.size(), PRODUCT_CACHE_EXPIRE_MINUTES);
            } catch (Exception e) {
                logger.error("写入商品列表到 Redis 缓存失败，错误: {}", e.getMessage(), e);
            }
        }
        }

        // 无论从缓存还是数据库，都需要设置店铺名称和销量（因为店铺信息和销量可能变化）
        productViewEnricher.fillStoreAndSale(products);

        return products != null ? products : List.of();
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
        List<Product> products = productMapper.selectList(queryWrapper);

        // 批量设置销量（与getAllProducts一致），未命中销量的商品兜底为 0
        Map<Long, Integer> saleMap = productViewEnricher.countSalesByProductIds(products != null ? products : List.of());
        if (products != null) {
            for (Product product : products) {
                Integer sale = saleMap.get(product.getId());
                product.setSale(sale != null ? sale : 0);
            }
        }

        return products != null ? products : new java.util.ArrayList<>();
    }

    @Override
    public IPage<Product> getProductPage(Page<Product> page) {
        return getProductPage(page, null);
    }

    @Override
    public IPage<Product> getProductPage(Page<Product> page, Long storeId) {
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        // 如果指定了店铺ID，按店铺过滤
        if (storeId != null) {
            queryWrapper.eq("store_id", storeId);
        }
        queryWrapper.orderByDesc("create_time");
        return productMapper.selectPage(page, queryWrapper);
    }

    @Override
    public Product createProduct(Product product) {
        product.setCreateTime(LocalDateTime.now());
        product.setUpdateTime(LocalDateTime.now());
        productMapper.insert(product);

        // 清除商品列表缓存（新商品加入需要刷新列表）
        clearProductCache(null);
        logger.info("商品创建成功，已清除缓存，productId: {}", product.getId());

        return product;
    }

    @Override
    public Product updateProduct(Product product) {
        product.setUpdateTime(LocalDateTime.now());

        // 记录更新前的数据，用于调试
        logger.info("更新商品 - productId: {}, specs: {}, services: {}",
            product.getId(), product.getSpecs(), product.getServices());

        // 更新商品信息
        int result = productMapper.updateById(product);
        logger.info("更新商品 - 数据库更新结果: {}, productId: {}", result, product.getId());

        // 重新从数据库查询，确保获取最新数据
        Product updatedProduct = productMapper.selectById(product.getId());
        logger.info("更新商品 - 重新查询后的数据, productId: {}, specs: {}, services: {}",
            updatedProduct != null ? updatedProduct.getId() : null,
            updatedProduct != null ? updatedProduct.getSpecs() : null,
            updatedProduct != null ? updatedProduct.getServices() : null);

        // 清除相关缓存
        clearProductCache(product.getId());
        logger.info("商品更新成功，已清除缓存，productId: {}", product.getId());

        return updatedProduct != null ? updatedProduct : product;
    }

    @Override
    public boolean deleteProduct(Long id) {
        boolean result = productMapper.deleteById(id) > 0;

        if (result) {
            // 清除相关缓存
            clearProductCache(id);
            logger.info("商品删除成功，已清除缓存，productId: {}", id);
        }

        return result;
    }

    @Override
    public Product getProductById(Long id) {
        if (id == null) {
            return null;
        }

        String cacheKey = PRODUCT_PREFIX + id;

        // 优先从 Redis 缓存获取
        if (stringRedisTemplate != null) {
            try {
                String json = stringRedisTemplate.opsForValue().get(cacheKey);
                if (StringUtils.hasText(json)) {
                    logger.info("从 Redis 缓存获取商品详情，productId: {}, key: {}", id, cacheKey);
                    return JSONUtil.parseObject(json, Product.class);
                }
            } catch (Exception e) {
                logger.warn("从 Redis 获取商品详情失败，降级到数据库查询，productId: {}, 错误: {}", id, e.getMessage());
            }
        }

        // 从数据库查询
        Product product = productMapper.selectById(id);

        // 设置销量（通过订单项表统计该商品的实际销售数量）
        if (product != null && orderItemMapper != null) {
            try {
                // 直接统计该商品的所有订单项数量（简化处理，统计所有状态的订单）
                QueryWrapper<com.pethome.entity.OrderItem> itemQuery = new QueryWrapper<>();
                itemQuery.eq("product_id", product.getId());
                Long saleCount = orderItemMapper.selectCount(itemQuery);

                product.setSale(saleCount != null && saleCount > 0 ? saleCount.intValue() : 0);
                logger.info("商品销量统计 - 商品ID: {}, 销量: {}", product.getId(), product.getSale());
            } catch (Exception e) {
                logger.warn("统计商品销量失败: " + e.getMessage(), e);
                product.setSale(0);
            }
        } else if (product != null) {
            product.setSale(0);
        }

        // 设置店铺信息
        if (product != null) {
            try {
                com.pethome.entity.Store store = null;
                // 如果商品有storeId，根据storeId加载店铺
                if (product.getStoreId() != null) {
                    store = storeMapper.selectById(product.getStoreId());
                }

                // 如果没有找到店铺，使用默认店铺（ID为1的店铺，或者第一个启用的店铺）
                if (store == null) {
                    QueryWrapper<com.pethome.entity.Store> storeQuery = new QueryWrapper<>();
                    storeQuery.eq("status", 1).last("limit 1");
                    store = storeMapper.selectOne(storeQuery);
                }

                if (store != null) {
                    product.setStoreInfo(store);
                    // 设置店铺名称（用于前端显示）
                    product.setStoreName(store.getName());
                    // 设置店铺头像和logo，URL转换在Controller层处理
                    product.setStoreAvatar(store.getAvatar());
                    product.setStoreLogo(store.getLogo() != null ? store.getLogo() : store.getAvatar());
                } else {
                    // 如果没有店铺，设置默认店铺信息
                    com.pethome.entity.Store defaultStore = new com.pethome.entity.Store();
                    defaultStore.setId(1L);
                    defaultStore.setName("宠物家商品专卖店");
                    defaultStore.setAvatar(productViewEnricher.defaultStoreImageUrl());
                    defaultStore.setLogo(productViewEnricher.defaultStoreImageUrl());
                    product.setStoreInfo(defaultStore);
                    product.setStoreAvatar(defaultStore.getAvatar());
                    product.setStoreLogo(defaultStore.getLogo());
                }
            } catch (Exception e) {
                logger.warn("加载店铺信息失败: " + e.getMessage());
                // 设置默认店铺信息
                com.pethome.entity.Store defaultStore = new com.pethome.entity.Store();
                defaultStore.setId(1L);
                defaultStore.setName("宠物家商品专卖店");
                defaultStore.setAvatar(productViewEnricher.defaultStoreImageUrl());
                defaultStore.setLogo(productViewEnricher.defaultStoreImageUrl());
                product.setStoreInfo(defaultStore);
                product.setStoreAvatar(defaultStore.getAvatar());
                product.setStoreLogo(defaultStore.getLogo());
            }
        }

        // 写入 Redis 缓存
        if (product != null && stringRedisTemplate != null) {
            try {
                String jsonValue = JSONUtil.toJSONString(product);
                stringRedisTemplate.opsForValue().set(cacheKey, jsonValue, PRODUCT_CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
                logger.info("成功写入商品详情到 Redis 缓存，productId: {}, key: {}, 过期时间: {} 分钟", id, cacheKey, PRODUCT_CACHE_EXPIRE_MINUTES);
            } catch (Exception e) {
                logger.error("写入商品详情到 Redis 缓存失败，productId: {}, 错误: {}", id, e.getMessage(), e);
            }
        }

        return product;
    }

    @Override
    public List<Product> listProductsByIds(java.util.Collection<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return productMapper.selectBatchIds(productIds);
    }

    /**
     * 清除商品相关缓存
     */
    private void clearProductCache(Long productId) {
        if (stringRedisTemplate == null) {
            return;
        }

        try {
            // 清除商品列表缓存
            stringRedisTemplate.delete(PRODUCT_LIST_KEY);
            logger.info("成功清除商品列表缓存，key: {}", PRODUCT_LIST_KEY);

            // 清除单个商品缓存
            if (productId != null) {
                String cacheKey = PRODUCT_PREFIX + productId;
                stringRedisTemplate.delete(cacheKey);
                logger.info("成功清除商品详情缓存，productId: {}, key: {}", productId, cacheKey);
            }
        } catch (Exception e) {
            logger.error("清除商品缓存失败，productId: {}, 错误: {}", productId, e.getMessage(), e);
        }
    }

    @Override
    public List<Product> getHotProducts(Integer limit) {
        // 根据 is_hot 标记获取管理员设置的热门推荐商品
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1) // 只查询在售商品（状态为1）
                .eq("is_hot", 1) // 只查询热门推荐商品
                .ne("category", "积分商城") // 排除积分商城商品
                .orderByDesc("create_time")
                .last("limit " + limit);

        return productMapper.selectList(queryWrapper);
    }

    @Override
    public boolean setProductHot(Long productId, Boolean isHot) {
        if (productId == null) return false;
        Product product = productMapper.selectById(productId);
        if (product == null) return false;
        product.setIsHot(Boolean.TRUE.equals(isHot));
        product.setUpdateTime(LocalDateTime.now());
        boolean ok = productMapper.updateById(product) > 0;
        if (ok) clearProductCache(null);
        return ok;
    }

    @Override
    public boolean batchSetHotProducts(List<Long> productIds, Boolean isHot) {
        if (productIds == null || productIds.isEmpty()) return false;
        boolean ok = true;
        for (Long id : productIds) {
            ok = setProductHot(id, isHot) && ok;
        }
        return ok;
    }

    @Override
    public List<Product> getRecommendProducts(Integer limit) {
        // 随机推荐商品（简化版，实际应该基于用户行为分析）
        // 排除积分商城商品（category != "积分商城"）
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1) // 只查询在售商品（状态为1，修复之前的"onsale"错误）
                .ne("category", "积分商城") // 排除积分商城商品
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

    @Override
    public List<ProductReview> getProductReviews(Long productId) {
        return productReviewQueryService.getProductReviews(productId);
    }

    @Override
    public ProductReview createProductReview(ProductReview review) {
        return productReviewCommandService.createProductReview(review);
    }

    @Override
    public java.util.List<java.util.Map<String, Object>> getProductSalesRecords(Long productId) {
        return productReviewQueryService.getProductSalesRecords(productId);
    }

    @Override
    public boolean toggleReviewLike(Long reviewId, Long userId) {
        return productReviewCommandService.toggleReviewLike(reviewId, userId);
    }

    @Override
    public boolean isReviewLiked(Long reviewId, Long userId) {
        return productReviewQueryService.isReviewLiked(reviewId, userId);
    }

    @Override
    public Integer getReviewLikeCount(Long reviewId) {
        return productReviewQueryService.getReviewLikeCount(reviewId);
    }

    @Override
    public com.pethome.entity.ReviewReply createReviewReply(Long reviewId, Long userId, String content) {
        return productReviewCommandService.createReviewReply(reviewId, userId, content);
    }

    @Override
    public java.util.List<com.pethome.entity.ReviewReply> getReviewReplies(Long reviewId) {
        return productReviewQueryService.getReviewReplies(reviewId);
    }

    @Override
    public Integer getReviewReplyCount(Long reviewId) {
        return productReviewQueryService.getReviewReplyCount(reviewId);
    }
}
