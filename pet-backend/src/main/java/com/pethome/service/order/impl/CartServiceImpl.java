package com.pethome.service.order.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.Cart;
import com.pethome.entity.Product;
import com.pethome.entity.Store;
import com.pethome.mapper.CartMapper;
import com.pethome.mapper.ProductMapper;
import com.pethome.mapper.StoreMapper;
import com.pethome.service.order.CartService;
import com.pethome.util.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    private static final Logger logger = LoggerFactory.getLogger(CartServiceImpl.class);
    
    // Redis 缓存 key 前缀
    private static final String CART_PREFIX = "cart:user:";
    // 缓存过期时间（天）
    private static final long CART_CACHE_EXPIRE_DAYS = 1;

    @Autowired
    private CartMapper cartMapper;
    
    @Autowired
    private ProductMapper productMapper;
    
    @Autowired
    private StoreMapper storeMapper;
    
    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;
    
    /**
     * 清除购物车缓存
     */
    private void clearCartCache(Long userId) {
        if (stringRedisTemplate == null || userId == null) {
            return;
        }
        
        try {
            String cacheKey = CART_PREFIX + userId;
            Boolean deleted = stringRedisTemplate.delete(cacheKey);
            if (Boolean.TRUE.equals(deleted)) {
                logger.info("成功清除购物车缓存，userId: {}, key: {}", userId, cacheKey);
            }
        } catch (Exception e) {
            logger.error("清除购物车缓存失败，userId: {}, 错误: {}", userId, e.getMessage(), e);
        }
    }

    @Override
    public IPage<Cart> getCartListByUserId(Page<Cart> page, Long userId) {
        QueryWrapper<Cart> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        return cartMapper.selectPage(page, queryWrapper);
    }

    @Override
    public Cart addToCart(Cart cart) {
        // 检查购物车中是否已存在相同商品
        QueryWrapper<Cart> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", cart.getUserId())
                .eq("product_id", cart.getProductId());
        Cart existingCart = cartMapper.selectOne(queryWrapper);

        Cart result;
        if (existingCart != null) {
            // 如果已存在，则增加数量
            existingCart.setQuantity(existingCart.getQuantity() + cart.getQuantity());
            existingCart.setUpdateTime(LocalDateTime.now());
            cartMapper.updateById(existingCart);
            result = existingCart;
        } else {
            // 如果不存在，则新增
            cart.setCreateTime(LocalDateTime.now());
            cart.setUpdateTime(LocalDateTime.now());
            cartMapper.insert(cart);
            result = cart;
        }
        
        // 清除购物车缓存
        if (cart.getUserId() != null) {
            clearCartCache(cart.getUserId());
            logger.info("添加到购物车成功，已清除缓存，userId: {}, productId: {}", cart.getUserId(), cart.getProductId());
        }
        
        return result;
    }

    @Override
    public Cart updateCartItem(Cart cart) {
        cart.setUpdateTime(LocalDateTime.now());
        cartMapper.updateById(cart);
        
        // 清除购物车缓存
        if (cart.getUserId() != null) {
            clearCartCache(cart.getUserId());
            logger.info("更新购物车商品成功，已清除缓存，userId: {}", cart.getUserId());
        }
        
        return cart;
    }

    @Override
    public boolean removeFromCart(Long id) {
        // 先查询获取 userId
        Cart cart = cartMapper.selectById(id);
        Long userId = cart != null ? cart.getUserId() : null;
        
        boolean result = cartMapper.deleteById(id) > 0;
        
        // 清除购物车缓存
        if (result && userId != null) {
            clearCartCache(userId);
            logger.info("从购物车删除商品成功，已清除缓存，cartId: {}, userId: {}", id, userId);
        }
        
        return result;
    }

    @Override
    public Cart getCartItemById(Long id) {
        return cartMapper.selectById(id);
    }

    @Override
    public Integer getCartCount(Long userId) {
        if (userId == null) {
            return 0;
        }
        
        // 优先从缓存获取购物车列表，然后计算数量
        List<Cart> cartList = getUserCartItems(userId);
        return cartList.stream().mapToInt(Cart::getQuantity).sum();
    }

    @Override
    public List<Cart> getUserCartItems(Long userId) {
        if (userId == null) {
            return List.of();
        }
        
        String cacheKey = CART_PREFIX + userId;
        
        // 优先从 Redis 缓存获取
        if (stringRedisTemplate != null) {
            try {
                String json = stringRedisTemplate.opsForValue().get(cacheKey);
                if (StringUtils.hasText(json)) {
                    logger.info("从 Redis 缓存获取购物车，userId: {}, key: {}", userId, cacheKey);
                    List<Cart> cachedCart = JSONUtil.parseArray(json, Cart.class);
                    if (cachedCart != null && cachedCart.size() > 0) {
                        // 如果缓存中有数据，直接返回
                        return cachedCart;
                    } else {
                        // 如果缓存是空数组，可能是旧缓存，清除后重新查询数据库
                        stringRedisTemplate.delete(cacheKey);
                    }
                }
            } catch (Exception e) {
                logger.warn("从 Redis 获取购物车失败，降级到数据库查询，userId: {}, 错误: {}", userId, e.getMessage());
            }
        }
        
        // 从数据库查询
        QueryWrapper<Cart> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.orderByDesc("create_time");
        List<Cart> cartItems = cartMapper.selectList(queryWrapper);

        // 写入 Redis 缓存
        if (cartItems != null && stringRedisTemplate != null) {
            try {
                String jsonValue = JSONUtil.toJSONString(cartItems);
                stringRedisTemplate.opsForValue().set(cacheKey, jsonValue, CART_CACHE_EXPIRE_DAYS, TimeUnit.DAYS);
                logger.info("成功写入购物车到 Redis 缓存，userId: {}, key: {}, 商品数量: {}, 过期时间: {} 天", userId, cacheKey, cartItems.size(), CART_CACHE_EXPIRE_DAYS);
            } catch (Exception e) {
                logger.error("写入购物车到 Redis 缓存失败，userId: {}, 错误: {}", userId, e.getMessage(), e);
            }
        }
        
        return cartItems != null ? cartItems : List.of();
    }

    @Override
    public List<Cart> getCartListWithProductInfo(Long userId) {
        // 获取购物车列表
        List<Cart> cartItems = getUserCartItems(userId);
        if (cartItems == null || cartItems.isEmpty()) {
            return List.of();
        }

        // 批量查询商品信息：单次 selectBatchIds 替代逐项 selectById，消除 N+1
        Set<Long> productIds = new LinkedHashSet<>();
        for (Cart cart : cartItems) {
            if (cart.getProductId() != null) {
                productIds.add(cart.getProductId());
            }
        }
        Map<Long, Product> productMap = productIds.isEmpty()
                ? Collections.emptyMap()
                : productMapper.selectBatchIds(productIds).stream()
                        .collect(Collectors.toMap(Product::getId, product -> product));

        // 批量查询店铺信息：单次 selectBatchIds 替代逐项 selectById
        Set<Long> storeIds = new LinkedHashSet<>();
        for (Product product : productMap.values()) {
            if (product.getStoreId() != null) {
                storeIds.add(product.getStoreId());
            }
        }
        Map<Long, Store> storeMap = storeIds.isEmpty()
                ? Collections.emptyMap()
                : storeMapper.selectBatchIds(storeIds).stream()
                        .collect(Collectors.toMap(Store::getId, store -> store));

        // 为每个购物车项填充商品与店铺信息
        for (Cart cart : cartItems) {
            Product product = cart.getProductId() != null ? productMap.get(cart.getProductId()) : null;
            if (product == null) {
                logger.warn("商品不存在或已下架，productId: {}", cart.getProductId());
                continue;
            }
            cart.setProductName(product.getName());
            String img = (product.getImage() != null && !product.getImage().trim().isEmpty())
                    ? product.getImage() : product.getImageUrl();
            if (img != null && !img.trim().isEmpty()) {
                cart.setProductPic(img.trim());
                cart.setProductImage(img.trim());
            }
            cart.setPrice(product.getPrice());
            cart.setProductAttr(product.getParams()); // 商品属性

            Store store = product.getStoreId() != null ? storeMap.get(product.getStoreId()) : null;
            if (store != null) {
                cart.setStoreName(store.getName());
                cart.setStoreId(product.getStoreId());
            } else {
                logger.warn("商品没有店铺或店铺不存在，productId: {}, storeId: {}", cart.getProductId(), product.getStoreId());
            }
        }

        if (logger.isDebugEnabled()) {
            for (Cart cart : cartItems) {
                logger.debug("购物车项 - productId: {}, productName: {}, storeId: {}, storeName: {}",
                        cart.getProductId(), cart.getProductName(), cart.getStoreId(), cart.getStoreName());
            }
        }

        return cartItems;
    }

    @Override
    public boolean clearUserCart(Long userId) {
        QueryWrapper<Cart> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        boolean result = cartMapper.delete(queryWrapper) > 0;
        
        // 清除购物车缓存
        if (result) {
            clearCartCache(userId);
            logger.info("清空购物车成功，已清除缓存，userId: {}", userId);
        }
        
        return result;
    }

    @Override
    public List<Cart> batchAddToCart(List<Cart> cartItems) {
        Long userId = null;
        for (Cart cart : cartItems) {
            addToCart(cart);
            if (userId == null && cart.getUserId() != null) {
                userId = cart.getUserId();
            }
        }
        
        // 批量添加后统一清除缓存
        if (userId != null) {
            clearCartCache(userId);
            logger.info("批量添加到购物车成功，已清除缓存，userId: {}", userId);
        }
        
        return cartItems;
    }
}


