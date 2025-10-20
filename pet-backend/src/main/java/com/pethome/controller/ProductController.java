package com.pethome.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.common.Result;
import com.pethome.entity.Product;
import com.pethome.service.ProductService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product")
@Api(tags = "商品管理")
public class ProductController {

    @Autowired
    private ProductService productService;
    
    @Value("${server.host:http://localhost}")
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

    @GetMapping("/list")
    @ApiOperation("获取商品列表")
    public Result<Object> getProductList(
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false, defaultValue = "1") Integer pageNo,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "1000") Integer size) {
        
        List<Product> products;
        
        // 如果指定了分类ID，按分类查询
        if (categoryId != null) {
            products = productService.getProductsByCategoryId(categoryId);
        } else {
            products = productService.getAllProducts();
        }
        
        // 转换图片URL为完整路径
        // 同时设置image字段（前端期望的字段）和imageUrl字段（备用）
        products.forEach(product -> {
            if (product.getImage() != null && !product.getImage().isEmpty()) {
                String fullUrl = convertToFullUrl(product.getImage());
                // 前端使用的是pic或image字段，所以需要保留原始image字段或转换为完整URL
                // 如果image已经是完整URL，保持不变；否则转换
                if (!product.getImage().startsWith("http://") && !product.getImage().startsWith("https://")) {
                    product.setImage(fullUrl);
                }
                product.setImageUrl(fullUrl);
            }
        });
        
        // 构建小程序需要的返回格式
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
    @ApiOperation("分页查询商品")
    public Result<IPage<Product>> getProductPage(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Product> page = new Page<>(pageNo, pageSize);
        IPage<Product> result = productService.getProductPage(page);
        return Result.success(result);
    }

    @PostMapping("/create")
    @ApiOperation("创建商品")
    public Result<Product> createProduct(@RequestBody Product product) {
        try {
            // 保存商品到数据库
            Product savedProduct = productService.createProduct(product);
            if (savedProduct != null) {
                return Result.success(savedProduct);
            } else {
                return Result.error("创建商品失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("创建商品失败: " + e.getMessage());
        }
    }

    @PutMapping("/update")
    @ApiOperation("更新商品")
    public Result<Product> updateProduct(@RequestBody Product product) {
        try {
            // 更新商品信息
            Product updatedProduct = productService.updateProduct(product);
            if (updatedProduct != null) {
                return Result.success(updatedProduct);
            } else {
                return Result.error("更新商品失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("更新商品失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除商品")
    public Result<Boolean> deleteProduct(@PathVariable Long id) {
        try {
            boolean success = productService.deleteProduct(id);
            return Result.success(success);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("删除商品失败: " + e.getMessage());
        }
    }

    @GetMapping("/hot")
    @ApiOperation("获取热门商品")
    public Result<java.util.List<Product>> getHotProducts(@RequestParam(defaultValue = "10") Integer limit) {
        List<Product> products = productService.getHotProducts(limit);
        // 转换图片URL为完整路径
        products.forEach(product -> {
            if (product.getImage() != null && !product.getImage().isEmpty()) {
                product.setImage(convertToFullUrl(product.getImage()));
            }
        });
        return Result.success(products);
    }

    @GetMapping("/recommend")
    @ApiOperation("获取推荐商品")
    public Result<java.util.List<Product>> getRecommendProducts(@RequestParam(defaultValue = "4") Integer limit) {
        List<Product> products = productService.getRecommendProducts(limit);
        // 转换图片URL为完整路径
        products.forEach(product -> {
            if (product.getImage() != null && !product.getImage().isEmpty()) {
                product.setImage(convertToFullUrl(product.getImage()));
            }
        });
        return Result.success(products);
    }

    @GetMapping("/favorites/count")
    @ApiOperation("获取用户商品收藏数量")
    public Result<Integer> getFavoriteCount(@RequestParam Long userId) {

        return Result.success(0);
    }

    @GetMapping("/{id}")
    @ApiOperation("获取商品详情")
    public Result<Product> getProductDetail(@PathVariable Long id) {
        try {
            Product product = productService.getProductById(id);
            if (product == null) {
                return Result.error("商品不存在");
            }
            
            // 处理图片URL
            if (product.getImage() != null) {
                product.setImage(convertToFullUrl(product.getImage()));
            }
            
            return Result.success(product);
        } catch (Exception e) {
            return Result.error("获取商品详情失败: " + e.getMessage());
        }
    }
}
