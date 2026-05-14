package com.pethome.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.Cart;
import com.pethome.entity.Product;
import com.pethome.entity.Store;
import com.pethome.mapper.CartMapper;
import com.pethome.mapper.ProductMapper;
import com.pethome.mapper.StoreMapper;
import com.pethome.service.CartService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
                    List<Cart> cachedCart = JSON.parseObject(json, new TypeReference<List<Cart>>() {});
                    if (cachedCart != null && cachedCart.size() > 0) {
                        // 如果缓存中有数据，直接返回
                        System.out.println("[CartServiceImpl] getUserCartItems - 从缓存返回，数量: " + cachedCart.size());
                        return cachedCart;
                    } else {
                        // 如果缓存是空数组，可能是旧缓存，清除后重新查询数据库
                        System.out.println("[CartServiceImpl] getUserCartItems - 缓存为空数组，清除缓存并查询数据库");
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
        
        System.out.println("[CartServiceImpl] getUserCartItems - 从数据库查询，userId: " + userId + ", 查询结果数量: " + (cartItems != null ? cartItems.size() : 0));
        if (cartItems != null && cartItems.size() > 0) {
            System.out.println("[CartServiceImpl] getUserCartItems - 购物车项详情: " + cartItems.stream()
                    .map(c -> "id=" + c.getId() + ",productId=" + c.getProductId() + ",quantity=" + c.getQuantity())
                    .collect(java.util.stream.Collectors.joining(", ")));
        }
        
        // 写入 Redis 缓存
        if (cartItems != null && stringRedisTemplate != null) {
            try {
                String jsonValue = JSON.toJSONString(cartItems);
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
        
        // 为每个购物车项填充商品信息
        for (Cart cart : cartItems) {
            if (cart.getProductId() != null) {
                try {
                    Product product = productMapper.selectById(cart.getProductId());
                    if (product != null) {
                        cart.setProductName(product.getName());
                        String img = (product.getImage() != null && !product.getImage().trim().isEmpty())
                                ? product.getImage() : product.getImageUrl();
                        if (img != null && !img.trim().isEmpty()) {
                            cart.setProductPic(img.trim());
                            cart.setProductImage(img.trim());
                        }
                        cart.setPrice(product.getPrice());
                        cart.setProductAttr(product.getParams()); // 商品属性
                        
                        // 获取店铺信息
                        if (product.getStoreId() != null) {
                            try {
                                logger.info("获取店铺信息，productId: {}, storeId: {}", cart.getProductId(), product.getStoreId());
                                Store store = storeMapper.selectById(product.getStoreId());
                                if (store != null) {
                                    cart.setStoreName(store.getName());
                                    cart.setStoreId(product.getStoreId());
                                    logger.info("成功设置店铺信息，productId: {}, storeId: {}, storeName: {}", cart.getProductId(), product.getStoreId(), store.getName());
                                } else {
                                    logger.warn("店铺不存在，productId: {}, storeId: {}", cart.getProductId(), product.getStoreId());
                                }
                            } catch (Exception e) {
                                logger.error("获取店铺信息失败，productId: {}, storeId: {}, 错误: {}", cart.getProductId(), product.getStoreId(), e.getMessage(), e);
                            }
                        } else {
                            logger.warn("商品没有店铺ID，productId: {}", cart.getProductId());
                        }
                    }
                } catch (Exception e) {
                    logger.warn("获取商品信息失败，productId: {}, 错误: {}", cart.getProductId(), e.getMessage());
                }
            }
        }
        
        // 打印最终结果，确保 storeName 和 storeId 被设置
        for (Cart cart : cartItems) {
            logger.info("最终购物车项 - productId: {}, productName: {}, storeId: {}, storeName: {}", 
                cart.getProductId(), cart.getProductName(), cart.getStoreId(), cart.getStoreName());
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


