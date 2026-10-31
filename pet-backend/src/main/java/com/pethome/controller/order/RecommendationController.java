package com.pethome.controller.order;


import com.pethome.common.SafeCall;
import com.pethome.common.Result;
import com.pethome.entity.Post;
import com.pethome.service.community.PostRecommendationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 推荐算法控制器。
 * 协同过滤已接 {@link com.pethome.service.community.PostRecommendationService}；深度学习接口当前为占位，
 * 实际返回混合/探索推荐（见接口响应 message 字段），避免与「已上线深度学习」类文案冲突。
 */
@RestController
@RequestMapping("/api/recommendation")
@Tag(name = "推荐算法")
public class RecommendationController {

    private static final Logger log = LoggerFactory.getLogger(RecommendationController.class);
    
    @Autowired
    private PostRecommendationService postRecommendationService;
    
    /**
     * 协同过滤推荐接口（预留）
     * 可以接入协同过滤算法，基于用户行为相似度推荐
     */
    @GetMapping("/collaborative-filtering")
    @Operation(summary = "协同过滤推荐（预留接口）")
    public Result<Map<String, Object>> collaborativeFiltering(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "20") Integer limit) {
        return com.pethome.common.SafeCall.of(() -> {
            List<Post> recommendedPosts = postRecommendationService.getCollaborativeRecommendations(
                    userId,
                    limit == null ? 20 : limit
            );
            
            Map<String, Object> result = new HashMap<>();
            result.put("posts", recommendedPosts);
            result.put("algorithm", "user-based-collaborative-filtering");
            result.put("message", "基于用户的协同过滤推荐");
            
            return Result.success(result);
        }, "协同过滤推荐失败，请稍后重试", log);
    }
    
    /**
     * 深度学习推荐接口（预留）
     * 可以接入深度学习模型（如GNN、BERT等）
     */
    @PostMapping("/deep-learning")
    @Operation(summary = "深度学习推荐（预留接口）")
    public Result<Map<String, Object>> deepLearningRecommendation(
            @RequestBody Map<String, Object> request) {
        return com.pethome.common.SafeCall.of(() -> {
            Long userId = Long.parseLong(request.get("userId").toString());
            Integer limit = request.get("limit") != null ? 
                Integer.parseInt(request.get("limit").toString()) : 20;
            
            // TODO: 调用深度学习模型API
            // 1. 准备用户特征向量
            // 2. 准备帖子特征向量
            // 3. 调用模型推理
            // 4. 返回推荐结果
            
            // 当前使用混合推荐作为fallback
            List<Post> recommendedPosts = postRecommendationService.getExplorePosts(userId, limit);
            
            Map<String, Object> result = new HashMap<>();
            result.put("posts", recommendedPosts);
            result.put("algorithm", "hybrid-fallback");
            result.put("message", "深度学习模型待接入，当前使用混合推荐算法");
            
            return Result.success(result);
        }, "深度学习推荐失败，请稍后重试", log);
    }
    
    /**
     * 获取用户兴趣标签
     */
    @GetMapping("/user-interests")
    @Operation(summary = "获取用户兴趣标签")
    public Result<List<String>> getUserInterests(@RequestParam Long userId) {
        return com.pethome.common.SafeCall.of(() -> {
            List<String> interests = postRecommendationService.getUserInterestTags(userId);
            return Result.success(interests);
        }, "获取用户兴趣失败，请稍后重试", log);
    }

    /**
     * 热度推荐接口
     * 根据点赞、评论、收藏、浏览量加权排序
     * 可以直接用于发现页
     */
    @GetMapping("/hot")
    @Operation(summary = "基于热度的推荐（发现页使用）")
    public Result<List<Post>> hotRecommendation(
            @RequestParam(defaultValue = "20") Integer limit) {
        return com.pethome.common.SafeCall.of(() -> {
            List<Post> hotPosts = postRecommendationService.getHotPosts(limit);
            return Result.success(hotPosts);
        }, "热度推荐失败，请稍后重试", log);
    }
}
