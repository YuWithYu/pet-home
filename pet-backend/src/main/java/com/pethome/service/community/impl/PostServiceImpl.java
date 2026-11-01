package com.pethome.service.community.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pethome.entity.Comment;
import com.pethome.entity.CommentLike;
import com.pethome.entity.DailyTopicPost;
import com.pethome.entity.Post;
import com.pethome.entity.PostCollect;
import com.pethome.entity.PostLike;
import com.pethome.entity.UserFollow;
import com.pethome.entity.UserPostInteraction;
import com.pethome.mapper.CommentLikeMapper;
import com.pethome.mapper.CommentMapper;
import com.pethome.mapper.DailyTopicPostMapper;
import com.pethome.mapper.PostCollectMapper;
import com.pethome.mapper.PostLikeMapper;
import com.pethome.mapper.PostMapper;
import com.pethome.mapper.PostTagMapper;
import com.pethome.mapper.UserFollowMapper;
import com.pethome.mapper.UserPostInteractionMapper;
import com.pethome.service.community.PostService;
import com.pethome.service.common.RedisCacheService;
import com.pethome.service.community.TagService;
import com.pethome.service.community.support.PostCounterService;
import com.pethome.service.community.support.PostViewAssembler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {

    @Autowired
    private PostMapper postMapper;
    
    @Autowired
    private PostLikeMapper postLikeMapper;

    @Autowired
    private UserFollowMapper userFollowMapper;
    
    @Autowired
    private RedisCacheService redisCacheService;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private CommentLikeMapper commentLikeMapper;

    @Autowired
    private PostTagMapper postTagMapper;

    @Autowired
    private PostCollectMapper postCollectMapper;

    @Autowired
    private DailyTopicPostMapper dailyTopicPostMapper;

    @Autowired
    private UserPostInteractionMapper userPostInteractionMapper;

    @Autowired
    private TagService tagService;

    @Autowired
    private PostViewAssembler postViewAssembler;

    @Autowired
    private PostCounterService postCounterService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void savePostTags(Post post, List<String> tagNames) {
        if (post == null || post.getId() == null || tagNames == null || tagNames.isEmpty()) {
            return;
        }
        List<com.pethome.entity.Tag> tags = tagService.batchGetOrCreateTags(tagNames);
        if (tags == null || tags.isEmpty()) {
            return;
        }
        List<com.pethome.entity.PostTag> postTags = new ArrayList<>();
        for (com.pethome.entity.Tag tag : tags) {
            // 增加标签热度（Redis）
            tagService.incrementTagHotness(tag.getName());
            com.pethome.entity.PostTag postTag = new com.pethome.entity.PostTag();
            postTag.setPostId(post.getId());
            postTag.setTagId(tag.getId());
            postTags.add(postTag);
        }
        postTagMapper.batchInsert(postTags);

        // 同步到 post.tags，便于快速检索/推荐筛选
        post.setTags(String.join(",", tagNames));
        updateById(post);
    }
    
    @Override
    public Page<Post> getPostList(Page<Post> page, String category, Long userId) {
        QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1);
        
        if (category != null && !category.isEmpty()) {
            queryWrapper.eq("category", category);
        }
        
        if (userId != null) {
            queryWrapper.eq("user_id", userId);
        }
        
        queryWrapper.orderByDesc("is_top", "create_time");
        
        Page<Post> result = postMapper.selectPage(page, queryWrapper);
        
        // 从Redis获取最新的点赞量，确保数据一致性
        for (Post post : result.getRecords()) {
            Integer cachedLikes = redisCacheService.getPostLikeCount(post.getId());
            if (cachedLikes != null) {
                post.setLikesCount(cachedLikes);
            }
        }
        
        // 注意：这里不填充点赞状态，因为getPostList方法没有currentUserId参数
        // 点赞状态应该在Controller层根据请求参数填充
        // 先填充用户信息（不检查点赞状态）
        fillUserInfoAndLikeStatus(result.getRecords(), null);
        
        return result;
    }
    
    @Override
    public Post getPostDetail(Long postId, Long currentUserId) {
        // 使用MyBatis-Plus的selectById获取帖子详情
        Post post = postMapper.selectById(postId);
        if (post == null || post.getStatus() != 1) {
            return null;
        }
        
        // 从Redis获取最新的浏览量和点赞量
        Integer cachedViews = redisCacheService.getPostViewCount(postId);
        if (cachedViews != null) {
            post.setViewsCount(cachedViews);
        }
        
        Integer cachedLikes = redisCacheService.getPostLikeCount(postId);
        if (cachedLikes != null) {
            post.setLikesCount(cachedLikes);
        }
        
        // 填充用户信息
        fillUserInfoAndLikeStatus(java.util.Arrays.asList(post), currentUserId);
        
        // 增加浏览量（更新Redis缓存）
        incrementViews(postId);
        
        return post;
    }
    
    @Override
    public List<Post> getHotPosts(Integer limit) {
        QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1)
                   .orderByDesc("likes_count", "views_count", "create_time")
                   .last("LIMIT " + limit);
        
        List<Post> posts = postMapper.selectList(queryWrapper);
        fillUserInfoAndLikeStatus(posts, null);
        
        return posts;
    }

    @Override
    public Page<Post> getVideoFeed(Page<Post> page) {
        QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1)
                .isNotNull("videos")
                .ne("videos", "")
                .ne("videos", "[]")
                .orderByDesc("likes_count", "comments_count", "create_time");
        Page<Post> result = postMapper.selectPage(page, queryWrapper);
        for (Post post : result.getRecords()) {
            Integer cached = redisCacheService.getPostLikeCount(post.getId());
            if (cached != null) post.setLikesCount(cached);
        }
        fillUserInfoAndLikeStatus(result.getRecords(), null);
        return result;
    }
    
    @Override
    public Page<Post> getFollowingPosts(Long userId, Page<Post> page) {
        // 获取关注用户ID列表
        List<UserFollow> followings = userFollowMapper.getFollowingList(userId);
        if (followings.isEmpty()) {
            return page; // 没有关注任何人，返回空结果
        }
        
        List<Long> followingIds = followings.stream()
                .map(UserFollow::getFollowingId)
                .collect(java.util.stream.Collectors.toList());
        
        QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1)
                   .in("user_id", followingIds)
                   .orderByDesc("create_time");
        
        Page<Post> result = postMapper.selectPage(page, queryWrapper);
        fillUserInfoAndLikeStatus(result.getRecords(), userId);
        
        return result;
    }
    
    @Override
    public void incrementViews(Long postId) {
        postCounterService.incrementViews(postId);
    }
    
    @Override
    public void updateLikesCount(Long postId, int delta) {
        postCounterService.updateLikesCount(postId, delta);
    }
    
    @Override
    public void updateCommentsCount(Long postId, int delta) {
        postCounterService.updateCommentsCount(postId, delta);
    }
    
    @Override
    public Page<Post> getAdminPostList(Page<Post> page, String keyword, Integer status, String category) {
        QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
        
        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.and(wrapper -> wrapper
                .like("title", keyword)
                .or()
                .like("content", keyword)
            );
        }
        
        if (status != null) {
            queryWrapper.eq("status", status);
        }
        
        if (category != null && !category.isEmpty()) {
            queryWrapper.eq("category", category);
        }
        
        queryWrapper.orderByDesc("create_time");
        
        Page<Post> result = postMapper.selectPage(page, queryWrapper);
        fillUserInfoAndLikeStatus(result.getRecords(), null);
        
        return result;
    }
    
    @Override
    public List<Post> getUserPosts(Long userId) {
        QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                   .orderByDesc("create_time");
        
        List<Post> posts = postMapper.selectList(queryWrapper);
        fillUserInfoAndLikeStatus(posts, null);
        
        return posts;
    }
    
    /**
     * 填充用户信息和点赞状态
     * 改为public，供其他服务调用
     * 批量查询作者信息与点赞状态，避免逐帖查询的 N+1
     */
    public void fillUserInfoAndLikeStatus(List<Post> posts, Long currentUserId) {
        postViewAssembler.fillUserInfoAndLikeStatus(posts, currentUserId);
    }
    
    @Override
    public List<Post> getTopPosts(Integer limit) {
        QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1)
                   .eq("is_top", true)
                   .orderByDesc("create_time")
                   .last("LIMIT " + (limit != null ? limit : 5));
        
        List<Post> posts = postMapper.selectList(queryWrapper);
        fillUserInfoAndLikeStatus(posts, null);
        return posts;
    }
    
    @Override
    public List<Post> getHotPostsByScore(Integer limit) {
        // 使用原生SQL查询，按热度分数排序
        // 热度 = like_count*0.5 + comment_count*0.3 + view_count*0.2 + exposure_score
        Integer queryLimit = limit != null ? limit : 10;
        List<Post> posts = postMapper.selectHotPostsByScore(queryLimit);
        fillUserInfoAndLikeStatus(posts, null);
        return posts;
    }
    
    @Override
    public List<Post> getLatestPosts(Integer limit) {
        QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1)
                   .eq("is_top", false)
                   .orderByDesc("create_time")
                   .last("LIMIT " + (limit != null ? limit : 10));
        
        List<Post> posts = postMapper.selectList(queryWrapper);
        fillUserInfoAndLikeStatus(posts, null);
        return posts;
    }
    
    @Override
    public Double calculateHotnessScore(Post post) {
        double likesScore = (post.getLikesCount() != null ? post.getLikesCount() : 0) * 0.5;
        double commentsScore = (post.getCommentsCount() != null ? post.getCommentsCount() : 0) * 0.3;
        double viewsScore = (post.getViewsCount() != null ? post.getViewsCount() : 0) * 0.2;
        double exposureScore = post.getExposureScore() != null ? post.getExposureScore() : 0.0;
        
        return likesScore + commentsScore + viewsScore + exposureScore;
    }
    
    @Override
    public int getTodayPostCount(Long userId) {
        QueryWrapper<Post> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        // 待审核(2)与正常(1)均视为已发布，用于任务进度；已删除为 0
        wrapper.in("status", java.util.Arrays.asList(1, 2));
        wrapper.apply("DATE(create_time) = CURDATE()");
        return (int) this.count(wrapper);
    }
    
    @Override
    public void updateExposureScoreOnInteraction(Long postId, String interactionType) {
        postCounterService.updateExposureScoreOnInteraction(postId, interactionType);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deletePostPhysically(Long postId) {
        if (postId == null) {
            return false;
        }
        Post post = postMapper.selectById(postId);
        if (post == null) {
            return false;
        }
        Long authorId = post.getUserId();

        List<Comment> comments = commentMapper.selectList(
                new QueryWrapper<Comment>().eq("post_id", postId));
        for (Comment c : comments) {
            if (c.getId() != null) {
                commentLikeMapper.delete(new QueryWrapper<CommentLike>().eq("comment_id", c.getId()));
            }
        }
        commentMapper.delete(new QueryWrapper<Comment>().eq("post_id", postId));

        postTagMapper.deleteByPostId(postId);
        postLikeMapper.delete(new QueryWrapper<PostLike>().eq("post_id", postId));
        postCollectMapper.delete(new QueryWrapper<PostCollect>().eq("post_id", postId));
        dailyTopicPostMapper.delete(new QueryWrapper<DailyTopicPost>().eq("post_id", postId));
        userPostInteractionMapper.delete(new QueryWrapper<UserPostInteraction>().eq("post_id", postId));

        redisCacheService.evictPostCache(postId);

        int rows = postMapper.deleteById(postId);
        if (authorId != null) {
            redisCacheService.clearUserStats(authorId);
        }
        return rows > 0;
    }
}
