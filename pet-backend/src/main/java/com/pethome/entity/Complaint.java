package com.pethome.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

@TableName("complaints")
public class Complaint {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 提交用户ID，未登录可为空 */
    private Long userId;
    /** 投诉类型：内容违规、用户行为、系统问题、其他 */
    private String type;
    /** 投诉内容 */
    private String content;
    /** 联系方式（选填） */
    private String contactInfo;
    /** 图片URL列表JSON，如 ["url1","url2"] */
    private String images;
    /** 状态：pending-待处理，processing-处理中，resolved-已解决 */
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Complaint() {
    }

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
}
