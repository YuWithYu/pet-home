package com.pethome.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.common.Result;
import com.pethome.entity.Banner;
import com.pethome.entity.Comment;
import com.pethome.entity.DailyTopic;
import com.pethome.entity.Post;
import com.pethome.service.BannerService;
import com.pethome.service.CommentService;
import com.pethome.entity.DailyTopicTheme;
import com.pethome.service.DailyTopicService;
import com.pethome.service.DailyTopicThemeService;
import com.pethome.service.TopicCollectService;
import com.pethome.service.TopicLikeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
@Api(tags = "每日专题")
public class DailyTopicController {

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
    @ApiOperation("获取专题主题分类列表")
    public Result<List<DailyTopicTheme>> getThemes() {
        try {
            List<DailyTopicTheme> list = dailyTopicThemeService.listEnabled();
            return Result.success(list);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取主题分类失败: " + e.getMessage());
        }
    }

    /**
     * 获取今日专题
     */
    @GetMapping("/today")
    @ApiOperation("获取今日专题")
    public Result<DailyTopic> getTodayTopic() {
        try {
            DailyTopic topic = dailyTopicService.getTodayTopic();
            if (topic != null) {
                // 增加浏览量
                dailyTopicService.incrementViewCount(topic.getId());
            }
            return Result.success(topic);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取今日专题失败: " + e.getMessage());
        }
    }

