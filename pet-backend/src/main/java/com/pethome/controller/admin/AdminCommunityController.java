package com.pethome.controller.admin;


import com.pethome.common.BusinessException;
import com.pethome.common.SafeCall;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.benmanes.caffeine.cache.Cache;
import com.pethome.common.Result;
import com.pethome.entity.Banner;
import com.pethome.entity.Comment;
import com.pethome.entity.Post;
import com.pethome.entity.User;
import com.pethome.service.user.UserService;
import com.pethome.service.common.BannerService;
import com.pethome.service.community.CommentService;
import com.pethome.service.community.PostService;
import com.pethome.service.admin.TaskService;
import com.pethome.service.user.UserFollowService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 管理员社区管理控制器
 */
@RestController
@RequestMapping("/api/admin/community")
@Tag(name = "管理员社区管理")
public class AdminCommunityController {

    private static final Logger log = LoggerFactory.getLogger(AdminCommunityController.class);

    @Autowired
    private PostService postService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserFollowService userFollowService;

    @Autowired
    private UserService userService;

    @Autowired
    private BannerService bannerService;

    @Autowired(required = false)
    private TaskService taskService;

    /** 社区统计本地缓存：Caffeine 单飞 + 60 秒过期，写操作显式失效 */
    @Autowired
    @Qualifier("communityStatisticsCache")
    private Cache<String, Map<String, Object>> communityStatisticsCache;

    /**
     * 获取社区统计数据
     * 性能：帖子三类计数（总数/已通过/待审核）合并为一条 GROUP BY，替代三次全表 COUNT；
     * 结果走本地缓存，审核/删帖等写操作主动失效
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取社区统计数据")
    public Result<Map<String, Object>> getStatistics() {
        Map<String, Object> statistics = communityStatisticsCache.get("statistics", key -> buildStatistics());
        return Result.success(statistics);
    }

    private Map<String, Object> buildStatistics() {
        Map<String, Object> statistics = new HashMap<>();

        // 帖子三类计数（总数/已通过/待审核）一条 GROUP BY：旧实现为 3 次独立全表 COUNT（含 2 次按 status 过滤扫描）
        QueryWrapper<Post> statusWrapper = new QueryWrapper<>();
        statusWrapper.select("status", "COUNT(*) AS cnt").groupBy("status");
        long totalPosts = 0L;
        long activePosts = 0L;
        long pendingPosts = 0L;
        List<Map<String, Object>> rows = postService.listMaps(statusWrapper);
        if (rows != null) {
            for (Map<String, Object> row : rows) {
                Object status = row.get("status");
                Object cnt = row.get("cnt");
                long count = cnt instanceof Number ? ((Number) cnt).longValue() : 0L;
                totalPosts += count;
                if (status instanceof Number) {
                    int s = ((Number) status).intValue();
                    if (s == 1) {
                        activePosts = count;
                    } else if (s == 2) {
                        pendingPosts = count;
                    }
                }
            }
        }
        statistics.put("totalPosts", totalPosts);
        statistics.put("activePosts", activePosts);
        statistics.put("pendingPosts", pendingPosts);

        // 总评论数
        statistics.put("totalComments", commentService.count());

        // 关注关系数
        statistics.put("totalFollows", userFollowService.count());

        return statistics;
    }

    /** 写操作后失效统计缓存，保证管理员看到的待审数实时准确 */
    private void invalidateStatsCache() {
        communityStatisticsCache.invalidateAll();
    }

