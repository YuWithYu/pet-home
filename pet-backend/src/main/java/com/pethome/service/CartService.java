package com.pethome.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.Cart;

public interface CartService {
    IPage<Cart> getCartListByUserId(Page<Cart> page, Long userId);
    Cart addToCart(Cart cart);
    Cart updateCartItem(Cart cart);
    boolean removeFromCart(Long id);
    Cart getCartItemById(Long id);
    Integer getCartCount(Long userId);
}

