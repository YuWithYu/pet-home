package com.pethome.entity;

import com.baomidou.mybatisplus.annotation.TableName;

@TableName("post_tag")
public class PostTag {
    private Long postId;
    private Long tagId;
    
    // Getter and Setter methods
    public Long getPostId() {
        return postId;
    }
    
    public void setPostId(Long postId) {
        this.postId = postId;
    }
    
    public Long getTagId() {
        return tagId;
    }
    
    public void setTagId(Long tagId) {
        this.tagId = tagId;
    }
}

