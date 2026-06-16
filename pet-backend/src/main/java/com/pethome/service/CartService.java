package com.pethome.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.Cart;

import java.util.List;

public interface CartService {
    IPage<Cart> getCartListByUserId(Page<Cart> page, Long userId);
    Cart addToCart(Cart cart);
    Cart updateCartItem(Cart cart);
    boolean removeFromCart(Long id);
    Cart getCartItemById(Long id);
    Integer getCartCount(Long userId);
    List<Cart> getUserCartItems(Long userId);
    List<Cart> getCartListWithProductInfo(Long userId); // 获取购物车列表并填充商品信息
    boolean clearUserCart(Long userId);
    List<Cart> batchAddToCart(List<Cart> cartItems);
}

