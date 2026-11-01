package com.pethome.controller.community;


import com.pethome.common.BusinessException;
import com.pethome.common.SafeCall;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.common.Result;
import com.pethome.entity.Banner;
import com.pethome.entity.Comment;
import com.pethome.entity.DailyTopic;
import com.pethome.entity.Post;
import com.pethome.service.common.BannerService;
import com.pethome.service.community.CommentService;
import com.pethome.entity.DailyTopicTheme;
import com.pethome.service.community.DailyTopicService;
import com.pethome.service.community.DailyTopicThemeService;
import com.pethome.service.community.TopicCollectService;
import com.pethome.service.community.TopicLikeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 每日专题用户端控制器
 */
@RestController
@RequestMapping("/api/daily-topics")
@Tag(name = "每日专题")
public class DailyTopicController {

    private static final Logger log = LoggerFactory.getLogger(DailyTopicController.class);

    /** 从登录态解析当前用户ID（RoleInterceptor 注入）。写操作一律以登录态为准，禁止信任前端传入的 userId */
    private Long currentUserId(HttpServletRequest request) {
        Object attr = request.getAttribute("userId");
        return attr instanceof Number ? ((Number) attr).longValue() : null;
    }

    @Autowired
    private DailyTopicService dailyTopicService;
    
    @Autowired
    private BannerService bannerService;
    
    @Autowired
    private CommentService commentService;

    @Autowired
    private TopicLikeService topicLikeService;

    @Autowired
    private TopicCollectService topicCollectService;

    @Autowired
    private DailyTopicThemeService dailyTopicThemeService;

    /**
     * 获取专题主题分类列表（小程序宠物专题 Tab 与后台下拉共用，无需登录）
     */
    @GetMapping("/themes")
    @Operation(summary = "获取专题主题分类列表")
    public Result<List<DailyTopicTheme>> getThemes() {
        return com.pethome.common.SafeCall.of(() -> {
            List<DailyTopicTheme> list = dailyTopicThemeService.listEnabled();
            return Result.success(list);
        }, "获取主题分类失败，请稍后重试", log);
    }

    /**
     * 获取今日专题
     */
    @GetMapping("/today")
    @Operation(summary = "获取今日专题")
    public Result<DailyTopic> getTodayTopic() {
        return com.pethome.common.SafeCall.of(() -> {
            DailyTopic topic = dailyTopicService.getTodayTopic();
            if (topic != null) {
                // 增加浏览量
                dailyTopicService.incrementViewCount(topic.getId());
            }
            return Result.success(topic);
        }, "获取今日专题失败，请稍后重试", log);
    }

    /**
     * 根据日期获取专题
     */
    @GetMapping("/date/{date}")
    @Operation(summary = "根据日期获取专题")
    public Result<DailyTopic> getTopicByDate(@PathVariable String date) {
        return com.pethome.common.SafeCall.of(() -> {
            LocalDate localDate = LocalDate.parse(date);
            DailyTopic topic = dailyTopicService.getTopicByDate(localDate);
            if (topic != null) {
                // 增加浏览量
                dailyTopicService.incrementViewCount(topic.getId());
            }
            return Result.success(topic);
        }, "获取专题失败，请稍后重试", log);
    }

    /**
     * 获取用户收藏的专题列表（用于「我的收藏-文章」，须在 /{id} 之前定义）
     */
    @GetMapping("/collected")
    @Operation(summary = "获取用户收藏的专题列表")
    public Result<Map<String, Object>> getCollectedTopics(
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            HttpServletRequest request) {
        return com.pethome.common.SafeCall.of(() -> {
            // 安全修复：收藏列表属个人数据，一律以登录态为准；query userId 仅兼容旧前端且不参与查询
            Long currentUserId = currentUserId(request);
            if (currentUserId == null) {
                throw new BusinessException(401, "用户未登录或token无效");
            }
            Page<DailyTopic> topicPage = new Page<>(page, size);
            Page<DailyTopic> result = topicCollectService.getCollectedTopics(currentUserId, topicPage);
            Map<String, Object> response = new HashMap<>();
            response.put("topics", result.getRecords());
            response.put("total", result.getTotal());
            response.put("pages", result.getPages());
            response.put("current", result.getCurrent());
            response.put("size", result.getSize());
            return Result.success(response);
        }, "获取收藏列表失败，请稍后重试", log);
    }

    /**
     * 获取专题详情（可选 userId 用于返回当前用户点赞/收藏状态）
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取专题详情")
    public Result<DailyTopic> getTopicDetail(@PathVariable Long id, @RequestParam(required = false) Long userId,
                                             HttpServletRequest request) {
        return com.pethome.common.SafeCall.of(() -> {
            // 安全修复：点赞/收藏状态属个人数据，以登录态为准（query userId 仅兼容旧前端，不参与查询）
            Long effectiveUserId = currentUserId(request);
            DailyTopic topic = dailyTopicService.getById(id);
            if (topic == null || topic.getStatus() != 1) {
                return Result.success(null);
            }
            List<Post> posts = dailyTopicService.getTopicPosts(id);
            topic.setPosts(posts);
            dailyTopicService.incrementViewCount(id);
            topic.setCollectCount(topicCollectService.getCollectCount(id));
            if (effectiveUserId != null) {
                topic.setIsLiked(topicLikeService.isLiked(id, effectiveUserId));
                topic.setIsCollected(topicCollectService.isCollected(id, effectiveUserId));
            }
            return Result.success(topic);
        }, "获取专题详情失败，请稍后重试", log);
    }

    /**
     * 点赞专题
     */
    @PostMapping("/{id}/like")
    @Operation(summary = "点赞专题")
    public Result<Boolean> likeTopic(@PathVariable Long id, HttpServletRequest request) {
        return com.pethome.common.SafeCall.of(() -> {
            // 安全修复：点赞人以登录态为准，防止替他人点赞
            Long userId = currentUserId(request);
            if (userId == null) {
                throw new BusinessException(401, "用户未登录或token无效");
            }
            boolean success = topicLikeService.likeTopic(id, userId);
            if (!success) {
                throw new BusinessException("已经点赞过了");
            }
            return Result.success(true);
        }, "点赞失败，请稍后重试", log);
    }

