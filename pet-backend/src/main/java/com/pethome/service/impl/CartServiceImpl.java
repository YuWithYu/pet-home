package com.pethome.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.Cart;
import com.pethome.mapper.CartMapper;
import com.pethome.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartMapper cartMapper;

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

        if (existingCart != null) {
            // 如果已存在，则增加数量
            existingCart.setQuantity(existingCart.getQuantity() + cart.getQuantity());
            existingCart.setUpdateTime(LocalDateTime.now());
            cartMapper.updateById(existingCart);
            return existingCart;
        } else {
            // 如果不存在，则新增
            cart.setCreateTime(LocalDateTime.now());
            cart.setUpdateTime(LocalDateTime.now());
            cartMapper.insert(cart);
            return cart;
        }
    }

    @Override
    public Cart updateCartItem(Cart cart) {
        cart.setUpdateTime(LocalDateTime.now());
        cartMapper.updateById(cart);
        return cart;
    }

    @Override
    public boolean removeFromCart(Long id) {
        return cartMapper.deleteById(id) > 0;
    }

    @Override
    public Cart getCartItemById(Long id) {
        return cartMapper.selectById(id);
    }

    @Override
    public Integer getCartCount(Long userId) {
        QueryWrapper<Cart> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);

        // 计算购物车商品总数量
        List<Cart> cartList = cartMapper.selectList(queryWrapper);
        return cartList.stream().mapToInt(Cart::getQuantity).sum();
    }
}


