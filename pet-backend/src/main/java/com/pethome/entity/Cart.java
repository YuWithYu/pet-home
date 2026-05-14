package com.pethome.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("shopping_cart")
public class Cart {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long productId;
    private Integer quantity;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 非数据库字段：商品信息（用于前端显示）
    @TableField(exist = false)
    private String productName;
    
    @TableField(exist = false)
    private String productPic;
    
    @TableField(exist = false)
    private String productImage;
    
    @TableField(exist = false)
    private java.math.BigDecimal price;
    
    @TableField(exist = false)
    private String productAttr; // 商品属性（JSON字符串）
    
    @TableField(exist = false)
    private String storeName; // 店铺名称
    
    @TableField(exist = false)
    private Long storeId; // 店铺ID

    // 构造函数
    public Cart() {
    }

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
    
    // 商品信息的getter和setter
    public String getProductName() {
        return productName;
    }
    
    public void setProductName(String productName) {
        this.productName = productName;
    }
    
    public String getProductPic() {
        return productPic;
    }
    
    public void setProductPic(String productPic) {
        this.productPic = productPic;
    }
    
    public String getProductImage() {
        return productImage;
    }
    
    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public String getProductAttr() {
        return productAttr;
    }
    
    public void setProductAttr(String productAttr) {
        this.productAttr = productAttr;
    }
    
    public String getStoreName() {
        return storeName;
    }
    
    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }
    
    public Long getStoreId() {
        return storeId;
    }
    
    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }
}

