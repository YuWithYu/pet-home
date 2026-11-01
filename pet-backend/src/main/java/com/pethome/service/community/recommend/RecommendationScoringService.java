package com.pethome.service.community.recommend;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pethome.entity.Post;
import com.pethome.entity.PostLike;
import com.pethome.mapper.PostLikeMapper;
import com.pethome.mapper.PostMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 推荐打分算法组件：关注/发现区打分、标签匹配、时间衰减与发现页栏目语义匹配。
 */
@Component
public class RecommendationScoringService {

    // 算法参数
    private static final double LIKE_WEIGHT = 0.4;
    private static final double COMMENT_WEIGHT = 0.3;
    private static final double VIEW_WEIGHT = 0.2;
    private static final double EXPOSURE_WEIGHT = 0.1;

    private static final double EXPLORE_LIKE_WEIGHT = 0.5;
    private static final double EXPLORE_COMMENT_WEIGHT = 0.3;
    private static final double EXPLORE_VIEW_WEIGHT = 0.2;

    private static final double ALPHA = 0.2; // 最近互动权重
    private static final double BETA = 0.3;  // 标签匹配权重
    private static final double LAMBDA = 0.01; // 时间衰减系数
    private static final double HOT_HALF_LIFE_HOURS = 36.0; // 热度半衰期

    @Autowired
    private PostLikeMapper postLikeMapper;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private RecommendationInterestTagService recommendationInterestTagService;

    public Double calculateFollowFeedScore(Post post, Long userId) {
        // 基础热度分数
        double baseHotScore =
            (post.getLikesCount() != null ? post.getLikesCount() : 0) * LIKE_WEIGHT +
            (post.getCommentsCount() != null ? post.getCommentsCount() : 0) * COMMENT_WEIGHT +
            (post.getViewsCount() != null ? post.getViewsCount() : 0) * VIEW_WEIGHT +
            (post.getExposureScore() != null ? post.getExposureScore() : 0.0) * EXPOSURE_WEIGHT;

        // 最近互动权重（简化实现：检查用户是否最近与该作者有互动）
        double recentInteraction = calculateRecentInteraction(userId, post.getUserId());

        // 标签匹配权重
        List<String> userTags = recommendationInterestTagService.getUserInterestTags(userId);
        double tagMatch = calculateTagMatchScore(post.getTags(), userTags);

        // 最终分数 = 基础分数 * (1 + α * 最近互动 + β * 标签匹配)
        double finalScore = baseHotScore * (1.0 + ALPHA * recentInteraction + BETA * tagMatch);

        return finalScore;
    }

    public Double calculateExploreScore(Post post, Long userId) {
        // 基础热度分数
        double baseScore =
            (post.getLikesCount() != null ? post.getLikesCount() : 0) * EXPLORE_LIKE_WEIGHT +
            (post.getCommentsCount() != null ? post.getCommentsCount() : 0) * EXPLORE_COMMENT_WEIGHT +
            (post.getViewsCount() != null ? post.getViewsCount() : 0) * EXPLORE_VIEW_WEIGHT +
            (post.getExposureScore() != null ? post.getExposureScore() : 0.0);

        // 时间衰减：score = raw_score * e^(-λ * hours_since_posted)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime postTime = post.getCreateTime();
        if (postTime != null) {
            long hoursSincePosted = ChronoUnit.HOURS.between(postTime, now);
            double timeDecay = Math.exp(-LAMBDA * hoursSincePosted);
            baseScore *= timeDecay;
        }

        // 标签匹配加成
        List<String> userTags = recommendationInterestTagService.getUserInterestTags(userId);
        double tagMatchBonus = 1.0 + calculateTagMatchScore(post.getTags(), userTags) * 0.5;

        // 最终分数
        double finalScore = baseScore * tagMatchBonus;

        return finalScore;
    }