    /**
     * 取消点赞专题
     */
    @DeleteMapping("/{id}/like")
    @Operation(summary = "取消点赞专题")
    public Result<Boolean> unlikeTopic(@PathVariable Long id, HttpServletRequest request) {
        return com.pethome.common.SafeCall.of(() -> {
            // 安全修复：操作人以登录态为准
            Long userId = currentUserId(request);
            if (userId == null) {
                throw new BusinessException(401, "用户未登录或token无效");
            }
            topicLikeService.unlikeTopic(id, userId);
            return Result.success(true);
        }, "取消点赞失败，请稍后重试", log);
    }

    /**
     * 收藏专题（收藏到「我的收藏-文章」）
     */
    @PostMapping("/{id}/collect")
    @Operation(summary = "收藏专题")
    public Result<Boolean> collectTopic(@PathVariable Long id, HttpServletRequest request) {
        return com.pethome.common.SafeCall.of(() -> {
            // 安全修复：操作人以登录态为准
            Long userId = currentUserId(request);
            if (userId == null) {
                throw new BusinessException(401, "用户未登录或token无效");
            }
            boolean success = topicCollectService.collectTopic(id, userId);
            if (!success) {
                throw new BusinessException("已经收藏过了");
            }
            return Result.success(true);
        }, "收藏失败，请稍后重试", log);
    }

    /**
     * 取消收藏专题
     */
    @DeleteMapping("/{id}/collect")
    @Operation(summary = "取消收藏专题")
    public Result<Boolean> uncollectTopic(@PathVariable Long id, HttpServletRequest request) {
        return com.pethome.common.SafeCall.of(() -> {
            // 安全修复：操作人以登录态为准
            Long userId = currentUserId(request);
            if (userId == null) {
                throw new BusinessException(401, "用户未登录或token无效");
            }
            topicCollectService.uncollectTopic(id, userId);
            return Result.success(true);
        }, "取消收藏失败，请稍后重试", log);
    }

    /**
     * 检查专题收藏状态
     */
    @GetMapping("/{id}/collect/status")
    @Operation(summary = "检查专题收藏状态")
    public Result<Boolean> getTopicCollectStatus(@PathVariable Long id, @RequestParam(required = false) Long userId,
                                                 HttpServletRequest request) {
        return com.pethome.common.SafeCall.of(() -> {
            // 安全修复：收藏状态属个人数据，以登录态为准
            Long currentUserId = currentUserId(request);
            if (currentUserId == null) {
                throw new BusinessException(401, "用户未登录或token无效");
            }
            boolean isCollected = topicCollectService.isCollected(id, currentUserId);
            return Result.success(isCollected);
        }, "检查收藏状态失败，请稍后重试", log);
    }


    /**
     * 获取专题列表（兼容旧接口）
     * 小程序每日专题精选列表依赖 data.topics，优先保障 topics 始终返回且不因其他逻辑异常而 500。
     * 降级逻辑（列表/今日专题/Banner 单项失败不影响 topics 返回）已下沉到 DailyTopicService。
     */
    @GetMapping
    @Operation(summary = "获取每日专题（兼容接口）")
    public Result<Map<String, Object>> getDailyTopics() {
        return com.pethome.common.SafeCall.of(
            () -> Result.success(dailyTopicService.getDailyTopicsOverview()),
            "获取每日专题失败，请稍后重试", log);
    }

    /**
     * 获取专题评论列表
     */
    @GetMapping("/{id}/comments")
    @Operation(summary = "获取专题评论列表")
    public Result<java.util.List<Comment>> getTopicComments(
            @PathVariable Long id,
            @RequestParam(required = false) Long userId,
            HttpServletRequest request) {
        return com.pethome.common.SafeCall.of(() -> {
            // 安全修复：点赞状态属个人数据，以登录态为准（query userId 仅兼容旧前端）
            Long effectiveUserId = currentUserId(request);
            java.util.List<Comment> comments = commentService.getCommentsByTopicId(id, effectiveUserId);
            return Result.success(comments != null ? comments : new java.util.ArrayList<>());
        }, "获取评论失败，请稍后重试", log);
    }

    /**
     * 添加专题评论
     */
    @PostMapping("/{id}/comments")
    @Operation(summary = "添加专题评论")
    public Result<Comment> addTopicComment(@PathVariable Long id, @RequestBody java.util.Map<String, Object> data,
                                          HttpServletRequest request) {
        return com.pethome.common.SafeCall.of(() -> {
            // 安全修复：评论人以登录态为准，禁止按前端传入 userId 冒充他人发言
            Long userId = currentUserId(request);
            if (userId == null) {
                throw new BusinessException(401, "用户未登录或token无效");
            }
            Comment comment = new Comment();
            comment.setTopicId(id);
            comment.setUserId(userId);
            comment.setContent((String) data.get("content"));
            Object parentIdObj = data.get("parentId");
            if (parentIdObj != null) {
                comment.setParentId(((Number) parentIdObj).longValue());
            } else {
                comment.setParentId(0L);
            }
            Object loc = data.get("location");
            if (loc != null) comment.setLocation(loc.toString());
            Comment result = commentService.addComment(comment);
            return Result.success(result);
        }, "添加评论失败，请稍后重试", log);
    }
}


