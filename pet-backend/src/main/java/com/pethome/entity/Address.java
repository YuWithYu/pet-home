package com.pethome.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

@TableName("addresses")
public class Address {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String contactName;
    private String contactPhone;
    private String province;
    private String city;
    private String district;
    private String street;
    private String detail;
    @TableField("is_default")
    private Boolean isDefault;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 非数据库字段：用于前端显示
    @TableField(exist = false)
    private String name; // 兼容前端字段名（对应contactName）
    
    @TableField(exist = false)
    private String phone; // 兼容前端字段名（对应contactPhone）
    
    @TableField(exist = false)
    private String fullAddress; // 完整地址（组合province+city+district+detail）

    // 构造函数
    public Address() {
    }
    
    // 初始化非数据库字段的方法
    public void initDisplayFields() {
        if (this.name == null && this.contactName != null) {
            this.name = this.contactName;
        }
        if (this.phone == null && this.contactPhone != null) {
            this.phone = this.contactPhone;
        }
        if (this.fullAddress == null) {
            StringBuilder sb = new StringBuilder();
            if (this.province != null) sb.append(this.province);
            if (this.city != null) sb.append(this.city);
            if (this.district != null) sb.append(this.district);
            if (this.detail != null) sb.append(this.detail);
            this.fullAddress = sb.toString();
        }
    }
    
    // 从前端数据设置到数据库字段
    public void setFromFrontendData() {
        if (this.name != null && this.contactName == null) {
            this.contactName = this.name;
        }
        if (this.phone != null && this.contactPhone == null) {
            this.contactPhone = this.phone;
        }
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

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
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

    // 非数据库字段的getter和setter
    public String getName() {
        if (name == null && contactName != null) {
            return contactName;
        }
        return name;
    }

    public void setName(String name) {
        this.name = name;
        if (contactName == null) {
            this.contactName = name;
        }
    }

    public String getPhone() {
        if (phone == null && contactPhone != null) {
            return contactPhone;
        }
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
        if (contactPhone == null) {
            this.contactPhone = phone;
        }
    }

    public String getFullAddress() {
        if (fullAddress == null) {
            StringBuilder sb = new StringBuilder();
            if (province != null) sb.append(province);
            if (city != null) sb.append(city);
            if (district != null) sb.append(district);
            if (detail != null) sb.append(detail);
            fullAddress = sb.toString();
        }
        return fullAddress;
    }

    public void setFullAddress(String fullAddress) {
        this.fullAddress = fullAddress;
    }
}

