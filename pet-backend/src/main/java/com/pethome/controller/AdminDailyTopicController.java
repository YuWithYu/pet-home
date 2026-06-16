package com.pethome.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.common.Result;
import com.pethome.entity.DailyTopic;
import com.pethome.entity.Post;
import com.pethome.service.DailyTopicService;
import com.pethome.service.PostService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员每日专题管理控制器
 */
@RestController
@RequestMapping("/api/admin/daily-topics")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
@Api(tags = "管理员每日专题管理")
public class AdminDailyTopicController {

    @Autowired
    private DailyTopicService dailyTopicService;

    @Autowired
    private PostService postService;

    /**
     * 获取专题列表（管理端）
     */
    @GetMapping
    @ApiOperation("获取专题列表（管理端）")
    public Result<Map<String, Object>> getTopicList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {
        try {
            Page<DailyTopic> topicPage = new Page<>(page, size);
            IPage<DailyTopic> result = dailyTopicService.getTopicList(topicPage, status);

            Map<String, Object> response = new HashMap<>();
            response.put("topics", result.getRecords());
            response.put("total", result.getTotal());
            response.put("pages", result.getPages());
            response.put("current", result.getCurrent());
            response.put("size", result.getSize());

            return Result.success(response);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取专题列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取专题详情
     */
    @GetMapping("/{id}")
    @ApiOperation("获取专题详情")
    public Result<DailyTopic> getTopicDetail(@PathVariable Long id) {
        try {
            DailyTopic topic = dailyTopicService.getById(id);
            if (topic != null) {
                // 加载关联的帖子
                List<Post> posts = dailyTopicService.getTopicPosts(id);
                topic.setPosts(posts);
            }
            return Result.success(topic);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取专题详情失败: " + e.getMessage());
        }
    }

    /**
     * 创建专题
     */
    @PostMapping
    @ApiOperation("创建专题")
    public Result<DailyTopic> createTopic(@RequestBody Map<String, Object> request) {
        try {
            DailyTopic topic = new DailyTopic();
            topic.setTitle((String) request.get("title"));
            topic.setDescription((String) request.get("description"));
            topic.setContent((String) request.get("content"));
            topic.setCoverImage((String) request.get("coverImage"));
            topic.setTheme((String) request.get("theme"));
            
            // 处理发布日期（须为 ISO yyyy-MM-dd，与前端 YYYY-MM-DD 输出一致）
            Object publishDateRaw = request.get("publishDate");
            if (publishDateRaw != null && !publishDateRaw.toString().trim().isEmpty()) {
                topic.setPublishDate(parsePublishDateInput(publishDateRaw));
            } else {
                topic.setPublishDate(LocalDate.now());
            }
            
            topic.setStatus(request.get("status") != null ? 
                Integer.parseInt(request.get("status").toString()) : 0);
            topic.setSortOrder(request.get("sortOrder") != null ? 
                Integer.parseInt(request.get("sortOrder").toString()) : 0);

            // 处理关联的帖子ID列表
            @SuppressWarnings("unchecked")
            List<Long> postIds = (List<Long>) request.get("postIds");

            // 允许多个专题使用同一发布日期（仅作展示/排序用，不做「一日一篇」限制）
            DailyTopic created = dailyTopicService.createWithPosts(topic, postIds);
            return Result.success(created);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("创建专题失败: " + e.getMessage());
        }
    }

    /**
     * 更新专题
     */
    @PutMapping("/{id}")
    @ApiOperation("更新专题")
    public Result<DailyTopic> updateTopic(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        try {
            DailyTopic topic = dailyTopicService.getById(id);
            if (topic == null) {
                return Result.error("专题不存在");
            }

            if (request.get("title") != null) {
                topic.setTitle((String) request.get("title"));
            }
            if (request.get("description") != null) {
                topic.setDescription((String) request.get("description"));
            }
            if (request.containsKey("content")) {
                topic.setContent((String) request.get("content"));
            }
            if (request.get("coverImage") != null) {
                topic.setCoverImage((String) request.get("coverImage"));
            }
            if (request.get("theme") != null) {
                topic.setTheme((String) request.get("theme"));
            }
            if (request.containsKey("publishDate") && request.get("publishDate") != null) {
                String raw = request.get("publishDate").toString().trim();
                if (!raw.isEmpty()) {
                    topic.setPublishDate(parsePublishDateInput(request.get("publishDate")));
                }
            }
            if (request.get("status") != null) {
                topic.setStatus(Integer.parseInt(request.get("status").toString()));
            }
            if (request.get("sortOrder") != null) {
                topic.setSortOrder(Integer.parseInt(request.get("sortOrder").toString()));
            }

            // 处理关联的帖子ID列表
            @SuppressWarnings("unchecked")
            List<Long> postIds = (List<Long>) request.get("postIds");

            DailyTopic updated = dailyTopicService.updateWithPosts(topic, postIds);
            return Result.success(updated);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("更新专题失败: " + e.getMessage());
        }
    }

    /**
     * 删除专题
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除专题")
    public Result<Void> deleteTopic(@PathVariable Long id) {
        try {
            boolean success = dailyTopicService.removeById(id);
            if (success) {
                return Result.success(null);
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("删除专题失败: " + e.getMessage());
        }
    }

    /**
     * 发布专题
     */
    @PostMapping("/{id}/publish")
    @ApiOperation("发布专题")
    public Result<Void> publishTopic(@PathVariable Long id) {
        try {
            boolean success = dailyTopicService.publishTopic(id);
            if (success) {
                return Result.success(null);
            } else {
                return Result.error("发布失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("发布专题失败: " + e.getMessage());
        }
    }

    /**
     * 下线专题
     */
    @PostMapping("/{id}/offline")
    @ApiOperation("下线专题")
    public Result<Void> offlineTopic(@PathVariable Long id) {
        try {
            boolean success = dailyTopicService.offlineTopic(id);
            if (success) {
                return Result.success(null);
            } else {
                return Result.error("下线失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("下线专题失败: " + e.getMessage());
        }
    }

    /**
     * 获取专题关联的帖子
     */
    @GetMapping("/{id}/posts")
    @ApiOperation("获取专题关联的帖子")
    public Result<List<Post>> getTopicPosts(@PathVariable Long id) {
        try {
            List<Post> posts = dailyTopicService.getTopicPosts(id);
            return Result.success(posts);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取专题帖子失败: " + e.getMessage());
        }
    }

    /**
     * 关联帖子到专题
     */
    @PostMapping("/{id}/posts")
    @ApiOperation("关联帖子到专题")
    public Result<Void> associatePosts(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        try {
            DailyTopic topic = dailyTopicService.getById(id);
            if (topic == null) {
                return Result.error("专题不存在");
            }

            List<Long> postIds = extractPostIds(request);

            dailyTopicService.updateWithPosts(topic, postIds);
            return Result.success(null);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("关联帖子失败: " + e.getMessage());
        }
    }

    /**
     * 安全地从请求体中解析帖子ID列表，避免 Integer/Long 类型转换异常
     */
    private List<Long> extractPostIds(Map<String, Object> request) {
        Object rawValue = request.get("postIds");
        if (rawValue == null) {
            return null;
        }

        if (rawValue instanceof List) {
            List<?> rawList = (List<?>) rawValue;
            List<Long> result = new java.util.ArrayList<>(rawList.size());
            for (Object item : rawList) {
                if (item == null) {
                    continue;
                }
                if (item instanceof Number) {
                    result.add(((Number) item).longValue());
                } else {
                    try {
                        result.add(Long.parseLong(item.toString()));
                    } catch (NumberFormatException ignore) {
                        // 跳过无法解析的值
                    }
                }
            }
            return result;
        }

        // 单个值的情况
        if (rawValue instanceof Number) {
            return java.util.Collections.singletonList(((Number) rawValue).longValue());
        }
        try {
            return java.util.Collections.singletonList(Long.parseLong(rawValue.toString()));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 搜索帖子（用于关联）
     */
    @GetMapping("/posts/search")
    @ApiOperation("搜索帖子（用于关联）")
    public Result<Map<String, Object>> searchPosts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        try {
            Page<Post> postPage = new Page<>(page, size);
            QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("status", 1); // 只查询正常状态的帖子
            
            if (keyword != null && !keyword.isEmpty()) {
                queryWrapper.and(wrapper -> wrapper
                    .like("title", keyword)
                    .or()
                    .like("content", keyword)
                );
            }
            if (category != null && !category.isEmpty()) {
                queryWrapper.eq("category", category);
            }
            
            queryWrapper.orderByDesc("create_time");
            
            IPage<Post> result = postService.page(postPage, queryWrapper);
            
            // 填充用户信息
            if (result.getRecords() != null && !result.getRecords().isEmpty()) {
                ((com.pethome.service.impl.PostServiceImpl) postService).fillUserInfoAndLikeStatus(result.getRecords(), null);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("posts", result.getRecords());
            response.put("total", result.getTotal());

            return Result.success(response);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("搜索帖子失败: " + e.getMessage());
        }
    }

    /**
     * 解析管理端传入的发布日期（ISO yyyy-MM-dd）
     */
    private LocalDate parsePublishDateInput(Object raw) {
        if (raw == null) {
            throw new IllegalArgumentException("发布日期不能为空");
        }
        String s = raw.toString().trim();
        if (s.isEmpty()) {
            throw new IllegalArgumentException("发布日期不能为空");
        }
        try {
            return LocalDate.parse(s, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("发布日期格式错误，请使用 yyyy-MM-dd");
        }
    }
}