    public Double calculateTagMatchScore(String postTags, List<String> userTags) {
        if (postTags == null || postTags.isEmpty() || userTags == null || userTags.isEmpty()) {
            return 0.0;
        }

        // 解析帖子标签
        Set<String> postTagSet = new HashSet<>();
        String[] tags = postTags.split(",");
        for (String tag : tags) {
            String trimmedTag = tag.trim();
            if (!trimmedTag.isEmpty()) {
                postTagSet.add(trimmedTag);
            }
        }

        // 如果没有tags，使用category
        if (postTagSet.isEmpty()) {
            // 从category提取
            if (postTags != null && !postTags.isEmpty()) {
                postTagSet.add(postTags.trim());
            }
        }

        // 计算匹配度
        int matchCount = 0;
        for (String userTag : userTags) {
            if (postTagSet.contains(userTag)) {
                matchCount++;
            }
        }

        // 返回匹配分数（匹配的标签数 / 帖子标签总数）
        if (postTagSet.isEmpty()) {
            return 0.0;
        }

        return (double) matchCount / postTagSet.size();
    }

    /**
     * 计算最近互动权重（简化实现）
     * 实际可以查询用户与该作者的互动历史（点赞、评论等）
     */
    public double calculateRecentInteraction(Long userId, Long authorId) {
        // 简化：检查用户是否最近点赞过该作者的帖子
        // 实际可以扩展为：最近7天的互动次数 / 总互动次数

        List<PostLike> recentLikes = postLikeMapper.selectList(
            new QueryWrapper<PostLike>()
                .eq("user_id", userId)
                .ge("create_time", LocalDateTime.now().minusDays(7))
        );

        if (recentLikes == null || recentLikes.isEmpty()) {
            return 0.0;
        }

        List<Long> likedPostIds = recentLikes.stream()
                .map(PostLike::getPostId)
                .collect(Collectors.toList());

        if (likedPostIds.isEmpty()) {
            return 0.0;
        }

        List<Post> likedPosts = postMapper.selectBatchIds(likedPostIds);
        long authorPostCount = likedPosts.stream()
                .filter(p -> p.getUserId().equals(authorId))
                .count();

        // 如果最近点赞过该作者的帖子，返回0.5（最高0.5）
        return authorPostCount > 0 ? 0.5 : 0.0;
    }

    public double calculateBestHotScore(Post post, List<String> userInterestTags, int collectCount) {
        int likes = post.getLikesCount() != null ? post.getLikesCount() : 0;
        int comments = post.getCommentsCount() != null ? post.getCommentsCount() : 0;
        int views = post.getViewsCount() != null ? post.getViewsCount() : 0;
        int shares = post.getSharesCount() != null ? post.getSharesCount() : 0;
        double exposure = post.getExposureScore() != null ? post.getExposureScore() : 0.0;

        // 1) 互动质量分：评论/收藏/转发权重大于点赞，浏览使用sqrt降低刷量影响
        double interactionScore =
                likes * 1.0
                        + comments * 3.0
                        + collectCount * 2.2
                        + shares * 2.8
                        + Math.sqrt(Math.max(0, views)) * 1.2
                        + Math.max(0.0, exposure) * 0.6;

        // 2) 可信度抑制：低互动高浏览时下调
        double engagementRate = interactionScore / (Math.max(views, 1.0) + 20.0);
        double credibilityFactor = Math.min(1.2, 0.7 + engagementRate * 8.0);
        if (credibilityFactor < 0.6) credibilityFactor = 0.6;

        // 3) 时间衰减（半衰期）
        double freshnessFactor = 1.0;
        if (post.getCreateTime() != null) {
            long hours = Math.max(0, ChronoUnit.HOURS.between(post.getCreateTime(), LocalDateTime.now()));
            freshnessFactor = Math.exp(-Math.log(2) * hours / HOT_HALF_LIFE_HOURS);
        }

        // 4) 兴趣匹配加权
        double interestBoost = hasInterestMatch(post, userInterestTags) ? 1.12 : 1.0;

        return interactionScore * credibilityFactor * freshnessFactor * interestBoost;
    }

    public boolean hasInterestMatch(Post post, List<String> userTags) {
        if (userTags == null || userTags.isEmpty()) return false;
        Set<String> postSet = new HashSet<>();
        String postTags = post.getTags();
        if (postTags != null && !postTags.trim().isEmpty()) {
            for (String t : postTags.split(",")) {
                String x = t.trim();
                if (!x.isEmpty()) postSet.add(x);
            }
        }
        if (post.getCategory() != null && !post.getCategory().trim().isEmpty()) {
            postSet.add(post.getCategory().trim());
        }
        if (postSet.isEmpty()) return false;
        for (String u : userTags) {
            if (u != null && postSet.contains(u.trim())) return true;
        }
        return false;
    }

