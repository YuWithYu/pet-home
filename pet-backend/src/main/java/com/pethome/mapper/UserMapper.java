package com.pethome.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pethome.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    /**
     * 统计用户关注数（该用户关注了多少人）
     */
    @Select("SELECT COUNT(*) FROM user_follow WHERE follower_id = #{userId}")
    Integer countFollow(Long userId);
    
    /**
     * 统计用户粉丝数（有多少人关注了该用户）
     */
    @Select("SELECT COUNT(*) FROM user_follow WHERE following_id = #{userId}")
    Integer countFans(Long userId);
    
    /**
     * 统计用户动态数
     */
    @Select("SELECT COUNT(*) FROM post WHERE user_id = #{userId} AND status = 1")
    Integer countPost(Long userId);
    
    /**
     * 统计用户获赞数（该用户发布的所有动态的总获赞数）
     */
    @Select("SELECT COUNT(*) FROM post_like WHERE post_id IN (SELECT id FROM post WHERE user_id = #{userId} AND status = 1)")
    Integer countLikes(Long userId);
    
    /**
     * 检查昵称是否已被使用（排除指定用户ID）
     */
    @Select("SELECT COUNT(*) FROM user WHERE nickname = #{nickname} AND status = 1 AND (#{excludeUserId} IS NULL OR id != #{excludeUserId})")
    Integer checkNicknameExists(@org.apache.ibatis.annotations.Param("nickname") String nickname, 
                                @org.apache.ibatis.annotations.Param("excludeUserId") Long excludeUserId);
}
