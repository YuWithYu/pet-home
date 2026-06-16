package com.pethome.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.common.Result;
import com.pethome.entity.Cart;
import com.pethome.entity.User;
import com.pethome.mapper.UserMapper;
import com.pethome.service.CartService;
import com.pethome.util.JwtUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
@Api(tags = "购物车管理")
public class CartController {

    @Autowired
    private CartService cartService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private UserMapper userMapper;
    
    @Value("${host:http://localhost}")
    private String serverHost;
    
    @Value("${server.port:8080}")
    private String serverPort;
    
    /**
     * 从请求头中获取token并解析userId
     */
    private Long getUserIdFromToken(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            System.out.println("[CartController] getUserIdFromToken - Authorization header: " + (authHeader != null ? authHeader.substring(0, Math.min(20, authHeader.length())) + "..." : "null"));
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String username = jwtUtil.getUsernameFromToken(token);
                System.out.println("[CartController] getUserIdFromToken - username from token: " + username);
                if (username != null) {
                    // 通过username查询用户ID
                    User user = userMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User>()
                            .eq("username", username)
                            .or()
                            .eq("email", username)
                            .last("LIMIT 1"));
                    if (user != null) {
                        System.out.println("[CartController] getUserIdFromToken - 找到用户，userId: " + user.getId());
                        return user.getId();
                    } else {
                        System.out.println("[CartController] getUserIdFromToken - 未找到用户，username: " + username);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("[CartController] getUserIdFromToken - 异常: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
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
        if (imagePath.startsWith("/upload/")) {
            return serverHost + ":" + serverPort + imagePath;
        }
        
        if (imagePath.startsWith("/")) {
            return serverHost + ":" + serverPort + "/upload" + imagePath;
        }
        
        // 其他情况（如 product/xxx.jpg），添加 /upload/ 前缀
        return serverHost + ":" + serverPort + "/upload/" + imagePath;
    }

    @GetMapping("/page")
    @ApiOperation("分页查询购物车")
    public Result<IPage<Cart>> getCartPage(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam Long userId) {
        Page<Cart> page = new Page<>(pageNo, pageSize);
        IPage<Cart> result = cartService.getCartListByUserId(page, userId);
        return Result.success(result);
    }

    @PostMapping("/add")
    @ApiOperation("添加到购物车")
    public Result<Cart> addToCart(@RequestBody Cart cart) {
        return Result.success(cartService.addToCart(cart));
    }

    @PutMapping("/update")
    @ApiOperation("更新购物车商品")
    public Result<Cart> updateCartItem(@RequestBody Cart cart) {
        return Result.success(cartService.updateCartItem(cart));
    }

    @DeleteMapping("/{id}")
    @ApiOperation("从购物车删除商品")
    public Result<Boolean> removeFromCart(@PathVariable Long id) {
        return Result.success(cartService.removeFromCart(id));
    }

    @GetMapping("/list")
    @ApiOperation("获取购物车列表（前端接口）")
    public Result<java.util.List<Cart>> getCartList(
            @RequestParam(required = false) Long userId,
            HttpServletRequest request) {
        // 如果没有传入userId，尝试从token中获取
        if (userId == null) {
            userId = getUserIdFromToken(request);
            if (userId == null) {
                System.out.println("[CartController] getCartList - token解析失败，返回错误");
                return Result.error("用户未登录或token无效");
            }
        }
        
        System.out.println("[CartController] getCartList - userId: " + userId);
        
        java.util.List<Cart> cartItems = cartService.getCartListWithProductInfo(userId);
        
        System.out.println("[CartController] getCartList - 查询结果数量: " + (cartItems != null ? cartItems.size() : 0));
        
        // 不在此处拼接完整图片 URL，保持相对路径（如 /upload/xxx）返回，由前端 util.getImageUrl 按当前环境 base 拼接，避免真机/小程序中 localhost 图不显示
        for (Cart cart : cartItems) {
            System.out.println("[CartController] getCartList - 购物车项: productId=" + cart.getProductId() + ", productName=" + cart.getProductName() + ", storeId=" + cart.getStoreId() + ", storeName=" + cart.getStoreName());
        }
        
        return Result.success(cartItems);
    }

    @PostMapping("/delete")
    @ApiOperation("删除购物车商品（支持批量删除）")
    public Result<Boolean> deleteCartItem(@RequestBody java.util.Map<String, Object> params) {
        try {
            Object idsObj = params.get("ids");
            if (idsObj == null) {
                return Result.error("ids参数不能为空");
            }
            
            // 支持单个ID或ID数组
            if (idsObj instanceof Number) {
                Long id = ((Number) idsObj).longValue();
                boolean success = cartService.removeFromCart(id);
                return Result.success(success);
            } else if (idsObj instanceof java.util.List) {
                @SuppressWarnings("unchecked")
                java.util.List<Number> ids = (java.util.List<Number>) idsObj;
                for (Number idNum : ids) {
                    cartService.removeFromCart(idNum.longValue());
                }
                return Result.success(true);
            } else {
                return Result.error("ids参数格式错误");
            }
        } catch (Exception e) {
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    @PostMapping("/update/quantity")
    @ApiOperation("更新购物车商品数量")
    public Result<Cart> updateQuantity(@RequestBody java.util.Map<String, Object> params) {
        try {
            Object idObj = params.get("id");
            Object quantityObj = params.get("quantity");
            
            if (idObj == null || quantityObj == null) {
                return Result.error("id和quantity参数不能为空");
            }
            
            Long id = ((Number) idObj).longValue();
            Integer quantity = ((Number) quantityObj).intValue();
            
            // 查询购物车项
            Cart cart = cartService.getCartItemById(id);
            if (cart == null) {
                return Result.error("购物车商品不存在");
            }
            
            cart.setQuantity(quantity);
            Cart updatedCart = cartService.updateCartItem(cart);
            return Result.success(updatedCart);
        } catch (Exception e) {
            return Result.error("更新数量失败: " + e.getMessage());
        }
    }

    @PostMapping("/clear")
    @ApiOperation("清空购物车")
    public Result<Boolean> clearCart(
            @RequestBody(required = false) java.util.Map<String, Object> params,
            HttpServletRequest request) {
        try {
            Long userId = null;
            if (params != null && params.containsKey("userId")) {
                userId = ((Number) params.get("userId")).longValue();
            }
            
            // 如果没有传入userId，尝试从token中获取
            if (userId == null) {
                userId = getUserIdFromToken(request);
                if (userId == null) {
                    return Result.error("用户未登录或token无效");
                }
            }
            
            boolean success = cartService.clearUserCart(userId);
            return Result.success(success);
        } catch (Exception e) {
            return Result.error("清空购物车失败: " + e.getMessage());
        }
    }

    @GetMapping("/count")
    @ApiOperation("获取购物车商品数量")
    public Result<Integer> getCartCount(@RequestParam Long userId) {
        try {
            Integer count = cartService.getCartCount(userId);
            return Result.success(count);
        } catch (Exception e) {
            return Result.error("获取购物车数量失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @ApiOperation("获取购物车商品详情")
    public Result<Cart> getCartItemDetail(@PathVariable Long id) {
        return Result.success(cartService.getCartItemById(id));
    }

    @GetMapping("/user/{userId}")
    @ApiOperation("获取用户购物车列表")
    public Result<java.util.List<Cart>> getUserCartItems(@PathVariable Long userId) {
        java.util.List<Cart> cartItems = cartService.getCartListWithProductInfo(userId);
        return Result.success(cartItems);
    }

    @DeleteMapping("/clear/{userId}")
    @ApiOperation("清空用户购物车（兼容旧接口）")
    public Result<Boolean> clearCartByPath(@PathVariable Long userId) {
        boolean success = cartService.clearUserCart(userId);
        return Result.success(success);
    }

    @PostMapping("/batch-add")
    @ApiOperation("批量添加到购物车")
    public Result<java.util.List<Cart>> batchAddToCart(@RequestBody java.util.List<Cart> cartItems) {
        java.util.List<Cart> result = cartService.batchAddToCart(cartItems);
        return Result.success(result);
    }
}
