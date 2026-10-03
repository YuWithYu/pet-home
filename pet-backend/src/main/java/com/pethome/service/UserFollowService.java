package com.pethome.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pethome.entity.UserFollow;

import java.util.List;

public interface UserFollowService extends IService<UserFollow> {
    
    /**
     * 关注用户
     */
    boolean followUser(Long followerId, Long followingId);
    
    /**
     * 取消关注用户
     */
    boolean unfollowUser(Long followerId, Long followingId);
    
    /**
     * 检查关注状态
     */
    boolean isFollowing(Long followerId, Long followingId);
    
    /**
     * 获取关注列表
     */
    List<UserFollow> getFollowingList(Long userId);
    
    /**
     * 获取粉丝列表
     */
    List<UserFollow> getFollowerList(Long userId);
    
    /**
     * 获取关注数量
     */
    int getFollowingCount(Long userId);
    
    /**
     * 获取粉丝数量
     */
    int getFollowerCount(Long userId);
}
