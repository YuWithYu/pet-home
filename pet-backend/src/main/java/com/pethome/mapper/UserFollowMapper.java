package com.pethome.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pethome.entity.UserFollow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserFollowMapper extends BaseMapper<UserFollow> {
    
    /**
     * 获取用户的关注列表
     */
    List<UserFollow> getFollowingList(@Param("userId") Long userId);
    
    /**
     * 获取用户的粉丝列表
     */
    List<UserFollow> getFollowerList(@Param("userId") Long userId);
    
    /**
     * 检查是否已关注
     */
    UserFollow checkFollowStatus(@Param("followerId") Long followerId, @Param("followingId") Long followingId);
}
