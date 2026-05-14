package com.pethome.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;

@TableName("user")
public class User {
    private Long id;

    private String username;

    private String password;

    private String email;

    @TableField(exist = false)
    private Integer type;

    private Integer status;

    @TableField(exist = false)
    private String activationCode;

    @TableField(exist = false)
    private String headerUrl;

    private Date createTime;

    private String nickname;
    private String avatar;
    @TableField(exist = false)
    private String avatarUrl;
    private String phone;
    private Integer gender;
    private String role;
    @TableField(exist = false)
    private String phoneNumber;
    private Date updateTime;
    private Integer points;
    private Integer memberLevel;
    private Integer charm; // 魅力值：所有历史积分的累计总和
    
    // 背景图URL
    @TableField("background_image")
    private String backgroundImage;

    // 个性签名
    private String signature;

    /** 用户兴趣标签（逗号分隔），用于发现页规则型推荐与帖子 tags 匹配 */
    @TableField("interest_tags")
    private String interestTags;
    
    // 微信相关字段
    private String openid; // 微信小程序openid
    private String unionid; // 微信unionid（可选，用于多平台统一用户）

    /** 账号(username)上次修改时间，用于「每1年可修改1次」限制 */
    @TableField("username_updated_at")
    private Date usernameUpdatedAt;

    // 用于 MyBatis 构造器映射的构造器（匹配 UserMapper.xml 中的 BaseResultMap）
    public User(Long id, String username, String password, String email, Integer status, Date createTime) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.status = status;
        this.createTime = createTime;
    }

    // 旧的构造器，保留用于向后兼容
    public User(Long id, String username, String password, String email, Integer type, Integer status, String activationCode, String headerUrl, Date createTime) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.type = type;
        this.status = status;
        this.activationCode = activationCode;
        this.headerUrl = headerUrl;
        this.createTime = createTime;
    }

    public User() {
        super();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username == null ? null : username.trim();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password == null ? null : password.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email == null ? null : email.trim();
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Object status) {
        if (status == null) {
            this.status = null;
            return;
        }
        if (status instanceof Number) {
            this.status = ((Number) status).intValue();
            return;
        }
        String s = String.valueOf(status).trim().toLowerCase();
        if (s.isEmpty()) {
            return;
        }
        if ("1".equals(s) || "true".equals(s) || "enabled".equals(s) || "enable".equals(s)
                || "active".equals(s) || "normal".equals(s) || "正常".equals(s) || "启用".equals(s)) {
            this.status = 1;
            return;
        }
        if ("0".equals(s) || "false".equals(s) || "disabled".equals(s) || "disable".equals(s)
                || "inactive".equals(s) || "banned".equals(s) || "禁用".equals(s) || "封禁".equals(s)) {
            this.status = 0;
            return;
        }
        try {
            this.status = Integer.parseInt(s);
        } catch (Exception ignored) {
            // 非法值忽略，保持原值不变
        }
    }

    public String getActivationCode() {
        return activationCode;
    }

    public void setActivationCode(String activationCode) {
        this.activationCode = activationCode == null ? null : activationCode.trim();
    }

    public String getHeaderUrl() {
        return headerUrl;
    }

    public void setHeaderUrl(String headerUrl) {
        this.headerUrl = headerUrl == null ? null : headerUrl.trim();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Integer getGender() { return gender; }
    public void setGender(Integer gender) { this.gender = gender; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }
    public Integer getMemberLevel() { return memberLevel; }
    public void setMemberLevel(Integer memberLevel) { this.memberLevel = memberLevel; }
    public String getBackgroundImage() { return backgroundImage; }
    public void setBackgroundImage(String backgroundImage) { this.backgroundImage = backgroundImage; }
    public String getSignature() { return signature; }
    public void setSignature(String signature) { this.signature = signature; }
    public String getInterestTags() { return interestTags; }
    public void setInterestTags(String interestTags) { this.interestTags = interestTags; }
    public Integer getCharm() { return charm; }
    public void setCharm(Integer charm) { this.charm = charm; }
    public String getOpenid() { return openid; }
    public void setOpenid(String openid) { this.openid = openid; }
    public String getUnionid() { return unionid; }
    public void setUnionid(String unionid) { this.unionid = unionid; }
    public Date getUsernameUpdatedAt() { return usernameUpdatedAt; }
    public void setUsernameUpdatedAt(Date usernameUpdatedAt) { this.usernameUpdatedAt = usernameUpdatedAt; }
}