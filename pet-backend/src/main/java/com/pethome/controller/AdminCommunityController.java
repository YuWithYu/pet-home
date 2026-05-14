package com.pethome.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.common.Result;
import com.pethome.entity.Banner;
import com.pethome.entity.Comment;
import com.pethome.entity.Post;
import com.pethome.entity.User;
import com.pethome.mapper.UserMapper;
import com.pethome.service.BannerService;
import com.pethome.service.CommentService;
import com.pethome.service.PostService;
import com.pethome.service.TaskService;
import com.pethome.service.UserFollowService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员社区管理控制器
 */
@RestController
@RequestMapping("/api/admin/community")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
@Api(tags = "管理员社区管理")
public class AdminCommunityController {

    @Autowired
    private PostService postService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserFollowService userFollowService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private BannerService bannerService;

    @Autowired(required = false)
    private TaskService taskService;

    /**
     * 获取社区统计数据
     */
    @GetMapping("/statistics")
    @ApiOperation("获取社区统计数据")
    public Result<Map<String, Object>> getStatistics() {
        try {
            Map<String, Object> statistics = new HashMap<>();

            // 总帖子数
            long totalPosts = postService.count();
            statistics.put("totalPosts", totalPosts);

            // 活跃帖子数（status = 1，已审核通过）
            QueryWrapper<Post> activePostWrapper = new QueryWrapper<>();
            activePostWrapper.eq("status", 1);
            long activePosts = postService.count(activePostWrapper);
            statistics.put("activePosts", activePosts);

            // 待审核帖子数（status = 2）
            QueryWrapper<Post> pendingPostWrapper = new QueryWrapper<>();
            pendingPostWrapper.eq("status", 2);
            long pendingPosts = postService.count(pendingPostWrapper);
            statistics.put("pendingPosts", pendingPosts);

            // 总评论数
            long totalComments = commentService.count();
            statistics.put("totalComments", totalComments);

            // 关注关系数
            long totalFollows = userFollowService.count();
            statistics.put("totalFollows", totalFollows);

            return Result.success(statistics);
        } catch (Exception e) {
            return Result.error("获取统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有帖子（管理员）
     */
    @GetMapping("/posts")
    @ApiOperation("获取所有帖子（管理员）")
    public Result<Map<String, Object>> getAdminPosts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String category) {
        try {
            Page<Post> postPage = new Page<>(page, size);
            IPage<Post> result = postService.getAdminPostList(postPage, keyword, status, category);

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

    /**
     * 更新帖子状态
     */
    @PutMapping("/posts/{id}/status")
    @ApiOperation("更新帖子状态")
    public Result<Void> updatePostStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        try {
            Post post = postService.getById(id);
            if (post == null) {
                return Result.error("帖子不存在");
            }
            post.setStatus(status);
            post.setUpdateTime(LocalDateTime.now());
            postService.updateById(post);

            // 审核/下架等会改变「今日发帖」可见数量，同步每日任务「发布内容」进度（修复：仅待审时进度为 0、通过后仍不更新）
            if (post.getUserId() != null && taskService != null) {
                try {
                    int todayCount = postService.getTodayPostCount(post.getUserId());
                    taskService.updateTaskProgress(post.getUserId(), "post_content", todayCount);
                } catch (Exception ex) {
                    System.err.println("管理员更新帖子状态后同步发布任务进度失败: " + ex.getMessage());
                }
            }
            return Result.success(null);
        } catch (Exception e) {
            return Result.error("更新帖子状态失败: " + e.getMessage());
        }
    }

    /**
     * 设置帖子置顶
     */
    @PutMapping("/posts/{id}/top")
    @ApiOperation("设置帖子置顶")
    public Result<Void> updatePostTop(
            @PathVariable Long id,
            @RequestParam Boolean isTop) {
        try {
            Post post = postService.getById(id);
            if (post == null) {
                return Result.error("帖子不存在");
            }
            post.setIsTop(isTop);
            post.setUpdateTime(LocalDateTime.now());
            postService.updateById(post);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error("设置置顶失败: " + e.getMessage());
        }
    }

    /**
     * 设置帖子热门
     */
    @PutMapping("/posts/{id}/hot")
    @ApiOperation("设置帖子热门")
    public Result<Void> updatePostHot(
            @PathVariable Long id,
            @RequestParam Boolean isHot) {
        try {
            Post post = postService.getById(id);
            if (post == null) {
                return Result.error("帖子不存在");
            }
            post.setIsHot(isHot);
            post.setUpdateTime(LocalDateTime.now());
            postService.updateById(post);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error("设置热门失败: " + e.getMessage());
        }
    }

    /**
     * 删除帖子（管理员）
     */
    @DeleteMapping("/posts/{id}")
    @ApiOperation("删除帖子（管理员，物理删除并级联清理关联数据）")
    public Result<Void> deletePost(@PathVariable Long id) {
        try {
            boolean removed = postService.deletePostPhysically(id);
            if (!removed) {
                return Result.error("帖子不存在或已删除");
            }
            return Result.success(null);
        } catch (Exception e) {
            return Result.error("删除帖子失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有评论（管理员）
     */
    @GetMapping("/comments")
    @ApiOperation("获取所有评论（管理员）")
    public Result<Map<String, Object>> getAdminComments(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long postId) {
        try {
            Page<Comment> commentPage = new Page<>(page, size);
            IPage<Comment> result = commentService.getAdminCommentList(commentPage, keyword, status, postId);

            // 填充用户信息
            List<Comment> comments = result.getRecords();
            fillCommentUserInfo(comments);

            Map<String, Object> response = new HashMap<>();
            response.put("comments", comments);
            response.put("total", result.getTotal());
            response.put("pages", result.getPages());
            response.put("current", result.getCurrent());
            response.put("size", result.getSize());

            return Result.success(response);
        } catch (Exception e) {
            return Result.error("获取评论列表失败: " + e.getMessage());
        }
    }

    /**
     * 更新评论状态
     */
    @PutMapping("/comments/{id}/status")
    @ApiOperation("更新评论状态")
    public Result<Void> updateCommentStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        try {
            Comment comment = commentService.getById(id);
            if (comment == null) {
                return Result.error("评论不存在");
            }
            comment.setStatus(status);
            comment.setUpdateTime(LocalDateTime.now());
            commentService.updateById(comment);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error("更新评论状态失败: " + e.getMessage());
        }
    }

    /**
     * 删除评论（管理员）
     */
    @DeleteMapping("/comments/{id}")
    @ApiOperation("删除评论（管理员）")
    public Result<Void> deleteComment(@PathVariable Long id) {
        try {
            Comment comment = commentService.getById(id);
            if (comment == null) {
                return Result.error("评论不存在");
            }
            // 软删除：设置状态为0
            comment.setStatus(0);
            comment.setUpdateTime(LocalDateTime.now());
            commentService.updateById(comment);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error("删除评论失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户的所有帖子
     */
    @GetMapping("/users/{id}/posts")
    @ApiOperation("获取用户的所有帖子")
    public Result<List<Post>> getUserPosts(@PathVariable Long id) {
        try {
            List<Post> posts = postService.getUserPosts(id);
            return Result.success(posts);
        } catch (Exception e) {
            return Result.error("获取用户帖子失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户的所有评论
     */
    @GetMapping("/users/{id}/comments")
    @ApiOperation("获取用户的所有评论")
    public Result<List<Comment>> getUserComments(@PathVariable Long id) {
        try {
            List<Comment> comments = commentService.getUserComments(id);
            fillCommentUserInfo(comments);
            return Result.success(comments);
        } catch (Exception e) {
            return Result.error("获取用户评论失败: " + e.getMessage());
        }
    }

    /**
     * 填充评论的用户信息
     */
    private void fillCommentUserInfo(List<Comment> comments) {
        if (comments == null || comments.isEmpty()) {
            return;
        }
        for (Comment comment : comments) {
            if (comment.getUserId() != null) {
                User user = userMapper.selectById(comment.getUserId());
                if (user != null) {
                    // 设置用户名（优先使用昵称）
                    String displayName = null;
                    if (user.getNickname() != null && !user.getNickname().trim().isEmpty()) {
                        displayName = user.getNickname().trim();
                    }
                    comment.setUserName(displayName != null && !displayName.isEmpty() ? displayName : "用户" + comment.getUserId());
                    
                    // 设置用户头像（排除临时路径）
                    String avatar = (user.getAvatar() != null && !user.getAvatar().isEmpty()) ? user.getAvatar() : null;
                    if (avatar != null && (avatar.contains("/tmp/") || avatar.contains("/__tmp__/") || 
                        avatar.startsWith("http://tmp/") || avatar.startsWith("http://__tmp__/") ||
                        avatar.startsWith("https://tmp/") || avatar.startsWith("https://__tmp__/"))) {
                        avatar = null;
                    }
                    comment.setUserAvatar(avatar != null ? avatar : "/static/images/garfield-default-avatar.png");
                }
            }
        }
    }

    /**
     * 获取每日专题帖子列表
     */
    @GetMapping("/daily-topics/posts")
    @ApiOperation("获取每日专题帖子列表")
    public Result<Map<String, Object>> getDailyTopicPosts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        try {
            Page<Post> postPage = new Page<>(page, size);
            // 搜索包含"每日专题"标签的帖子
            QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
            if (keyword != null && !keyword.isEmpty()) {
                queryWrapper.and(wrapper -> wrapper
                    .like("title", keyword)
                    .or()
                    .like("content", keyword)
                );
            }
            // 筛选包含"每日专题"标签的帖子（支持 #每日专题 和 每日专题 两种格式）
            queryWrapper.and(wrapper -> wrapper
                .like("tags", "每日专题")
                .or()
                .like("tags", "#每日专题")
            );
            queryWrapper.orderByDesc("create_time");
            
            IPage<Post> result = postService.page(postPage, queryWrapper);
            
            // 调试日志
            System.out.println("每日专题帖子查询结果: total=" + result.getTotal() + ", records=" + (result.getRecords() != null ? result.getRecords().size() : 0));
            
            // 填充用户信息
            if (result.getRecords() != null && !result.getRecords().isEmpty()) {
                for (Post post : result.getRecords()) {
                    if (post.getUserId() != null) {
                        User user = userMapper.selectById(post.getUserId());
                        if (user != null) {
                            String displayName = null;
                            if (user.getNickname() != null && !user.getNickname().trim().isEmpty()) {
                                displayName = user.getNickname().trim();
                            }
                            post.setUserName(displayName != null && !displayName.isEmpty() ? displayName : "用户" + post.getUserId());
                            String avatar = (user.getAvatar() != null && !user.getAvatar().isEmpty()) ? user.getAvatar() : null;
                            if (avatar != null && (avatar.contains("/tmp/") || avatar.contains("/__tmp__/"))) {
                                avatar = null;
                            }
                            post.setUserAvatar(avatar != null ? avatar : "/static/images/garfield-default-avatar.png");
                        }
                    }
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("posts", result.getRecords());
            response.put("total", result.getTotal());
            response.put("pages", result.getPages());
            response.put("current", result.getCurrent());
            response.put("size", result.getSize());

            return Result.success(response);
        } catch (Exception e) {
            return Result.error("获取每日专题帖子失败: " + e.getMessage());
        }
    }

    /**
     * 获取每日专题Banner列表
     */
    @GetMapping("/daily-topics/banners")
    @ApiOperation("获取每日专题Banner列表")
    public Result<List<Banner>> getDailyTopicBanners() {
        try {
            List<Banner> allBanners = bannerService.getAllBannersForManagement();
            // 调试日志
            System.out.println("每日专题Banner查询结果: count=" + (allBanners != null ? allBanners.size() : 0));
            // 可以根据需要筛选，这里返回所有Banner
            return Result.success(allBanners != null ? allBanners : new ArrayList<>());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取每日专题Banner失败: " + e.getMessage());
        }
    }

    /**
     * 创建或更新每日专题Banner
     */
    @PostMapping("/daily-topics/banners")
    @ApiOperation("创建或更新每日专题Banner")
    public Result<Banner> saveDailyTopicBanner(@RequestBody Banner banner) {
        try {
            if (banner.getId() != null) {
                banner.setUpdateTime(LocalDateTime.now());
                Banner updated = bannerService.updateBanner(banner);
                return Result.success(updated);
            } else {
                banner.setCreateTime(LocalDateTime.now());
                banner.setUpdateTime(LocalDateTime.now());
                Banner created = bannerService.createBanner(banner);
                return Result.success(created);
            }
        } catch (Exception e) {
            return Result.error("保存每日专题Banner失败: " + e.getMessage());
        }
    }

    /**
     * 删除每日专题Banner
     */
    @DeleteMapping("/daily-topics/banners/{id}")
    @ApiOperation("删除每日专题Banner")
    public Result<Void> deleteDailyTopicBanner(@PathVariable Long id) {
        try {
            boolean success = bannerService.deleteBanner(id);
            if (success) {
                return Result.success(null);
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            return Result.error("删除每日专题Banner失败: " + e.getMessage());
        }
    }
}
