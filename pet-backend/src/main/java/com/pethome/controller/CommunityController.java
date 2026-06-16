package com.pethome.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.common.Result;
import com.pethome.entity.Post;
import com.pethome.service.PostService;
import com.pethome.service.CommentService;
import com.pethome.service.UserFollowService;
import com.pethome.service.UserService;
import com.pethome.service.PostLikeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/community")
@Api(tags = "社区管理")
public class CommunityController {

    @Autowired
    private PostService postService;
    
    @Autowired
    private CommentService commentService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserFollowService userFollowService;
    
    @Autowired
    private PostLikeService postLikeService;
    
    @Autowired
    private com.pethome.service.PostCollectService postCollectService;
    
    @Autowired
    private com.pethome.service.BannerService bannerService;
    
    @Autowired(required = false)
    private com.pethome.service.TaskService taskService;
    
    @Autowired(required = false)
    private com.pethome.service.RedisCacheService redisCacheService;
    

    @GetMapping("/posts/video-feed")
    @ApiOperation("视频推荐流（类抖音）：仅视频帖，按热度分页")
    public Result<Map<String, Object>> getVideoFeed(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long userId) {
        try {
            Map<String, Object> resp = new HashMap<>();
            int pageNum = (page == null || page < 1) ? 1 : page;
            int pageSize = (size == null || size < 1) ? 10 : Math.min(size, 50);

            // 登录用户：按个性化推荐（视频标签）拉流；未登录：回退热度视频流
            if (userId != null && userId > 0) {
                List<Post> personalized = postRecommendationService.getRecommendPosts(userId, pageNum, pageSize, "视频");
                List<Long> postIds = personalized.stream()
                        .map(Post::getId)
                        .filter(java.util.Objects::nonNull)
                        .collect(java.util.stream.Collectors.toList());
                resp.put("postIds", postIds);
                resp.put("total", null);
                resp.put("hasMore", personalized.size() >= pageSize);
                resp.put("nextPage", pageNum + 1);
                resp.put("algorithm", "personalized-video");
            } else {
                Page<Post> postPage = new Page<>(pageNum, pageSize);
                IPage<Post> result = postService.getVideoFeed(postPage);
                List<Long> postIds = result.getRecords().stream()
                        .map(Post::getId)
                        .filter(java.util.Objects::nonNull)
                        .collect(java.util.stream.Collectors.toList());
                resp.put("postIds", postIds);
                resp.put("total", result.getTotal());
                resp.put("hasMore", result.getCurrent() < result.getPages());
                resp.put("nextPage", result.getCurrent() + 1);
                resp.put("algorithm", "hot-video");
            }
            return Result.success(resp);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取视频推荐流失败: " + e.getMessage());
        }
    }

