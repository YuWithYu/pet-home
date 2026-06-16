package com.pethome.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pethome.entity.Comment;
import com.pethome.entity.CommentLike;
import com.pethome.entity.DailyTopicPost;
import com.pethome.entity.Post;
import com.pethome.entity.PostCollect;
import com.pethome.entity.PostLike;
import com.pethome.entity.User;
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
import com.pethome.mapper.UserMapper;
import com.pethome.mapper.UserPostInteractionMapper;
import com.pethome.service.PostService;
import com.pethome.service.PostLikeService;
import com.pethome.service.RedisCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {
    
    @Autowired
    private PostMapper postMapper;
    
    @Autowired
    private PostLikeMapper postLikeMapper;
    
    @Autowired
    @Lazy
    private PostLikeService postLikeService;
    
    @Autowired
    private UserFollowMapper userFollowMapper;
    
    @Autowired
    private UserMapper userMapper;
    
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
        Long viewCount = redisCacheService.incrementPostView(postId);

        if (viewCount == null || viewCount == 1) {
            Post post = postMapper.selectById(postId);
            if (post != null) {
                Integer currentViews = post.getViewsCount() != null ? post.getViewsCount() : 0;
                redisCacheService.setPostViewCount(postId, currentViews + 1);
                viewCount = (long) (currentViews + 1);
            }
        }

        if (viewCount != null) {
            Post post = postMapper.selectById(postId);
            if (post != null) {
                post.setViewsCount(viewCount.intValue());
                postMapper.updateById(post);
            }
        }
    }
    
    @Override
    public void updateLikesCount(Long postId, int delta) {
        // 1. 更新 Redis 缓存
        if (delta > 0) {
            redisCacheService.incrementPostLike(postId);
        } else if (delta < 0) {
            redisCacheService.decrementPostLike(postId);
        }
        
        Integer newCount = redisCacheService.getPostLikeCount(postId);
        if (newCount == null) {
            Post post = postMapper.selectById(postId);
            if (post != null) {
                Integer currentLikes = post.getLikesCount() != null ? post.getLikesCount() : 0;
                newCount = Math.max(0, currentLikes + delta);
                redisCacheService.setPostLikeCount(postId, newCount);
            }
        }
        
        // 2. 立即回写数据库，避免仅依赖定时同步导致刷新后点赞数丢失
        if (newCount != null) {
            Post post = postMapper.selectById(postId);
            if (post != null) {
                post.setLikesCount(newCount);
                postMapper.updateById(post);
            }
        }
    }
    
    @Override
    public void updateCommentsCount(Long postId, int delta) {
        Post post = postMapper.selectById(postId);
        if (post != null) {
            post.setCommentsCount(Math.max(0, post.getCommentsCount() + delta));
            postMapper.updateById(post);
            
            // 更新Redis缓存
            redisCacheService.incrementPostStat(postId, "commentCount", delta);
        }
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
     */
    public void fillUserInfoAndLikeStatus(List<Post> posts, Long currentUserId) {
        for (Post post : posts) {
            // 从数据库获取用户信息
            User user = userMapper.selectById(post.getUserId());
            if (user != null) {
                // 只使用昵称，如果昵称为空则使用默认值（不使用手机号）
                String displayName = null;
                if (user.getNickname() != null && !user.getNickname().trim().isEmpty()) {
                    displayName = user.getNickname().trim();
                }
                // 如果昵称为空，使用默认格式，绝不使用手机号（username）
                post.setUserName(displayName != null && !displayName.isEmpty() ? displayName : "用户" + post.getUserId());
                // 优先使用avatar字段，但需要排除临时路径
                String avatar = (user.getAvatar() != null && !user.getAvatar().isEmpty()) ? user.getAvatar() : null;
                
                // 检查是否是临时路径（tmp路径）
                if (avatar != null && (avatar.contains("/tmp/") || avatar.contains("/__tmp__/") || 
                    avatar.startsWith("http://tmp/") || avatar.startsWith("http://__tmp__/") ||
                    avatar.startsWith("https://tmp/") || avatar.startsWith("https://__tmp__/"))) {
                    // 如果是临时路径，使用默认头像
                    avatar = null;
                    System.out.println("检测到用户头像为临时路径，使用默认头像。用户ID: " + user.getId() + ", 临时路径: " + user.getAvatar());
                }
                
                // 使用正确的默认头像路径（默认头像可以使用静态资源路径）
                // 注意：用户上传的头像应保存在 /upload/ 目录，只有默认头像使用 /static/
                post.setUserAvatar(avatar != null ? avatar : "/static/images/garfield-default-avatar.png");
            }
            
            // 视频帖无封面时从 videos[0] 的 thumb/customThumb/cover 补全，方便列表展示
            if ((post.getCoverImage() == null || post.getCoverImage().trim().isEmpty()) && post.getVideos() != null && !post.getVideos().trim().isEmpty()) {
                try {
                    com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
                    java.util.List<?> list = om.readValue(post.getVideos(), java.util.List.class);
                    if (list != null && !list.isEmpty() && list.get(0) instanceof java.util.Map) {
                        @SuppressWarnings("unchecked")
                        java.util.Map<String, Object> first = (java.util.Map<String, Object>) list.get(0);
                        String thumb = first.get("thumb") != null ? String.valueOf(first.get("thumb")).trim() : null;
                        if (thumb == null || thumb.isEmpty()) thumb = first.get("customThumb") != null ? String.valueOf(first.get("customThumb")).trim() : null;
                        if (thumb == null || thumb.isEmpty()) thumb = first.get("cover") != null ? String.valueOf(first.get("cover")).trim() : null;
                        if (thumb != null && !thumb.isEmpty()) post.setCoverImage(thumb);
                    }
                } catch (Exception ignored) {}
            }
            
            // 检查当前用户是否已点赞该帖子
            if (currentUserId != null) {
                try {
                    boolean isLiked = postLikeService.isLiked(post.getId(), currentUserId);
                    post.setIsLiked(isLiked);
                } catch (Exception e) {
                    // 如果检查失败，默认设置为false
                    post.setIsLiked(false);
                    System.out.println("检查点赞状态失败: " + e.getMessage());
                }
            } else {
                // 如果用户未登录，默认设置为false
                post.setIsLiked(false);
            }
        }
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
        Post post = postMapper.selectById(postId);
        if (post == null) {
            return;
        }
        
        // 根据不同互动类型增加曝光分数
        double increment = 0.0;
        switch (interactionType.toLowerCase()) {
            case "like":
                increment = 2.0; // 点赞增加2分
                break;
            case "comment":
                increment = 3.0; // 评论增加3分
                break;
            case "view":
                increment = 0.1; // 浏览增加0.1分
                break;
            default:
                increment = 1.0; // 其他互动增加1分
        }
        
        double currentScore = post.getExposureScore() != null ? post.getExposureScore() : 0.0;
        post.setExposureScore(currentScore + increment);
        postMapper.updateById(post);
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