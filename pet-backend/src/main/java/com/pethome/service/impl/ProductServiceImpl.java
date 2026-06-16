package com.pethome.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.Product;
import com.pethome.entity.ProductReview;
import com.pethome.mapper.ProductMapper;
import com.pethome.mapper.ProductReviewMapper;
import com.pethome.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
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
    private ProductReviewMapper productReviewMapper;
    
    @Autowired
    private com.pethome.mapper.UserMapper userMapper;
    
    @Autowired
    private com.pethome.mapper.StoreMapper storeMapper;
    
    @Autowired(required = false)
    private com.pethome.mapper.OrderMapper orderMapper;
    
    @Autowired(required = false)
    private com.pethome.mapper.OrderItemMapper orderItemMapper;
    
    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;
    
    @Autowired(required = false)
    private com.pethome.mapper.ReviewLikeMapper reviewLikeMapper;
    
    @Autowired(required = false)
    private com.pethome.mapper.ReviewReplyMapper reviewReplyMapper;

    @Value("${app.public-base-url:}")
    private String publicBaseUrl;

    @Value("${upload.http-url:http://localhost:8080/upload/}")
    private String uploadHttpUrl;

    /** 与 BannerController 等一致：优先 app.public-base-url，否则从 upload.http-url 推导站点根 */
    private String publicSiteOrigin() {
        if (publicBaseUrl != null && !publicBaseUrl.trim().isEmpty()) {
            return publicBaseUrl.trim().replaceAll("/+$", "");
        }
        String u = uploadHttpUrl != null ? uploadHttpUrl.trim() : "http://localhost:8080/upload/";
        if (!u.endsWith("/")) {
            u += "/";
        }
        if (u.toLowerCase().endsWith("upload/")) {
            u = u.substring(0, u.length() - "upload/".length());
        }
        return u.replaceAll("/+$", "");
    }

    private String defaultStoreImageUrl() {
        return publicSiteOrigin() + "/upload/store/store-20260117001700-cb17d3ce.png";
    }

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
                    products = JSON.parseObject(json, new TypeReference<List<Product>>() {});
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
                String jsonValue = JSON.toJSONString(products);
                stringRedisTemplate.opsForValue().set(PRODUCT_LIST_KEY, jsonValue, PRODUCT_CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
                logger.info("成功写入商品列表到 Redis 缓存，key: {}, 商品数量: {}, 过期时间: {} 分钟", PRODUCT_LIST_KEY, products.size(), PRODUCT_CACHE_EXPIRE_MINUTES);
            } catch (Exception e) {
                logger.error("写入商品列表到 Redis 缓存失败，错误: {}", e.getMessage(), e);
            }
        }
        }
        
        // 无论从缓存还是数据库，都需要设置店铺名称和销量（因为店铺信息和销量可能变化）
        if (products != null && !products.isEmpty()) {
            for (Product product : products) {
                // 设置店铺名称
                if (storeMapper != null && product.getStoreId() != null) {
                    try {
                        com.pethome.entity.Store store = storeMapper.selectById(product.getStoreId());
                        if (store != null) {
                            product.setStoreName(store.getName());
                        } else {
                            product.setStoreName(null); // 店铺不存在，清空店铺名称
                        }
                    } catch (Exception e) {
                        logger.warn("加载商品店铺信息失败 - 商品ID: {}, 店铺ID: {}, 错误: {}", 
                            product.getId(), product.getStoreId(), e.getMessage());
                        product.setStoreName(null);
                    }
                } else if (product.getStoreId() == null) {
                    product.setStoreName(null); // 没有店铺ID，清空店铺名称
                }
                
                // 设置销量（通过订单项表统计该商品的实际销售数量）
                if (orderItemMapper != null) {
                    try {
                        // 直接统计该商品的所有订单项数量（简化处理，统计所有状态的订单）
                        QueryWrapper<com.pethome.entity.OrderItem> itemQuery = new QueryWrapper<>();
                        itemQuery.eq("product_id", product.getId());
                        Long saleCount = orderItemMapper.selectCount(itemQuery);
                        
                        product.setSale(saleCount != null && saleCount > 0 ? saleCount.intValue() : 0);
                    } catch (Exception e) {
                        logger.warn("统计商品销量失败 - 商品ID: {}, 错误: {}", product.getId(), e.getMessage());
                        product.setSale(0);
                    }
                } else {
                    product.setSale(0);
                }
            }
        }
        
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
        
        // 为每个商品设置销量（与getAllProducts一致）
        if (products != null && !products.isEmpty() && orderItemMapper != null) {
            for (Product product : products) {
                try {
                    QueryWrapper<com.pethome.entity.OrderItem> itemQuery = new QueryWrapper<>();
                    itemQuery.eq("product_id", product.getId());
                    Long saleCount = orderItemMapper.selectCount(itemQuery);
                    product.setSale(saleCount != null && saleCount > 0 ? saleCount.intValue() : 0);
                } catch (Exception e) {
                    logger.warn("统计商品销量失败 - 商品ID: {}, 错误: {}", product.getId(), e.getMessage());
                    product.setSale(0);
                }
            }
        } else if (products != null) {
            for (Product product : products) {
                if (product.getSale() == null) product.setSale(0);
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
                    return JSON.parseObject(json, Product.class);
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
                    defaultStore.setAvatar(defaultStoreImageUrl());
                    defaultStore.setLogo(defaultStoreImageUrl());
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
                defaultStore.setAvatar(defaultStoreImageUrl());
                defaultStore.setLogo(defaultStoreImageUrl());
                product.setStoreInfo(defaultStore);
                product.setStoreAvatar(defaultStore.getAvatar());
                product.setStoreLogo(defaultStore.getLogo());
            }
        }
        
        // 写入 Redis 缓存
        if (product != null && stringRedisTemplate != null) {
            try {
                String jsonValue = JSON.toJSONString(product);
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
        QueryWrapper<ProductReview> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("product_id", productId);
        queryWrapper.eq("status", 1); // 只返回正常状态的评价
        queryWrapper.orderByDesc("create_time");
        
        List<ProductReview> reviews = productReviewMapper.selectList(queryWrapper);
        
        // 为每个评价设置用户信息（昵称和头像），并过滤临时路径图片
        for (ProductReview review : reviews) {
            // 过滤临时路径图片
            if (review.getImages() != null && !review.getImages().isEmpty()) {
                String imagesStr = review.getImages();
                // 如果是临时路径，清空图片字段
                if (imagesStr.startsWith("http://tmp/") || imagesStr.startsWith("https://tmp/") || imagesStr.startsWith("tmp/")) {
                    review.setImages(null);
                } else if (imagesStr.contains(",")) {
                    // 如果是逗号分隔的字符串，过滤掉临时路径
                    List<String> imageList = java.util.Arrays.asList(imagesStr.split(","));
                    List<String> validImages = imageList.stream()
                        .map(String::trim)
                        .filter(img -> !img.isEmpty() 
                            && !img.startsWith("http://tmp/") 
                            && !img.startsWith("https://tmp/")
                            && !img.startsWith("tmp/"))
                        .collect(java.util.stream.Collectors.toList());
                    if (validImages.isEmpty()) {
                        review.setImages(null);
                    } else {
                        review.setImages(String.join(",", validImages));
                    }
                }
            }
            
            if (review.getUserId() != null) {
                try {
                    // 从用户表查询用户信息
                    com.pethome.entity.User user = userMapper.selectById(review.getUserId());
                    if (user != null) {
                        // 设置用户昵称（优先使用nickname，其次是username）
                        review.setUserName(user.getNickname() != null && !user.getNickname().isEmpty() 
                            ? user.getNickname() 
                            : (user.getUsername() != null ? user.getUsername() : "用户" + review.getUserId()));
                        // 设置用户头像
                        if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                            review.setUserAvatar(user.getAvatar());
                        } else {
                            review.setUserAvatar("/static/images/garfield-default-avatar.png");
                        }
                    } else {
                        // 用户不存在，使用默认值
                        review.setUserName("用户" + review.getUserId());
                        review.setUserAvatar("/static/images/garfield-default-avatar.png");
                    }
                } catch (Exception e) {
                    // 查询用户信息失败，使用默认值
                    logger.warn("查询评价用户信息失败 - 用户ID: " + review.getUserId() + ", 错误: " + e.getMessage());
                    review.setUserName("用户" + review.getUserId());
                    review.setUserAvatar("/static/images/garfield-default-avatar.png");
                }
            } else {
                review.setUserName("匿名用户");
                review.setUserAvatar("/static/images/garfield-default-avatar.png");
            }
            
            // 设置点赞数和回复数
            if (reviewLikeMapper != null) {
                review.setLikeCount(getReviewLikeCount(review.getId()));
            } else {
                review.setLikeCount(0);
            }
            
            if (reviewReplyMapper != null) {
                review.setReplyCount(getReviewReplyCount(review.getId()));
                // 获取回复列表（可选，如果前端需要显示回复）
                // review.setReplies(getReviewReplies(review.getId()));
            } else {
                review.setReplyCount(0);
            }
        }
        
        return reviews;
    }

    @Override
    public ProductReview createProductReview(ProductReview review) {
        try {
            logger.info("开始创建评价 - 商品ID: {}, 用户ID: {}, 评分: {}", 
                review.getProductId(), review.getUserId(), review.getRating());
            
            // 验证必填字段
            if (review.getProductId() == null) {
                throw new IllegalArgumentException("商品ID不能为空");
            }
            if (review.getUserId() == null) {
                throw new IllegalArgumentException("用户ID不能为空");
            }
            if (review.getRating() == null || review.getRating() < 1 || review.getRating() > 5) {
                throw new IllegalArgumentException("评分必须在1-5之间");
            }
            if (review.getComment() == null || review.getComment().trim().isEmpty()) {
                throw new IllegalArgumentException("评价内容不能为空");
            }
            
            // 设置创建时间和更新时间
            LocalDateTime now = LocalDateTime.now();
            review.setCreateTime(now);
            review.setUpdateTime(now);
            
            // 如果状态为空，设置为正常状态
            if (review.getStatus() == null) {
                review.setStatus(1);
            }
            
            // 处理图片字段：如果是数组，转换为逗号分隔的字符串，并过滤临时路径
            if (review.getImages() != null && !review.getImages().trim().isEmpty()) {
                String imagesStr = review.getImages().trim();
                List<String> imageList = null;
                
                if (imagesStr.startsWith("[")) {
                    try {
                        // 如果是JSON数组格式，解析后转换为逗号分隔
                        imageList = JSON.parseArray(imagesStr, String.class);
                        logger.info("解析JSON数组格式图片，数量: {}", imageList != null ? imageList.size() : 0);
                    } catch (Exception e) {
                        // 如果解析失败，尝试按逗号分隔
                        logger.warn("解析评价图片JSON失败，尝试按逗号分隔: " + e.getMessage());
                        imageList = java.util.Arrays.asList(imagesStr.split(","));
                    }
                } else {
                    // 如果是逗号分隔的字符串，按逗号拆分
                    imageList = java.util.Arrays.asList(imagesStr.split(","));
                    logger.info("按逗号分隔图片字符串，原始字符串: {}, 拆分后数量: {}", imagesStr, imageList.size());
                }
                
                // 过滤临时路径和空值
                if (imageList != null && !imageList.isEmpty()) {
                    imageList = imageList.stream()
                        .filter(img -> img != null && !img.trim().isEmpty() 
                            && !img.trim().startsWith("http://tmp/") 
                            && !img.trim().startsWith("https://tmp/")
                            && !img.trim().startsWith("tmp/"))
                        .map(img -> img.trim()) // 去除空格
                        .collect(java.util.stream.Collectors.toList());
                    
                    logger.info("过滤临时路径后图片数量: {}", imageList.size());
                    
                    if (!imageList.isEmpty()) {
                        String filteredImages = String.join(",", imageList);
                        review.setImages(filteredImages);
                        logger.info("最终保存的图片字段: {}", filteredImages);
                    } else {
                        review.setImages(null);
                        logger.info("所有图片都是临时路径，设置为null");
                    }
                } else {
                    review.setImages(null);
                    logger.info("图片列表为空，设置为null");
                }
            } else {
                review.setImages(null);
                logger.info("图片字段为空或null，设置为null");
            }

            if (review.getVideos() != null && !review.getVideos().trim().isEmpty()) {
                String vStr = review.getVideos().trim();
                java.util.List<String> vList = java.util.Arrays.asList(vStr.split(","));
                vList = vList.stream()
                    .filter(v -> v != null && !v.trim().isEmpty()
                        && !v.trim().startsWith("http://tmp/")
                        && !v.trim().startsWith("https://tmp/")
                        && !v.trim().startsWith("tmp/"))
                    .map(String::trim)
                    .collect(java.util.stream.Collectors.toList());
                review.setVideos(vList.isEmpty() ? null : String.join(",", vList));
            } else {
                review.setVideos(null);
            }
            
            logger.info("准备插入评价 - 商品ID: {}, 用户ID: {}, 评分: {}, 内容: {}, 图片: {}, 视频: {}", 
                review.getProductId(), review.getUserId(), review.getRating(), 
                review.getComment(), review.getImages(), review.getVideos());
            
            // 插入评价到数据库
            int result = productReviewMapper.insert(review);
            
            logger.info("插入评价结果 - 返回值: {}, 评价ID: {}", result, review.getId());
            
            if (result > 0) {
                logger.info("创建评价成功 - 评价ID: {}, 商品ID: {}, 用户ID: {}", 
                    review.getId(), review.getProductId(), review.getUserId());
                
                // 更新商品销量（如果有销量字段，可以通过订单统计，这里先不更新）
                // 注意：如果需要更新销量，可以通过统计已完成订单数量来更新
                // 或者添加sale字段到Product实体，然后在这里更新
                
                return review;
            } else {
                logger.error("创建评价失败 - 插入数据库返回0");
                throw new RuntimeException("插入评价到数据库失败");
            }
        } catch (IllegalArgumentException e) {
            logger.error("创建评价参数错误: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("创建评价异常: " + e.getMessage(), e);
            e.printStackTrace();
            throw new RuntimeException("创建评价失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public java.util.List<java.util.Map<String, Object>> getProductSalesRecords(Long productId) {
        try {
            if (orderItemMapper == null || orderMapper == null) {
                logger.warn("OrderItemMapper或OrderMapper未注入，返回空列表");
                return new java.util.ArrayList<>();
            }
            
            // 查询该商品的所有订单项（只查询已支付、已发货、已完成的订单）
            List<com.pethome.entity.OrderItem> orderItems = orderItemMapper.selectByProductId(productId);
            
            if (orderItems == null || orderItems.isEmpty()) {
                return new java.util.ArrayList<>();
            }
            
            // 构建销售记录列表
            java.util.List<java.util.Map<String, Object>> salesRecords = new java.util.ArrayList<>();
            
            for (com.pethome.entity.OrderItem item : orderItems) {
                // 查询订单信息
                com.pethome.entity.Order order = orderMapper.selectById(item.getOrderId());
                if (order == null) {
                    continue;
                }
                
                // 查询用户信息
                com.pethome.entity.User user = null;
                String customerName = "未知用户";
                if (order.getUserId() != null) {
                    try {
                        user = userMapper.selectById(order.getUserId());
                        if (user != null) {
                            // 优先使用nickname，其次使用username，最后使用"用户{id}"
                            if (user.getNickname() != null && !user.getNickname().trim().isEmpty()) {
                                customerName = user.getNickname();
                            } else if (user.getUsername() != null && !user.getUsername().trim().isEmpty()) {
                                customerName = user.getUsername();
                            } else {
                                customerName = "用户" + user.getId();
                            }
                        } else {
                            logger.warn("用户不存在 - 用户ID: {}", order.getUserId());
                            customerName = "用户" + order.getUserId();
                        }
                    } catch (Exception e) {
                        logger.error("查询用户信息失败 - 用户ID: {}, 错误: {}", order.getUserId(), e.getMessage());
                        customerName = "用户" + order.getUserId();
                    }
                } else {
                    logger.warn("订单用户ID为空 - 订单ID: {}", order.getId());
                }
                
                // 计算金额（单价 * 数量）
                java.math.BigDecimal amount = item.getPrice().multiply(new java.math.BigDecimal(item.getQuantity()));
                if (item.getSpecPrice() != null) {
                    amount = item.getSpecPrice().multiply(new java.math.BigDecimal(item.getQuantity()));
                }
                
                // 构建销售记录
                java.util.Map<String, Object> record = new java.util.HashMap<>();
                record.put("orderId", order.getOrderNo() != null ? order.getOrderNo() : "ORDER" + order.getId());
                record.put("customer", customerName);
                record.put("quantity", item.getQuantity());
                record.put("amount", amount);
                record.put("date", order.getCreateTime() != null ? order.getCreateTime().toString() : "");
                
                salesRecords.add(record);
            }
            
            return salesRecords;
        } catch (Exception e) {
            logger.error("获取商品销售记录失败: " + e.getMessage(), e);
            e.printStackTrace();
            return new java.util.ArrayList<>();
        }
    }
    
    // ========== 评论点赞相关方法 ==========
    
    @Override
    public boolean toggleReviewLike(Long reviewId, Long userId) {
        try {
            if (reviewLikeMapper == null) {
                logger.warn("ReviewLikeMapper未注入，无法执行点赞操作");
                return false;
            }
            
            if (reviewId == null || userId == null) {
                throw new IllegalArgumentException("评论ID和用户ID不能为空");
            }
            
            // 检查是否已点赞
            com.pethome.entity.ReviewLike existingLike = reviewLikeMapper.findByReviewIdAndUserId(reviewId, userId);
            
            if (existingLike != null) {
                // 已点赞，执行取消点赞
                int result = reviewLikeMapper.deleteById(existingLike.getId());
                logger.info("取消点赞 - 评论ID: {}, 用户ID: {}, 结果: {}", reviewId, userId, result > 0);
                return false; // 返回false表示已取消点赞
            } else {
                // 未点赞，执行点赞
                com.pethome.entity.ReviewLike newLike = new com.pethome.entity.ReviewLike(reviewId, userId);
                int result = reviewLikeMapper.insert(newLike);
                logger.info("点赞成功 - 评论ID: {}, 用户ID: {}, 结果: {}", reviewId, userId, result > 0);
                return result > 0; // 返回true表示已点赞
            }
        } catch (Exception e) {
            logger.error("切换点赞状态失败: " + e.getMessage(), e);
            throw new RuntimeException("切换点赞状态失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean isReviewLiked(Long reviewId, Long userId) {
        try {
            if (reviewLikeMapper == null || reviewId == null || userId == null) {
                return false;
            }
            
            com.pethome.entity.ReviewLike like = reviewLikeMapper.findByReviewIdAndUserId(reviewId, userId);
            return like != null;
        } catch (Exception e) {
            logger.error("检查点赞状态失败: " + e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public Integer getReviewLikeCount(Long reviewId) {
        try {
            if (reviewLikeMapper == null || reviewId == null) {
                return 0;
            }
            
            Integer count = reviewLikeMapper.countByReviewId(reviewId);
            return count != null ? count : 0;
        } catch (Exception e) {
            logger.error("获取点赞数失败: " + e.getMessage(), e);
            return 0;
        }
    }
    
    // ========== 评论回复相关方法 ==========
    
    @Override
    public com.pethome.entity.ReviewReply createReviewReply(Long reviewId, Long userId, String content) {
        try {
            if (reviewReplyMapper == null) {
                logger.warn("ReviewReplyMapper未注入，无法创建回复");
                throw new RuntimeException("回复功能未启用");
            }
            
            if (reviewId == null || userId == null) {
                throw new IllegalArgumentException("评论ID和用户ID不能为空");
            }
            
            if (content == null || content.trim().isEmpty()) {
                throw new IllegalArgumentException("回复内容不能为空");
            }
            
            // 验证评论是否存在
            ProductReview review = productReviewMapper.selectById(reviewId);
            if (review == null) {
                throw new IllegalArgumentException("评论不存在");
            }
            
            // 创建回复
            com.pethome.entity.ReviewReply reply = new com.pethome.entity.ReviewReply(reviewId, userId, content.trim());
            int result = reviewReplyMapper.insert(reply);
            
            if (result > 0) {
                logger.info("创建回复成功 - 回复ID: {}, 评论ID: {}, 用户ID: {}", reply.getId(), reviewId, userId);
                
                // 填充用户信息
                com.pethome.entity.User user = userMapper.selectById(userId);
                if (user != null) {
                    reply.setUserName(user.getNickname() != null ? user.getNickname() : 
                                     (user.getUsername() != null ? user.getUsername() : "用户" + userId));
                    reply.setUserAvatar(user.getAvatar());
                }
                
                return reply;
            } else {
                throw new RuntimeException("创建回复失败");
            }
        } catch (IllegalArgumentException e) {
            logger.error("创建回复参数错误: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("创建回复异常: " + e.getMessage(), e);
            throw new RuntimeException("创建回复失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public java.util.List<com.pethome.entity.ReviewReply> getReviewReplies(Long reviewId) {
        try {
            if (reviewReplyMapper == null || reviewId == null) {
                return new java.util.ArrayList<>();
            }
            
            List<com.pethome.entity.ReviewReply> replies = reviewReplyMapper.findByReviewId(reviewId);
            
            // 填充用户信息
            if (replies != null && !replies.isEmpty() && userMapper != null) {
                for (com.pethome.entity.ReviewReply reply : replies) {
                    if (reply.getUserId() != null) {
                        com.pethome.entity.User user = userMapper.selectById(reply.getUserId());
                        if (user != null) {
                            reply.setUserName(user.getNickname() != null ? user.getNickname() : 
                                             (user.getUsername() != null ? user.getUsername() : "用户" + reply.getUserId()));
                            reply.setUserAvatar(user.getAvatar());
                        }
                    }
                }
            }
            
            return replies != null ? replies : new java.util.ArrayList<>();
        } catch (Exception e) {
            logger.error("获取回复列表失败: " + e.getMessage(), e);
            return new java.util.ArrayList<>();
        }
    }
    
    @Override
    public Integer getReviewReplyCount(Long reviewId) {
        try {
            if (reviewReplyMapper == null || reviewId == null) {
                return 0;
            }
            
            Integer count = reviewReplyMapper.countByReviewId(reviewId);
            return count != null ? count : 0;
        } catch (Exception e) {
            logger.error("获取回复数失败: " + e.getMessage(), e);
            return 0;
        }
    }
}
