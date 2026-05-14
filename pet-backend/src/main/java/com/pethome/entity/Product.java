package com.pethome.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;

@TableName("product")
public class Product {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
    private String description;
    private String category;
    private String brand;
    private Long storeId; // 店铺ID
    private BigDecimal price;
    private String image;
    private Integer stock;

    @TableField("status")
    private Integer status;

    @TableField(exist = false)
    private String imageUrl;

    @TableField(exist = false)
    private Integer sortOrder;

    // 销量字段
    @TableField(exist = false)
    private Integer sale;

    // 店铺信息（非数据库字段）
    @TableField(exist = false)
    private com.pethome.entity.Store storeInfo;
    
    @TableField(exist = false)
    private String storeAvatar;
    
    @TableField(exist = false)
    private String storeLogo;
    
    @TableField(exist = false)
    private String storeName; // 店铺名称（非数据库字段，用于前端显示）

    // 详情图片字段
    private String detailImages;
    
    // 功能特点字段
    private String features;
    
    // 商品参数字段
    private String params;

    // 服务承诺字段（JSON字符串，存储服务列表）
    @TableField(updateStrategy = com.baomidou.mybatisplus.annotation.FieldStrategy.IGNORED)
    private String services;
    
    // 购买类型（规格）字段（JSON字符串，存储规格列表）
    @TableField(updateStrategy = com.baomidou.mybatisplus.annotation.FieldStrategy.IGNORED)
    private String specs;

    /** 前端规格弹窗用：根据 specs 生成的 SKU 列表（价格/库存/图随规格变化），不落库 */
    @TableField(exist = false)
    private java.util.List<java.util.Map<String, Object>> skuStockList;

    /** 是否热门推荐 */
    @TableField("is_hot")
    private Boolean isHot;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 构造函数
    public Product() {
    }

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
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

    public String getDetailImages() {
        return detailImages;
    }

    public void setDetailImages(String detailImages) {
        this.detailImages = detailImages;
    }

    public String getFeatures() {
        return features;
    }

    public void setFeatures(String features) {
        this.features = features;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public String getServices() {
        return services;
    }

    public void setServices(String services) {
        this.services = services;
    }

    public String getSpecs() {
        return specs;
    }

    public void setSpecs(String specs) {
        this.specs = specs;
    }

    public Boolean getIsHot() {
        return isHot;
    }

    public void setIsHot(Boolean isHot) {
        this.isHot = isHot;
    }

    public java.util.List<java.util.Map<String, Object>> getSkuStockList() {
        return skuStockList;
    }

    public void setSkuStockList(java.util.List<java.util.Map<String, Object>> skuStockList) {
        this.skuStockList = skuStockList;
    }

    public Integer getSale() {
        return sale;
    }

    public void setSale(Integer sale) {
        this.sale = sale;
    }

    public com.pethome.entity.Store getStoreInfo() {
        return storeInfo;
    }

    public void setStoreInfo(com.pethome.entity.Store storeInfo) {
        this.storeInfo = storeInfo;
    }

    public String getStoreAvatar() {
        return storeAvatar;
    }

    public void setStoreAvatar(String storeAvatar) {
        this.storeAvatar = storeAvatar;
    }

    public String getStoreLogo() {
        return storeLogo;
    }

    public void setStoreLogo(String storeLogo) {
        this.storeLogo = storeLogo;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }
}