    /**
     * 获取所有帖子（管理员）
     */
    @GetMapping("/posts")
    @Operation(summary = "获取所有帖子（管理员）")
    public Result<Map<String, Object>> getAdminPosts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String category) {
        return com.pethome.common.SafeCall.of(() -> {
            Page<Post> postPage = new Page<>(page, size);
            IPage<Post> result = postService.getAdminPostList(postPage, keyword, status, category);

            Map<String, Object> response = new HashMap<>();
            response.put("posts", result.getRecords());
            response.put("total", result.getTotal());
            response.put("pages", result.getPages());
            response.put("current", result.getCurrent());
            response.put("size", result.getSize());

            return Result.success(response);
        }, "获取帖子列表失败，请稍后重试", log);
    }

    /**
     * 更新帖子状态
     */
    @PutMapping("/posts/{id}/status")
    @Operation(summary = "更新帖子状态")
    public Result<Void> updatePostStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        return com.pethome.common.SafeCall.of(() -> {
            Post post = postService.getById(id);
            if (post == null) {
                throw new BusinessException("帖子不存在");
            }
            post.setStatus(status);
            post.setUpdateTime(LocalDateTime.now());
            postService.updateById(post);
            invalidateStatsCache();

            // 审核/下架等会改变「今日发帖」可见数量，同步每日任务「发布内容」进度（修复：仅待审时进度为 0、通过后仍不更新）
            // 同步失败的降级处理在 TaskService 内部完成，不影响审核主流程
            if (post.getUserId() != null && taskService != null) {
                taskService.syncPostContentProgress(post.getUserId());
            }
            return Result.success(null);
        }, "更新帖子状态失败，请稍后重试", log);
    }

    /**
     * 设置帖子置顶
     */
    @PutMapping("/posts/{id}/top")
    @Operation(summary = "设置帖子置顶")
    public Result<Void> updatePostTop(
            @PathVariable Long id,
            @RequestParam Boolean isTop) {
        return com.pethome.common.SafeCall.of(() -> {
            Post post = postService.getById(id);
            if (post == null) {
                throw new BusinessException("帖子不存在");
            }
            post.setIsTop(isTop);
            post.setUpdateTime(LocalDateTime.now());
            postService.updateById(post);
            return Result.success(null);
        }, "设置置顶失败，请稍后重试", log);
    }

    /**
     * 设置帖子热门
     */
    @PutMapping("/posts/{id}/hot")
    @Operation(summary = "设置帖子热门")
    public Result<Void> updatePostHot(
            @PathVariable Long id,
            @RequestParam Boolean isHot) {
        return com.pethome.common.SafeCall.of(() -> {
            Post post = postService.getById(id);
            if (post == null) {
                throw new BusinessException("帖子不存在");
            }
            post.setIsHot(isHot);
            post.setUpdateTime(LocalDateTime.now());
            postService.updateById(post);
            return Result.success(null);
        }, "设置热门失败，请稍后重试", log);
    }

    /**
     * 删除帖子（管理员）
     */
    @DeleteMapping("/posts/{id}")
    @Operation(summary = "删除帖子（管理员，物理删除并级联清理关联数据）")
    public Result<Void> deletePost(@PathVariable Long id) {
        return com.pethome.common.SafeCall.of(() -> {
            boolean removed = postService.deletePostPhysically(id);
            if (!removed) {
                throw new BusinessException("帖子不存在或已删除");
            }
            invalidateStatsCache();
            return Result.success(null);
        }, "删除帖子失败，请稍后重试", log);
    }

    /**
     * 获取所有评论（管理员）
     */
    @GetMapping("/comments")
    @Operation(summary = "获取所有评论（管理员）")
    public Result<Map<String, Object>> getAdminComments(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long postId) {
        return com.pethome.common.SafeCall.of(() -> {
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
        }, "获取评论列表失败，请稍后重试", log);
    }

    /**
     * 更新评论状态
     */
    @PutMapping("/comments/{id}/status")
    @Operation(summary = "更新评论状态")
    public Result<Void> updateCommentStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        return com.pethome.common.SafeCall.of(() -> {
            Comment comment = commentService.getById(id);
            if (comment == null) {
                throw new BusinessException("评论不存在");
            }
            comment.setStatus(status);
            comment.setUpdateTime(LocalDateTime.now());
            commentService.updateById(comment);
            return Result.success(null);
        }, "更新评论状态失败，请稍后重试", log);
    }

    /**
     * 删除评论（管理员）
     */
    @DeleteMapping("/comments/{id}")
    @Operation(summary = "删除评论（管理员）")
    public Result<Void> deleteComment(@PathVariable Long id) {
        return com.pethome.common.SafeCall.of(() -> {
            Comment comment = commentService.getById(id);
            if (comment == null) {
                throw new BusinessException("评论不存在");
            }
            // 软删除：设置状态为0
            comment.setStatus(0);
            comment.setUpdateTime(LocalDateTime.now());
            commentService.updateById(comment);
            return Result.success(null);
        }, "删除评论失败，请稍后重试", log);
    }

    /**
     * 获取用户的所有帖子
     */
    @GetMapping("/users/{id}/posts")
    @Operation(summary = "获取用户的所有帖子")
    public Result<List<Post>> getUserPosts(@PathVariable Long id) {
        return com.pethome.common.SafeCall.of(() -> {
            List<Post> posts = postService.getUserPosts(id);
            return Result.success(posts);
        }, "获取用户帖子失败，请稍后重试", log);
    }

    /**
     * 获取用户的所有评论
     */
    @GetMapping("/users/{id}/comments")
    @Operation(summary = "获取用户的所有评论")
    public Result<List<Comment>> getUserComments(@PathVariable Long id) {
        return com.pethome.common.SafeCall.of(() -> {
            List<Comment> comments = commentService.getUserComments(id);
            fillCommentUserInfo(comments);
            return Result.success(comments);
        }, "获取用户评论失败，请稍后重试", log);
    }

    /**
     * 批量查询用户并按 id 建立映射（替代逐条 selectById 的 N+1 查询）
     */
    private Map<Long, User> loadUserMap(List<? extends Object> items, java.util.function.Function<Object, Long> userIdGetter) {
        Set<Long> userIds = new HashSet<>();
        for (Object item : items) {
            Long userId = userIdGetter.apply(item);
            if (userId != null) {
                userIds.add(userId);
            }
        }
        Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            for (User user : userService.getUsersByIds(userIds)) {
                userMap.put(user.getId(), user);
            }
        }
        return userMap;
    }

    /**
     * 填充评论的用户信息（批量查询，避免 N+1）
     */
    private void fillCommentUserInfo(List<Comment> comments) {
        if (comments == null || comments.isEmpty()) {
            return;
        }
        Map<Long, User> userMap = loadUserMap(comments, item -> ((Comment) item).getUserId());
        for (Comment comment : comments) {
            User user = comment.getUserId() != null ? userMap.get(comment.getUserId()) : null;
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

    /**
     * 获取每日专题帖子列表
     */
    @GetMapping("/daily-topics/posts")
    @Operation(summary = "获取每日专题帖子列表")
    public Result<Map<String, Object>> getDailyTopicPosts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        return com.pethome.common.SafeCall.of(() -> {
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

            // 批量填充用户信息（避免 N+1 查询）
            if (result.getRecords() != null && !result.getRecords().isEmpty()) {
                Map<Long, User> userMap = loadUserMap(result.getRecords(), item -> ((Post) item).getUserId());
                for (Post post : result.getRecords()) {
                    User user = post.getUserId() != null ? userMap.get(post.getUserId()) : null;
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

            Map<String, Object> response = new HashMap<>();
            response.put("posts", result.getRecords());
            response.put("total", result.getTotal());
            response.put("pages", result.getPages());
            response.put("current", result.getCurrent());
            response.put("size", result.getSize());

            return Result.success(response);
        }, "获取每日专题帖子失败，请稍后重试", log);
    }

    /**
     * 获取每日专题Banner列表
     */
    @GetMapping("/daily-topics/banners")
    @Operation(summary = "获取每日专题Banner列表")
    public Result<List<Banner>> getDailyTopicBanners() {
        return com.pethome.common.SafeCall.of(() -> {
            List<Banner> allBanners = bannerService.getAllBannersForManagement();
            // 可以根据需要筛选，这里返回所有Banner
            return Result.success(allBanners != null ? allBanners : new ArrayList<>());
        }, "获取每日专题Banner失败，请稍后重试", log);
    }

    /**
     * 创建或更新每日专题Banner
     */
    @PostMapping("/daily-topics/banners")
    @Operation(summary = "创建或更新每日专题Banner")
    public Result<Banner> saveDailyTopicBanner(@RequestBody Banner banner) {
        return com.pethome.common.SafeCall.of(() -> {
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
        }, "保存每日专题Banner失败，请稍后重试", log);
    }

    /**
     * 删除每日专题Banner
     */
    @DeleteMapping("/daily-topics/banners/{id}")
    @Operation(summary = "删除每日专题Banner")
    public Result<Void> deleteDailyTopicBanner(@PathVariable Long id) {
        return com.pethome.common.SafeCall.of(() -> {
            boolean success = bannerService.deleteBanner(id);
            if (success) {
                return Result.success(null);
            } else {
                throw new BusinessException("删除失败");
            }
        }, "删除每日专题Banner失败，请稍后重试", log);
    }
}