    @GetMapping("/posts")
    @ApiOperation("获取帖子列表")
    public Result<Map<String, Object>> getPostList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long checkUserId) {
        try {
            Page<Post> postPage = new Page<>(page, size);
            IPage<Post> result = postService.getPostList(postPage, category, userId);
            
            // 如果提供了checkUserId，填充点赞状态
            if (checkUserId != null && result.getRecords() != null) {
                ((com.pethome.service.impl.PostServiceImpl) postService).fillUserInfoAndLikeStatus(result.getRecords(), checkUserId);
            } else if (result.getRecords() != null) {
                // 如果没有提供checkUserId，仍然填充用户信息，但不检查点赞状态
                ((com.pethome.service.impl.PostServiceImpl) postService).fillUserInfoAndLikeStatus(result.getRecords(), null);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("posts", result.getRecords());
            response.put("total", result.getTotal());
            response.put("pages", result.getPages());
            response.put("current", result.getCurrent());
            response.put("size", result.getSize());
            
            return Result.success(response);
        } catch (Exception e) {
            return Result.error("获取帖子列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/posts/{id}/mention-users")
    @ApiOperation("获取@提及用户列表（抖音式：输入@后弹出选择）排序：帖子作者>评论区用户>关注的人")
    public Result<List<Map<String, Object>>> getMentionUsers(
            @PathVariable Long id,
            @RequestParam(required = false) Long userId) {
        try {
            if (userId == null) {
                return Result.error("用户ID不能为空");
            }
            Post post = postService.getPostDetail(id, userId);
            if (post == null) return Result.error("帖子不存在");
            List<Map<String, Object>> result = new ArrayList<>();
            java.util.Set<Long> added = new java.util.HashSet<>();
            // 1. 帖子作者优先（排除自己）
            if (post.getUserId() != null && !post.getUserId().equals(userId)) {
                com.pethome.entity.User author = userService.getUserById(post.getUserId());
                if (author != null) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", author.getId());
                    m.put("userName", author.getNickname() != null ? author.getNickname() : author.getUsername());
                    m.put("userAvatar", author.getAvatar());
                    result.add(m);
                    added.add(author.getId());
                }
            }
            // 2. 评论区用户（按评论时间近的优先，去重）
            List<com.pethome.entity.Comment> comments = commentService.getCommentsByPostId(id, userId);
            if (comments != null) {
                for (com.pethome.entity.Comment c : comments) {
                    if (c.getUserId() == null || c.getUserId().equals(userId) || added.contains(c.getUserId())) continue;
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", c.getUserId());
                    m.put("userName", c.getUserName() != null ? c.getUserName() : "用户");
                    m.put("userAvatar", c.getUserAvatar());
                    result.add(m);
                    added.add(c.getUserId());
                }
            }
            // 3. 关注的人（补充，最多再取10个）
            List<com.pethome.entity.UserFollow> followList = userFollowService.getFollowingList(userId);
            int limit = 20 - result.size();
            if (followList != null && limit > 0) {
                for (com.pethome.entity.UserFollow f : followList) {
                    if (limit <= 0) break;
                    Long fid = f.getFollowingId();
                    if (fid == null || fid.equals(userId) || added.contains(fid)) continue;
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", fid);
                    m.put("userName", f.getFollowingName() != null ? f.getFollowingName() : "用户");
                    m.put("userAvatar", f.getFollowingAvatar());
                    result.add(m);
                    added.add(fid);
                    limit--;
                }
            }
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取@用户列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/posts/{id}/comments")
    @ApiOperation("获取帖子评论列表")
    public Result<List<com.pethome.entity.Comment>> getPostComments(
            @PathVariable Long id,
            @RequestParam(required = false) Long userId) {
        try {
            List<com.pethome.entity.Comment> comments = commentService.getCommentsByPostId(id, userId);
            return Result.success(comments);
        } catch (Exception e) {
            return Result.error("获取评论失败: " + e.getMessage());
        }
    }

    @PostMapping("/comments/{commentId}/like")
    @ApiOperation("点赞评论")
    public Result<Boolean> likeComment(@PathVariable Long commentId, @RequestParam(required = false) Long userId) {
        try {
            if (commentId == null || userId == null) {
                return Result.error("评论ID和用户ID不能为空");
            }
            boolean success = commentService.likeComment(commentId, userId);
            return Result.success(success);
        } catch (Exception e) {
            return Result.error("点赞评论失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/comments/{commentId}/like")
    @ApiOperation("取消点赞评论")
    public Result<Boolean> unlikeComment(@PathVariable Long commentId, @RequestParam(required = false) Long userId) {
        try {
            if (commentId == null || userId == null) {
                return Result.error("评论ID和用户ID不能为空");
            }
            boolean success = commentService.unlikeComment(commentId, userId);
            return Result.success(success);
        } catch (Exception e) {
            return Result.error("取消点赞评论失败: " + e.getMessage());
        }
    }

    @GetMapping("/comments/{commentId}/replies")
    @ApiOperation("获取评论的回复列表")
    public Result<List<com.pethome.entity.Comment>> getCommentReplies(
            @PathVariable Long commentId,
            @RequestParam(required = false) Long userId) {
        try {
            List<com.pethome.entity.Comment> replies = commentService.getRepliesByCommentId(commentId, userId);
            return Result.success(replies);
        } catch (Exception e) {
            return Result.error("获取回复失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/comments/{commentId}")
    @ApiOperation("删除评论（本人或帖子作者可删）")
    public Result<Boolean> deleteComment(@PathVariable Long commentId, @RequestParam Long userId) {
        try {
            com.pethome.entity.Comment comment = commentService.getById(commentId);
            if (comment == null) {
                return Result.error("评论不存在");
            }
            Post post = postService.getById(comment.getPostId());
            if (post == null) {
                return Result.error("帖子不存在");
            }
            boolean isCommentAuthor = comment.getUserId() != null && comment.getUserId().equals(userId);
            boolean isPostAuthor = post.getUserId() != null && post.getUserId().equals(userId);
            if (!isCommentAuthor && !isPostAuthor) {
                return Result.error("无权限删除此评论");
            }
            boolean success = commentService.deleteComment(commentId, userId);
            return Result.success(success);
        } catch (Exception e) {
            return Result.error("删除评论失败: " + e.getMessage());
        }
    }

    @PostMapping("/posts/{id}/comments")
    @ApiOperation("添加评论")
    public Result<com.pethome.entity.Comment> addComment(@PathVariable Long id, @RequestBody Map<String, Object> data) {
        try {
            com.pethome.entity.Comment comment = new com.pethome.entity.Comment();
            comment.setPostId(id);
            comment.setUserId(((Number) data.get("userId")).longValue());
            comment.setContent((String) data.get("content"));
            // 支持回复功能，如果传入了parentId则作为回复，否则为顶级评论
            Object parentIdObj = data.get("parentId");
            if (parentIdObj != null) {
                comment.setParentId(((Number) parentIdObj).longValue());
            } else {
                comment.setParentId(0L); // 默认0表示顶级评论
            }
            comment.setLikesCount(0);
            comment.setStatus(1);
            comment.setCreateTime(LocalDateTime.now());
            // 设置位置信息
            Object locationObj = data.get("location");
            if (locationObj != null) {
                comment.setLocation((String) locationObj);
            }
            
            com.pethome.entity.Comment result = commentService.addComment(comment);
            
            // CommentService已更新帖子评论数，无需重复调用
            
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("添加评论失败: " + e.getMessage());
        }
    }

    @GetMapping("/posts/{id}")
    @ApiOperation("获取帖子详情")
    public Result<Post> getPostDetail(@PathVariable Long id, @RequestParam(required = false) Long userId) {
        try {
            Post post = postService.getPostDetail(id, userId);
            if (post == null) {
                return Result.error("帖子不存在");
            }
            // 更新曝光分数（浏览）
            postService.updateExposureScoreOnInteraction(id, "view");
            return Result.success(post);
        } catch (Exception e) {
            return Result.error("获取帖子详情失败: " + e.getMessage());
        }
    }

    @Autowired
    private com.pethome.service.TagService tagService;
    
    @Autowired
    private com.pethome.mapper.PostTagMapper postTagMapper;
    
    @PostMapping("/posts")
    @ApiOperation("发布帖子")
    public Result<Post> publishPost(@RequestBody Map<String, Object> requestData) {
        try {
            Object userIdObj = requestData.get("userId");
            if (userIdObj == null) {
                return Result.error("请先登录");
            }
            long userIdVal;
            if (userIdObj instanceof Number) {
                userIdVal = ((Number) userIdObj).longValue();
            } else if (userIdObj instanceof String) {
                try {
                    userIdVal = Long.parseLong((String) userIdObj);
                } catch (NumberFormatException e) {
                    return Result.error("请先登录");
                }
            } else {
                return Result.error("请先登录");
            }
            if (userIdVal <= 0) {
                return Result.error("请先登录");
            }
            Post post = new Post();
            post.setUserId(userIdVal);
            post.setTitle((String) requestData.get("title"));
            post.setContent((String) requestData.get("content"));
            post.setCategory((String) requestData.getOrDefault("category", "推荐"));
            post.setImages((String) requestData.get("images"));
            post.setImageCount(requestData.get("imageCount") != null ? ((Number) requestData.get("imageCount")).intValue() : 0);
            
            // 先获取用户传入的封面图（如果有）
            String userCoverImage = (String) requestData.get("coverImage");
            
            // 处理视频信息
            Object videosObj = requestData.get("videos");
            if (videosObj != null) {
                String videosStr = null;
                if (videosObj instanceof String) {
                    videosStr = (String) videosObj;
                } else {
                    // 如果是对象或数组，转换为JSON字符串
                    try {
                        com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                        videosStr = objectMapper.writeValueAsString(videosObj);
                    } catch (Exception e) {
                        System.err.println("转换视频数据失败: " + e.getMessage());
                    }
                }
                post.setVideos(videosStr);
                
                // 如果有视频但没有用户指定的封面图，使用视频封面作为封面图
                if ((userCoverImage == null || userCoverImage.trim().isEmpty()) && 
                    (post.getCoverImage() == null || post.getCoverImage().trim().isEmpty())) {
                    try {
                        if (videosStr != null && !videosStr.trim().isEmpty()) {
                            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                            java.util.List<Map<String, Object>> videosList = objectMapper.readValue(videosStr, 
                                objectMapper.getTypeFactory().constructCollectionType(java.util.List.class, java.util.Map.class));
                            if (videosList != null && !videosList.isEmpty() && videosList.get(0).containsKey("thumb")) {
                                String videoThumb = (String) videosList.get(0).get("thumb");
                                if (videoThumb != null && !videoThumb.trim().isEmpty()) {
                                    post.setCoverImage(videoThumb);
                                }
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("解析视频封面失败: " + e.getMessage());
                    }
                }
            }
            
            // 如果用户指定了封面图，优先使用用户指定的
            if (userCoverImage != null && !userCoverImage.trim().isEmpty()) {
                post.setCoverImage(userCoverImage);
            }
            
            post.setCreateTime(LocalDateTime.now());
            post.setUpdateTime(LocalDateTime.now());
            post.setStatus(2); // 新帖子默认为待审核，管理员审核通过后变为正常(1)
            
            // 保存帖子
            boolean success = postService.save(post);
            if (!success) {
                return Result.error("发布帖子失败");
            }
            
            // 清除用户统计缓存，确保统计数据实时更新
            Long userId = post.getUserId();
            if (userId != null && redisCacheService != null) {
                redisCacheService.clearUserStats(userId);
                System.out.println("发布帖子后清除用户统计缓存，用户ID: " + userId);
            }
            
            // 更新任务进度：发布内容任务
            try {
                if (userId != null) {
                    // 获取用户今日已发布的帖子数量
                    int todayPostCount = postService.getTodayPostCount(userId);
                    if (taskService != null) {
                        taskService.updateTaskProgress(userId, "post_content", todayPostCount);
                        System.out.println("更新发布内容任务进度，用户ID: " + userId + ", 今日发布数: " + todayPostCount);
                    }
                }
            } catch (Exception e) {
                // 任务进度更新失败不影响发布流程
                System.err.println("更新发布内容任务进度失败: " + e.getMessage());
            }
            
            // 处理标签
            if (requestData.containsKey("tags")) {
                Object tagsObj = requestData.get("tags");
                List<String> tagNames = new ArrayList<>();
                
                if (tagsObj instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Object> tagsList = (List<Object>) tagsObj;
                    for (Object tag : tagsList) {
                        if (tag != null) {
                            tagNames.add(tag.toString().trim());
                        }
                    }
                } else if (tagsObj instanceof String) {
                    String tagsStr = (String) tagsObj;
                    if (!tagsStr.isEmpty()) {
                        String[] tagsArray = tagsStr.split(",");
                        for (String tag : tagsArray) {
                            if (!tag.trim().isEmpty()) {
                                tagNames.add(tag.trim());
                            }
                        }
                    }
                }
                
                // 批量获取或创建标签
                List<com.pethome.entity.Tag> tags = tagService.batchGetOrCreateTags(tagNames);
                
                // 建立帖子与标签的关联
                List<com.pethome.entity.PostTag> postTags = new ArrayList<>();
                for (com.pethome.entity.Tag tag : tags) {
                    // 增加标签热度（Redis）
                    tagService.incrementTagHotness(tag.getName());
                    
                    // 创建关联
                    com.pethome.entity.PostTag postTag = new com.pethome.entity.PostTag();
                    postTag.setPostId(post.getId());
                    postTag.setTagId(tag.getId());
                    postTags.add(postTag);
                }
                
                if (!postTags.isEmpty()) {
                    postTagMapper.batchInsert(postTags);
                }

                // 同步到 post.tags，便于快速检索/推荐筛选
                if (!tagNames.isEmpty()) {
                    post.setTags(String.join(",", tagNames));
                    postService.updateById(post);
                }
            }
            
            return Result.success(post);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("发布帖子失败: " + e.getMessage());
        }
    }

    @PutMapping("/posts/{id}")
    @ApiOperation("更新帖子")
    public Result<Post> updatePost(@PathVariable Long id, @RequestBody Post post, @RequestParam Long userId) {
        try {
            Post existingPost = postService.getById(id);
            if (existingPost == null || !existingPost.getUserId().equals(userId)) {
                return Result.error("无权限修改此帖子");
            }
            
            post.setId(id);
            post.setUpdateTime(LocalDateTime.now());
            boolean success = postService.updateById(post);
            return Result.success(success ? post : null);
        } catch (Exception e) {
            return Result.error("更新帖子失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/posts/{id}")
    @ApiOperation("删除帖子")
    public Result<Boolean> deletePost(@PathVariable Long id, @RequestParam Long userId) {
        try {
            Post post = postService.getById(id);
            if (post == null || !post.getUserId().equals(userId)) {
                return Result.error("无权限删除此帖子");
            }
            
            post.setStatus(0);
            post.setUpdateTime(LocalDateTime.now());
            boolean success = postService.updateById(post);
            if (success && redisCacheService != null) {
                redisCacheService.clearUserStats(post.getUserId());
            }
            return Result.success(success);
        } catch (Exception e) {
            return Result.error("删除帖子失败: " + e.getMessage());
        }
    }

    @PostMapping("/posts/{id}/like")
    @ApiOperation("点赞帖子")
    public Result<Boolean> likePost(@PathVariable Long id, @RequestParam Long userId) {
        try {
            // 参数验证
            if (id == null) {
                return Result.error("帖子ID不能为空");
            }
            if (userId == null) {
                return Result.error("用户ID不能为空，请先登录");
            }
            
            boolean success = postLikeService.likePost(id, userId);
            if (success) {
                // 更新曝光分数
                postService.updateExposureScoreOnInteraction(id, "like");
            }
            return Result.success(success);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("点赞失败异常详情:");
            System.err.println("帖子ID: " + id);
            System.err.println("用户ID: " + userId);
            System.err.println("异常类型: " + e.getClass().getName());
            System.err.println("异常消息: " + e.getMessage());
            return Result.error("点赞失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/posts/{id}/like")
    @ApiOperation("取消点赞帖子")
    public Result<Boolean> unlikePost(@PathVariable Long id, @RequestParam Long userId) {
        try {
            // 参数验证
            if (id == null) {
                return Result.error("帖子ID不能为空");
            }
            if (userId == null) {
                return Result.error("用户ID不能为空，请先登录");
            }
            
            boolean success = postLikeService.unlikePost(id, userId);
            return Result.success(success);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("取消点赞失败异常详情:");
            System.err.println("帖子ID: " + id);
            System.err.println("用户ID: " + userId);
            System.err.println("异常类型: " + e.getClass().getName());
            System.err.println("异常消息: " + e.getMessage());
            return Result.error("取消点赞失败: " + e.getMessage());
        }
    }

    @PostMapping("/posts/{id}/collect")
    @ApiOperation("收藏帖子")
    public Result<Boolean> collectPost(@PathVariable Long id, @RequestParam Long userId) {
        try {
            // 参数验证
            if (id == null) {
                return Result.error("帖子ID不能为空");
            }
            if (userId == null) {
                return Result.error("用户ID不能为空，请先登录");
            }
            
            boolean success = postCollectService.collectPost(id, userId);
            if (success) {
                return Result.success(true);
            } else {
                return Result.error("已经收藏过了");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("收藏失败异常详情:");
            System.err.println("帖子ID: " + id);
            System.err.println("用户ID: " + userId);
            System.err.println("异常类型: " + e.getClass().getName());
            System.err.println("异常消息: " + e.getMessage());
            return Result.error("收藏失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/posts/{id}/collect")
    @ApiOperation("取消收藏帖子")
    public Result<Boolean> uncollectPost(@PathVariable Long id, @RequestParam Long userId) {
        try {
            // 参数验证
            if (id == null) {
                return Result.error("帖子ID不能为空");
            }
            if (userId == null) {
                return Result.error("用户ID不能为空，请先登录");
            }
            
            boolean success = postCollectService.uncollectPost(id, userId);
            return Result.success(success);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("取消收藏失败异常详情:");
            System.err.println("帖子ID: " + id);
            System.err.println("用户ID: " + userId);
            System.err.println("异常类型: " + e.getClass().getName());
            System.err.println("异常消息: " + e.getMessage());
            return Result.error("取消收藏失败: " + e.getMessage());
        }
    }

    @GetMapping("/posts/{id}/collect/status")
    @ApiOperation("检查收藏状态")
    public Result<Boolean> checkCollectStatus(@PathVariable Long id, @RequestParam Long userId) {
        try {
            if (id == null || userId == null) {
                return Result.error("帖子ID和用户ID不能为空");
            }
            boolean isCollected = postCollectService.isCollected(id, userId);
            return Result.success(isCollected);
        } catch (Exception e) {
            return Result.error("检查收藏状态失败: " + e.getMessage());
        }
    }

    @GetMapping("/posts/collected")
    @ApiOperation("获取用户收藏的帖子列表")
    public Result<Map<String, Object>> getCollectedPosts(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String type) {
        try {
            if (userId == null) {
                return Result.error("用户ID不能为空");
            }
            
            // 使用真实服务获取收藏列表
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<Post> postPage = 
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size);
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<Post> result = 
                postCollectService.getCollectedPosts(userId, postPage, type);
            
            // 填充用户信息和点赞状态
            ((com.pethome.service.impl.PostServiceImpl) postService).fillUserInfoAndLikeStatus(result.getRecords(), userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("posts", result.getRecords());
            response.put("total", result.getTotal());
            response.put("pages", result.getPages());
            response.put("current", result.getCurrent());
            response.put("size", result.getSize());
            
            return Result.success(response);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取收藏列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/posts/hot")
    @ApiOperation("获取热门帖子")
    public Result<List<Post>> getHotPosts(@RequestParam(defaultValue = "10") Integer limit) {
        try {
            List<Post> hotPosts = postService.getHotPosts(limit);
            return Result.success(hotPosts);
        } catch (Exception e) {
            return Result.error("获取热门帖子失败: " + e.getMessage());
        }
    }
    
    @Autowired
    private com.pethome.service.PostRecommendationService postRecommendationService;
    
    @GetMapping("/posts/feed")
    @ApiOperation("获取帖子Feed（置顶+热门+最新+关注区+发现区）")
    public Result<Map<String, List<Post>>> getPostFeed(
            @RequestParam(defaultValue = "5") Integer topLimit,
            @RequestParam(defaultValue = "10") Integer hotLimit,
            @RequestParam(defaultValue = "10") Integer latestLimit,
            @RequestParam(defaultValue = "20") Integer followLimit,
            @RequestParam(defaultValue = "20") Integer exploreLimit,
            @RequestParam(required = false) Long userId) {
        try {
            Map<String, List<Post>> feed = new HashMap<>();
            
            // 获取置顶帖
            List<Post> topPosts = postService.getTopPosts(topLimit);
            feed.put("topPosts", topPosts);
            
            // 获取热门帖（按热度分数排序）
            List<Post> hotPosts = postService.getHotPostsByScore(hotLimit);
            feed.put("hotPosts", hotPosts);
            
            // 获取最新帖
            List<Post> latestPosts = postService.getLatestPosts(latestLimit);
            feed.put("latestPosts", latestPosts);
            
            // 获取关注区帖子（个性化排序）
            if (userId != null) {
                List<Post> followPosts = postRecommendationService.getFollowFeedPosts(userId, followLimit);
                feed.put("followPosts", followPosts);
            } else {
                feed.put("followPosts", new ArrayList<Post>());
            }
            
            // 获取发现区帖子（智能推荐）
            if (userId != null) {
                List<Post> explorePosts = postRecommendationService.getExplorePosts(userId, exploreLimit);
                feed.put("explorePosts", explorePosts);
            } else {
                // 如果未登录，返回普通热门帖
                List<Post> explorePosts = postService.getHotPostsByScore(exploreLimit);
                feed.put("explorePosts", explorePosts);
            }
            
            return Result.success(feed);
        } catch (Exception e) {
            return Result.error("获取帖子Feed失败: " + e.getMessage());
        }
    }

    /**
     * 发现页规则型推荐（小红书风格）
     * score = 兴趣匹配(+5) + 点赞×2 + 收藏×3 + 评论×4 + 浏览×0.2 + 新帖24h(+20)
     */
    @GetMapping("/posts/recommend")
    @ApiOperation("发现页推荐帖子（规则型排序）")
    public Result<Map<String, Object>> getRecommendPosts(
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String tag) {
        try {
            List<Post> posts = postRecommendationService.getRecommendPosts(userId, page, size, tag);
            Map<String, Object> response = new HashMap<>();
            response.put("posts", posts);
            response.put("current", page);
            response.put("size", size);
            response.put("tag", tag);
            return Result.success(response);
        } catch (Exception e) {
            return Result.error("获取推荐帖子失败: " + e.getMessage());
        }
    }

    @GetMapping("/posts/following")
    @ApiOperation("获取关注用户的帖子")
    public Result<Map<String, Object>> getFollowingPosts(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        try {
            Page<Post> postPage = new Page<>(page, size);
            IPage<Post> result = postService.getFollowingPosts(userId, postPage);
            
            Map<String, Object> response = new HashMap<>();
            response.put("posts", result.getRecords());
            response.put("total", result.getTotal());
            response.put("pages", result.getPages());
            
            return Result.success(response);
        } catch (Exception e) {
            return Result.error("获取关注用户帖子失败: " + e.getMessage());
        }
    }

    @GetMapping("/daily-topics")
    @ApiOperation("获取每日专题")
    public Result<Map<String, Object>> getDailyTopics() {
        try {
            // 获取Banner作为轮播图（最多5个）
            List<com.pethome.entity.Banner> allBanners = bannerService.getAllBanners();
            List<com.pethome.entity.Banner> banners = allBanners != null && allBanners.size() > 5 
                ? allBanners.subList(0, 5) 
                : (allBanners != null ? allBanners : new ArrayList<>());
            
            // 获取热门帖子作为文章（最多5个）
            List<Post> hotPosts = postService.getHotPosts(5);
            
            // 获取今日主题（根据星期几，0=周日，1=周一...6=周六）
            String[] themes = {
                "猫咪护理", "狗狗训练", "宠物健康", "宠物美容", 
                "宠物饮食", "宠物玩具", "宠物训练"
            };
            // getDayOfWeek().getValue() 返回 1-7 (周一到周日)，需要转换为 0-6
            int dayOfWeek = (java.time.LocalDate.now().getDayOfWeek().getValue() - 1) % 7;
            String todayTheme = themes[dayOfWeek];
            
            Map<String, Object> response = new HashMap<>();
            response.put("banners", banners);
            response.put("posts", hotPosts);
            response.put("todayTheme", todayTheme);
            response.put("date", java.time.LocalDate.now().toString());
            
            return Result.success(response);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取每日专题失败: " + e.getMessage());
        }
    }

}
