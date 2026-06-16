package com.pethome.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pethome.entity.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PostMapper extends BaseMapper<Post> {
    
    /**
     * 根据热度分数获取热门帖子
     * 热度 = like_count*0.5 + comment_count*0.3 + view_count*0.2 + exposure_score
     */
    List<Post> selectHotPostsByScore(@Param("limit") Integer limit);
}