    /**
     * 根据日期获取专题
     */
    @GetMapping("/date/{date}")
    @ApiOperation("根据日期获取专题")
    public Result<DailyTopic> getTopicByDate(@PathVariable String date) {
        try {
            LocalDate localDate = LocalDate.parse(date);
            DailyTopic topic = dailyTopicService.getTopicByDate(localDate);
            if (topic != null) {
                // 增加浏览量
                dailyTopicService.incrementViewCount(topic.getId());
            }
            return Result.success(topic);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取专题失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户收藏的专题列表（用于「我的收藏-文章」，须在 /{id} 之前定义）
     */
    @GetMapping("/collected")
    @ApiOperation("获取用户收藏的专题列表")
    public Result<Map<String, Object>> getCollectedTopics(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        try {
            Page<DailyTopic> topicPage = new Page<>(page, size);
            Page<DailyTopic> result = topicCollectService.getCollectedTopics(userId, topicPage);
            Map<String, Object> response = new HashMap<>();
            response.put("topics", result.getRecords());
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

    /**
     * 获取专题详情（可选 userId 用于返回当前用户点赞/收藏状态）
     */
    @GetMapping("/{id}")
    @ApiOperation("获取专题详情")
    public Result<DailyTopic> getTopicDetail(@PathVariable Long id, @RequestParam(required = false) Long userId) {
        try {
            DailyTopic topic = dailyTopicService.getById(id);
            if (topic == null || topic.getStatus() != 1) {
                return Result.success(null);
            }
            List<Post> posts = dailyTopicService.getTopicPosts(id);
            topic.setPosts(posts);
            dailyTopicService.incrementViewCount(id);
            topic.setCollectCount(topicCollectService.getCollectCount(id));
            if (userId != null) {
                topic.setIsLiked(topicLikeService.isLiked(id, userId));
                topic.setIsCollected(topicCollectService.isCollected(id, userId));
            }
            return Result.success(topic);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取专题详情失败: " + e.getMessage());
        }
    }

    /**
     * 点赞专题
     */
    @PostMapping("/{id}/like")
    @ApiOperation("点赞专题")
    public Result<Boolean> likeTopic(@PathVariable Long id, @RequestParam Long userId) {
        try {
            boolean success = topicLikeService.likeTopic(id, userId);
            if (!success) {
                return Result.error("已经点赞过了");
            }
            return Result.success(true);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("点赞失败: " + e.getMessage());
        }
    }

    /**
     * 取消点赞专题
     */
    @DeleteMapping("/{id}/like")
    @ApiOperation("取消点赞专题")
    public Result<Boolean> unlikeTopic(@PathVariable Long id, @RequestParam Long userId) {
        try {
            topicLikeService.unlikeTopic(id, userId);
            return Result.success(true);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("取消点赞失败: " + e.getMessage());
        }
    }

    /**
     * 收藏专题（收藏到「我的收藏-文章」）
     */
    @PostMapping("/{id}/collect")
    @ApiOperation("收藏专题")
    public Result<Boolean> collectTopic(@PathVariable Long id, @RequestParam Long userId) {
        try {
            boolean success = topicCollectService.collectTopic(id, userId);
            if (!success) {
                return Result.error("已经收藏过了");
            }
            return Result.success(true);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("收藏失败: " + e.getMessage());
        }
    }

    /**
     * 取消收藏专题
     */
    @DeleteMapping("/{id}/collect")
    @ApiOperation("取消收藏专题")
    public Result<Boolean> uncollectTopic(@PathVariable Long id, @RequestParam Long userId) {
        try {
            topicCollectService.uncollectTopic(id, userId);
            return Result.success(true);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("取消收藏失败: " + e.getMessage());
        }
    }

    /**
     * 检查专题收藏状态
     */
    @GetMapping("/{id}/collect/status")
    @ApiOperation("检查专题收藏状态")
    public Result<Boolean> getTopicCollectStatus(@PathVariable Long id, @RequestParam Long userId) {
        try {
            boolean isCollected = topicCollectService.isCollected(id, userId);
            return Result.success(isCollected);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("检查收藏状态失败: " + e.getMessage());
        }
    }


    /**
     * 获取专题列表（兼容旧接口）
     * 小程序每日专题精选列表依赖 data.topics，优先保障 topics 始终返回且不因其他逻辑异常而 500。
     */
    @GetMapping
    @ApiOperation("获取每日专题（兼容接口）")
    public Result<Map<String, Object>> getDailyTopics() {
        Map<String, Object> response = new HashMap<>();
        
        // 今日主题、日期（与业务库无关，始终可返回）
        String[] themes = {
            "猫咪护理", "狗狗训练", "宠物健康", "宠物美容",
            "宠物饮食", "宠物玩具", "宠物训练"
        };
        int dayOfWeek = (java.time.LocalDate.now().getDayOfWeek().getValue() - 1) % 7;
        String todayTheme = dayOfWeek >= 0 && dayOfWeek < themes.length ? themes[dayOfWeek] : "宠物健康";
        response.put("todayTheme", todayTheme);
        response.put("date", LocalDate.now().toString());
        response.put("topic", null);
        response.put("posts", new ArrayList<Post>());
        response.put("banners", new ArrayList<Banner>());

        // 已发布专题列表（管理员后台创建，供精选专题展示）——优先拉取，确保小程序有列表
        List<DailyTopic> topics = new ArrayList<>();
        try {
            Page<DailyTopic> topicPage = new Page<>(1, 20);
            IPage<DailyTopic> topicResult = dailyTopicService.getTopicList(topicPage, 1);
            if (topicResult != null && topicResult.getRecords() != null) {
                topics = topicResult.getRecords();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        response.put("topics", topics);

        // 今日专题、Banner 等（失败不影响 topics 返回）
        try {
            DailyTopic todayTopic = dailyTopicService.getTodayTopic();
            List<Banner> allBanners = bannerService.getAllBanners();
            if (allBanners == null) allBanners = new ArrayList<Banner>();
            List<Banner> activeBanners = allBanners.size() > 5
                ? allBanners.subList(0, 5)
                : allBanners;
            response.put("banners", activeBanners);
            if (todayTopic != null) {
                List<Post> posts = dailyTopicService.getTopicPosts(todayTopic.getId());
                todayTopic.setPosts(posts != null ? posts : new ArrayList<Post>());
                response.put("topic", todayTopic);
                response.put("posts", todayTopic.getPosts());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Result.success(response);
    }

    /**
     * 获取专题评论列表
     */
    @GetMapping("/{id}/comments")
    @ApiOperation("获取专题评论列表")
    public Result<java.util.List<Comment>> getTopicComments(
            @PathVariable Long id,
            @RequestParam(required = false) Long userId) {
        try {
            java.util.List<Comment> comments = commentService.getCommentsByTopicId(id, userId);
            return Result.success(comments != null ? comments : new java.util.ArrayList<>());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取评论失败: " + e.getMessage());
        }
    }

    /**
     * 添加专题评论
     */
    @PostMapping("/{id}/comments")
    @ApiOperation("添加专题评论")
    public Result<Comment> addTopicComment(@PathVariable Long id, @RequestBody java.util.Map<String, Object> data) {
        try {
            Comment comment = new Comment();
            comment.setTopicId(id);
            comment.setUserId(((Number) data.get("userId")).longValue());
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
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("添加评论失败: " + e.getMessage());
        }
    }
}