    public boolean containsTag(String postTags, String targetTag) {
        if (postTags == null || postTags.trim().isEmpty() || targetTag == null || targetTag.trim().isEmpty()) {
            return false;
        }
        String target = targetTag.trim();
        String[] arr = postTags.split(",");
        for (String item : arr) {
            if (target.equalsIgnoreCase(item == null ? "" : item.trim())) {
                return true;
            }
        }
        return false;
    }

    /** 发现页子栏目是否为「视频」（与前端标签 key/文案兼容） */
    public boolean isDiscoverVideoTabTag(String tag) {
        if (tag == null) return false;
        String t = tag.trim();
        return "视频".equals(t) || "video".equalsIgnoreCase(t);
    }

    /**
     * 是否为视频帖：有 videos JSON、或 images 字段里含常见视频后缀（发布时 category 常为「推荐」也可进视频流）
     */
    public boolean isPostVideoContent(Post p) {
        if (p == null) return false;
        String v = p.getVideos();
        if (v != null) {
            String s = v.trim();
            if (!s.isEmpty() && !"null".equalsIgnoreCase(s) && !"[]".equals(s)) {
                return true;
            }
        }
        String imgs = p.getImages();
        if (imgs == null || imgs.trim().isEmpty()) return false;
        String lower = imgs.toLowerCase();
        return lower.contains(".mp4") || lower.contains(".mov") || lower.contains(".m4v")
                || lower.contains(".avi") || lower.contains(".webm") || lower.contains(".3gp");
    }

    /** 标题+正文+标签+分类拼成一段，用于轻量语义分栏（非大模型，关键词可后续扩充） */
    public String postTextBag(Post p) {
        if (p == null) return "";
        StringBuilder sb = new StringBuilder();
        if (p.getTitle() != null) sb.append(p.getTitle()).append(' ');
        if (p.getContent() != null) sb.append(p.getContent()).append(' ');
        if (p.getTags() != null) sb.append(p.getTags()).append(' ');
        if (p.getCategory() != null) sb.append(p.getCategory());
        return sb.toString();
    }

    public boolean isDiscoverCatTabTag(String tag) {
        if (tag == null) return false;
        String t = tag.trim();
        return "猫咪".equals(t) || "cat".equalsIgnoreCase(t);
    }

    public boolean isDiscoverDogTabTag(String tag) {
        if (tag == null) return false;
        String t = tag.trim();
        return "狗狗".equals(t) || "狗".equals(t) || "dog".equalsIgnoreCase(t);
    }

    public boolean isDiscoverKnowledgeTabTag(String tag) {
        if (tag == null) return false;
        String t = tag.trim();
        return "养宠知识".equals(t) || "百科".equals(t);
    }

    /** 猫咪栏：含猫/喵等语义即入（排除「熊猫」等误伤） */
    public boolean matchesCatColumnSemantic(Post p) {
        String bag = postTextBag(p);
        if (bag.isEmpty()) return false;
        if (bag.contains("熊猫")) return false;
        return bag.contains("猫咪") || bag.contains("猫猫") || bag.contains("喵星人") || bag.contains("养猫")
                || bag.contains("英短") || bag.contains("布偶") || bag.contains("橘猫")
                || bag.contains("猫") || bag.contains("喵");
    }

    /** 狗狗栏：含犬/狗等；排除「热狗」等 */
    public boolean matchesDogColumnSemantic(Post p) {
        String bag = postTextBag(p);
        if (bag.isEmpty()) return false;
        if (bag.contains("热狗")) return false;
        return bag.contains("狗狗") || bag.contains("狗子") || bag.contains("养狗") || bag.contains("汪星人")
                || bag.contains("柯基") || bag.contains("金毛") || bag.contains("哈士奇")
                || bag.contains("犬") || bag.contains("狗");
    }

    /** 养宠知识栏：科普/教程/医疗向关键词 */
    public boolean matchesKnowledgeColumnSemantic(Post p) {
        String bag = postTextBag(p);
        if (bag.isEmpty()) return false;
        String[] kws = {"科普", "攻略", "教程", "疫苗", "驱虫", "疾病", "领养", "注意", "建议", "干货",
                "养宠", "怎么养", "如何养", "为什么", "指南", "新手", "误区", "医院", "症状", "治疗"};
        for (String k : kws) {
            if (bag.contains(k)) return true;
        }
        return bag.contains("怎么") || bag.contains("如何");
    }
}
