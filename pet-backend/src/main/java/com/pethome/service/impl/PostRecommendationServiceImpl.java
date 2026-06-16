package com.pethome.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pethome.entity.Post;
import com.pethome.entity.Comment;
import com.pethome.entity.PostCollect;
import com.pethome.entity.PostLike;
import com.pethome.entity.RecommendationInteractionWeight;
import com.pethome.entity.User;
import com.pethome.entity.UserPostInteraction;
import com.pethome.mapper.CommentMapper;
import com.pethome.mapper.RecommendationInteractionWeightMapper;
import com.pethome.entity.UserFollow;
import com.pethome.mapper.PostCollectMapper;
import com.pethome.mapper.PostLikeMapper;
import com.pethome.mapper.PostMapper;
import com.pethome.mapper.UserPostInteractionMapper;
import com.pethome.mapper.UserFollowMapper;
import com.pethome.mapper.UserMapper;
import com.pethome.service.PostRecommendationService;
import com.pethome.service.PostService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 帖子推荐服务实现
 * 实现关注区和发现区的个性化推荐算法
 */
@Service
public class PostRecommendationServiceImpl implements PostRecommendationService {

    private static final Logger log = LoggerFactory.getLogger(PostRecommendationServiceImpl.class);
    
    @Autowired
    private PostMapper postMapper;
    
    @Autowired
    private UserFollowMapper userFollowMapper;
    
    @Autowired
    private PostService postService;
    
    @Autowired
    private PostLikeMapper postLikeMapper;

    @Autowired
    private PostCollectMapper postCollectMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserPostInteractionMapper userPostInteractionMapper;

    @Autowired
    private RecommendationInteractionWeightMapper recommendationInteractionWeightMapper;
    
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

    // 协同过滤行为权重默认值（当配置表无数据时兜底）
    private static final double DEFAULT_LIKE_SCORE = 3.0;
    private static final double DEFAULT_COMMENT_SCORE = 2.0;
    private static final double DEFAULT_COLLECT_SCORE = 4.0;
    private static final double DEFAULT_VIEW_SCORE = 1.0;
    // 小样本场景参数：冷启动阈值与探索比例
    private static final int COLD_START_INTERACTION_THRESHOLD = 8;
    private static final double COLD_START_EXPLORE_RATIO = 0.35;
    private static final double NORMAL_EXPLORE_RATIO = 0.15;
    
    @Override
    public List<Post> getFollowFeedPosts(Long userId, Integer limit) {
        // 1. 获取用户关注列表
        List<UserFollow> followings = userFollowMapper.getFollowingList(userId);
        if (followings == null || followings.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Long> followingIds = followings.stream()
                .map(UserFollow::getFollowingId)
                .collect(Collectors.toList());
        
        // 2. 查询关注用户的帖子
        QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1)
                   .in("user_id", followingIds)
                   .orderByDesc("create_time");
        
        List<Post> posts = postMapper.selectList(queryWrapper);
        
        // 3. 计算每个帖子的推荐分数并排序
        List<Post> scoredPosts = posts.stream()
                .map(post -> {
                    double score = calculateFollowFeedScore(post, userId);
                    post.setRecommendationScore(score);
                    return post;
                })
                .sorted((p1, p2) -> Double.compare(p2.getRecommendationScore(), p1.getRecommendationScore()))
                .limit(limit != null ? limit : 20)
                .collect(Collectors.toList());
        
        // 4. 填充用户信息（使用反射调用PostServiceImpl的public方法）
        fillUserInfoAndLikeStatus(scoredPosts, userId);
        
        return scoredPosts;
    }
    
    @Override
    public List<Post> getExplorePosts(Long userId, Integer limit) {
        // 1. 获取用户关注列表（排除已关注的用户）
        List<UserFollow> followings = userFollowMapper.getFollowingList(userId);
        List<Long> followingIds = new ArrayList<>();
        if (followings != null && !followings.isEmpty()) {
            followingIds = followings.stream()
                    .map(UserFollow::getFollowingId)
                    .collect(Collectors.toList());
        }
        
        // 2. 查询非关注用户的帖子（排除置顶帖）
        QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1)
                   .eq("is_top", false);
        
        if (!followingIds.isEmpty()) {
            queryWrapper.notIn("user_id", followingIds);
        }
        
        queryWrapper.orderByDesc("create_time");
        
        List<Post> posts = postMapper.selectList(queryWrapper);
        
        // 3. 计算每个帖子的推荐分数并排序
        List<Post> scoredPosts = posts.stream()
                .map(post -> {
                    double score = calculateExploreScore(post, userId);
                    post.setRecommendationScore(score);
                    return post;
                })
                .sorted((p1, p2) -> Double.compare(p2.getRecommendationScore(), p1.getRecommendationScore()))
                .limit(limit != null ? limit : 20)
                .collect(Collectors.toList());
        
        // 4. 填充用户信息
        fillUserInfoAndLikeStatus(scoredPosts, userId);
        
        return scoredPosts;
    }
    
    /**
     * 填充用户信息和点赞状态（复用PostServiceImpl的逻辑）
     */
    private void fillUserInfoAndLikeStatus(List<Post> posts, Long currentUserId) {
        // 直接调用PostServiceImpl的public方法
        if (postService instanceof com.pethome.service.impl.PostServiceImpl) {
            ((com.pethome.service.impl.PostServiceImpl) postService).fillUserInfoAndLikeStatus(posts, currentUserId);
        }
    }
    
    @Override
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
        List<String> userTags = getUserInterestTags(userId);
        double tagMatch = calculateTagMatchScore(post.getTags(), userTags);
        
        // 最终分数 = 基础分数 * (1 + α * 最近互动 + β * 标签匹配)
        double finalScore = baseHotScore * (1.0 + ALPHA * recentInteraction + BETA * tagMatch);
        
        return finalScore;
    }
    
    @Override
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
        List<String> userTags = getUserInterestTags(userId);
        double tagMatchBonus = 1.0 + calculateTagMatchScore(post.getTags(), userTags) * 0.5;
        
        // 最终分数
        double finalScore = baseScore * tagMatchBonus;
        
        return finalScore;
    }
    
    @Override
    public List<String> getUserInterestTags(Long userId) {
        if (userId == null || userId <= 0) {
            return new ArrayList<>();
        }
        // 简化实现：基于用户点赞的帖子标签提取兴趣
        // 实际可以使用更复杂的机器学习模型
        
        List<String> interestTags = new ArrayList<>();
        
        // 查询用户点赞的帖子
        List<PostLike> userLikes = postLikeMapper.selectList(
            new QueryWrapper<PostLike>().eq("user_id", userId)
                .last("LIMIT 50") // 最近50个点赞
        );
        
        if (userLikes != null && !userLikes.isEmpty()) {
            List<Long> postIds = userLikes.stream()
                    .map(PostLike::getPostId)
                    .collect(Collectors.toList());
            
            List<Post> likedPosts = postMapper.selectBatchIds(postIds);
            
            // 提取标签
            Set<String> tagSet = new HashSet<>();
            for (Post post : likedPosts) {
                if (post.getTags() != null && !post.getTags().isEmpty()) {
                    String[] tags = post.getTags().split(",");
                    for (String tag : tags) {
                        String trimmedTag = tag.trim();
                        if (!trimmedTag.isEmpty()) {
                            tagSet.add(trimmedTag);
                        }
                    }
                }
                // 如果没有tags，使用category作为标签
                if ((post.getTags() == null || post.getTags().isEmpty()) && 
                    post.getCategory() != null && !post.getCategory().isEmpty()) {
                    tagSet.add(post.getCategory().trim());
                }
            }
            
            interestTags = new ArrayList<>(tagSet);
        }
        
        return interestTags;
    }
    
    @Override
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
    private double calculateRecentInteraction(Long userId, Long authorId) {
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

    // ---------- 发现页热度推荐（更强热度算法） ----------

    @Override
    public List<Post> getRecommendPosts(Long userId, Integer page, Integer size, String tag) {
        int pageNum = (page == null || page < 1) ? 1 : page;
        int pageSize = (size == null || size < 1) ? 20 : Math.min(size, 50);

        // 1. 查询所有正常帖子
        QueryWrapper<Post> qw = new QueryWrapper<>();
        qw.eq("status", 1).orderByDesc("create_time");
        List<Post> allPosts = postMapper.selectList(qw);
        String normalizedTag = (tag == null) ? null : tag.trim();
        if (normalizedTag != null && !normalizedTag.isEmpty() && !"推荐".equals(normalizedTag)) {
            allPosts = allPosts.stream().filter(p -> {
                // 「视频」：有视频资源即入栏（不必手选 #视频）
                if (isDiscoverVideoTabTag(normalizedTag) && isPostVideoContent(p)) {
                    return true;
                }
                // 「猫咪 / 狗狗 / 养宠知识」：手选标签命中 **或** 正文/标题语义命中（小红书式：内容信号 + 用户标签）
                if (isDiscoverCatTabTag(normalizedTag) && matchesCatColumnSemantic(p)) {
                    return true;
                }
                if (isDiscoverDogTabTag(normalizedTag) && matchesDogColumnSemantic(p)) {
                    return true;
                }
                if (isDiscoverKnowledgeTabTag(normalizedTag) && matchesKnowledgeColumnSemantic(p)) {
                    return true;
                }
                String postTags = p.getTags() == null ? "" : p.getTags();
                String category = p.getCategory() == null ? "" : p.getCategory();
                return containsTag(postTags, normalizedTag) || normalizedTag.equalsIgnoreCase(category.trim());
            }).collect(Collectors.toList());
        }
        if (allPosts == null || allPosts.isEmpty()) {
            fillUserInfoAndLikeStatus(new ArrayList<>(), userId);
            return new ArrayList<>();
        }

        // 2. 用户兴趣标签：优先 user.interest_tags，否则从点赞行为推断
        List<String> userInterestTags = getUserInterestTagsForRecommend(userId);

        // 3. 计算热度分（互动质量 + 时间衰减 + 兴趣匹配 + 作者质量）
        List<Post> scored = new ArrayList<>();
        for (Post post : allPosts) {
            int collectCount = postCollectMapper.countByPostId(post.getId());
            double score = calculateBestHotScore(post, userInterestTags, collectCount);
            post.setRecommendationScore(score);
            scored.add(post);
        }

        // 4. 按 score 倒序
        scored.sort((a, b) -> Double.compare(b.getRecommendationScore(), a.getRecommendationScore()));

        // 4.1 同一作者去重打散：避免首屏全是同一用户（分数接近时常见）
        scored = diversifyPostsByAuthor(scored, 2);

        // 4.2 小样本增强：交互不足时提高探索比例，避免推荐“越刷越像”且重复
        int interactionCount = countUserInteractions(userId);
        boolean coldStartUser = interactionCount < COLD_START_INTERACTION_THRESHOLD;
        double exploreRatio = coldStartUser ? COLD_START_EXPLORE_RATIO : NORMAL_EXPLORE_RATIO;
        if (normalizedTag == null || normalizedTag.isEmpty() || "推荐".equals(normalizedTag)) {
            scored = injectExploration(scored, allPosts, pageNum, pageSize, userId, exploreRatio);
        }

        // 5. 分页
        int from = (pageNum - 1) * pageSize;
        if (from >= scored.size()) {
            fillUserInfoAndLikeStatus(new ArrayList<>(), userId);
            return new ArrayList<>();
        }
        int to = Math.min(from + pageSize, scored.size());
        List<Post> pageList = new ArrayList<>(scored.subList(from, to));

        // 6. 填充用户信息与点赞状态
        fillUserInfoAndLikeStatus(pageList, userId);
        return pageList;
    }

    /**
     * 统计用户有效互动量（点赞/收藏/评论）；用于冷启动判定。
     */
    private int countUserInteractions(Long userId) {
        if (userId == null || userId <= 0) {
            return 0;
        }
        int likes = postLikeMapper.selectCount(new QueryWrapper<PostLike>().eq("user_id", userId)).intValue();
        int collects = postCollectMapper.selectCount(new QueryWrapper<PostCollect>().eq("user_id", userId)).intValue();
        int comments = commentMapper.selectCount(new QueryWrapper<Comment>().eq("user_id", userId)).intValue();
        return likes + collects + comments;
    }

    /**
     * 将候选流按“主推荐 + 探索池”重排：
     * - 主推荐：保持算法主序（保证相关性）
     * - 探索池：从最新与随机中抽样（保证新颖性）
     */
    private List<Post> injectExploration(List<Post> ranked, List<Post> allPosts, int pageNum, int pageSize, Long userId, double exploreRatio) {
        if (ranked == null || ranked.isEmpty()) {
            return ranked;
        }
        int total = ranked.size();
        int from = Math.max(0, (pageNum - 1) * pageSize);
        if (from >= total) {
            return ranked;
        }
        int to = Math.min(from + pageSize, total);
        int currentPageSize = to - from;
        if (currentPageSize <= 1) {
            return ranked;
        }

        int exploreCount = (int) Math.round(currentPageSize * exploreRatio);
        exploreCount = Math.max(1, Math.min(exploreCount, Math.max(1, currentPageSize / 2)));
        int keepCount = currentPageSize - exploreCount;

        List<Post> basePage = new ArrayList<>(ranked.subList(from, to));
        List<Post> kept = new ArrayList<>(basePage.subList(0, Math.min(keepCount, basePage.size())));
        Set<Long> usedPostIds = kept.stream().map(Post::getId).filter(Objects::nonNull).collect(Collectors.toSet());

        // 探索池：优先最新帖，再补充随机帖
        List<Post> freshPool = allPosts.stream()
                .filter(Objects::nonNull)
                .filter(p -> p.getId() != null && !usedPostIds.contains(p.getId()))
                .sorted((a, b) -> {
                    LocalDateTime ta = a.getCreateTime();
                    LocalDateTime tb = b.getCreateTime();
                    if (ta == null && tb == null) return 0;
                    if (ta == null) return 1;
                    if (tb == null) return -1;
                    return tb.compareTo(ta);
                })
                .limit(Math.max(40, exploreCount * 6L))
                .collect(Collectors.toList());

        List<Post> randomPool = allPosts.stream()
                .filter(Objects::nonNull)
                .filter(p -> p.getId() != null && !usedPostIds.contains(p.getId()))
                .collect(Collectors.toList());
        Collections.shuffle(randomPool);

        List<Post> explored = new ArrayList<>();
        int freshTake = Math.max(1, (int) Math.ceil(exploreCount * 0.6));
        for (Post p : freshPool) {
            if (explored.size() >= freshTake) break;
            if (p.getId() != null && usedPostIds.add(p.getId())) {
                explored.add(p);
            }
        }
        for (Post p : randomPool) {
            if (explored.size() >= exploreCount) break;
            if (p.getId() != null && usedPostIds.add(p.getId())) {
                explored.add(p);
            }
        }

        List<Post> mixedPage = new ArrayList<>(kept);
        mixedPage.addAll(explored);
        Collections.shuffle(mixedPage);

        // 替换当前页区间，其他页保持原有顺序
        List<Post> result = new ArrayList<>(ranked);
        int writeIndex = from;
        for (Post p : mixedPage) {
            if (writeIndex >= to) break;
            result.set(writeIndex++, p);
        }
        return result;
    }

    private double calculateBestHotScore(Post post, List<String> userInterestTags, int collectCount) {
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

    private boolean hasInterestMatch(Post post, List<String> userTags) {
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

    private boolean containsTag(String postTags, String targetTag) {
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
    private boolean isDiscoverVideoTabTag(String tag) {
        if (tag == null) return false;
        String t = tag.trim();
        return "视频".equals(t) || "video".equalsIgnoreCase(t);
    }

    /**
     * 是否为视频帖：有 videos JSON、或 images 字段里含常见视频后缀（发布时 category 常为「推荐」也可进视频流）
     */
    private boolean isPostVideoContent(Post p) {
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
    private String postTextBag(Post p) {
        if (p == null) return "";
        StringBuilder sb = new StringBuilder();
        if (p.getTitle() != null) sb.append(p.getTitle()).append(' ');
        if (p.getContent() != null) sb.append(p.getContent()).append(' ');
        if (p.getTags() != null) sb.append(p.getTags()).append(' ');
        if (p.getCategory() != null) sb.append(p.getCategory());
        return sb.toString();
    }

    private boolean isDiscoverCatTabTag(String tag) {
        if (tag == null) return false;
        String t = tag.trim();
        return "猫咪".equals(t) || "cat".equalsIgnoreCase(t);
    }

    private boolean isDiscoverDogTabTag(String tag) {
        if (tag == null) return false;
        String t = tag.trim();
        return "狗狗".equals(t) || "狗".equals(t) || "dog".equalsIgnoreCase(t);
    }

    private boolean isDiscoverKnowledgeTabTag(String tag) {
        if (tag == null) return false;
        String t = tag.trim();
        return "养宠知识".equals(t) || "百科".equals(t);
    }

    /** 猫咪栏：含猫/喵等语义即入（排除「熊猫」等误伤） */
    private boolean matchesCatColumnSemantic(Post p) {
        String bag = postTextBag(p);
        if (bag.isEmpty()) return false;
        if (bag.contains("熊猫")) return false;
        return bag.contains("猫咪") || bag.contains("猫猫") || bag.contains("喵星人") || bag.contains("养猫")
                || bag.contains("英短") || bag.contains("布偶") || bag.contains("橘猫")
                || bag.contains("猫") || bag.contains("喵");
    }

    /** 狗狗栏：含犬/狗等；排除「热狗」等 */
    private boolean matchesDogColumnSemantic(Post p) {
        String bag = postTextBag(p);
        if (bag.isEmpty()) return false;
        if (bag.contains("热狗")) return false;
        return bag.contains("狗狗") || bag.contains("狗子") || bag.contains("养狗") || bag.contains("汪星人")
                || bag.contains("柯基") || bag.contains("金毛") || bag.contains("哈士奇")
                || bag.contains("犬") || bag.contains("狗");
    }

    /** 养宠知识栏：科普/教程/医疗向关键词 */
    private boolean matchesKnowledgeColumnSemantic(Post p) {
        String bag = postTextBag(p);
        if (bag.isEmpty()) return false;
        String[] kws = {"科普", "攻略", "教程", "疫苗", "驱虫", "疾病", "领养", "注意", "建议", "干货",
                "养宠", "怎么养", "如何养", "为什么", "指南", "新手", "误区", "医院", "症状", "治疗"};
        for (String k : kws) {
            if (bag.contains(k)) return true;
        }
        return bag.contains("怎么") || bag.contains("如何");
    }

    /** 用户兴趣：优先 user.interest_tags，否则从点赞行为推断 */
    private List<String> getUserInterestTagsForRecommend(Long userId) {
        if (userId == null) return new ArrayList<>();
        User user = userMapper.selectById(userId);
        if (user != null && user.getInterestTags() != null && !user.getInterestTags().trim().isEmpty()) {
            List<String> list = new ArrayList<>();
            for (String t : user.getInterestTags().split(",")) {
                String x = t.trim();
                if (!x.isEmpty()) list.add(x);
            }
            if (!list.isEmpty()) return list;
        }
        return getUserInterestTags(userId);
    }

    @Override
    public List<Post> getHotPosts(Integer limit) {
        return getHotPostsInternal(limit, null);
    }

    /**
     * 热度榜；viewerUserId 用于填充当前用户是否已点赞（协同过滤回退热度时必须传入，否则列表心形永远为未赞）。
     */
    private List<Post> getHotPostsInternal(Integer limit, Long viewerUserId) {
        int topN = (limit == null || limit < 1) ? 20 : Math.min(limit, 100);
        // 拉取较大候选池，按热度算法重排再截断
        QueryWrapper<Post> qw = new QueryWrapper<>();
        qw.eq("status", 1).orderByDesc("create_time").last("LIMIT 300");
        List<Post> candidates = postMapper.selectList(qw);
        if (candidates == null || candidates.isEmpty()) {
            return new ArrayList<>();
        }

        List<Post> scored = new ArrayList<>();
        for (Post post : candidates) {
            int collectCount = postCollectMapper.countByPostId(post.getId());
            post.setRecommendationScore(calculateBestHotScore(post, Collections.emptyList(), collectCount));
            scored.add(post);
        }

        scored.sort((a, b) -> Double.compare(
                b.getRecommendationScore() != null ? b.getRecommendationScore() : 0.0,
                a.getRecommendationScore() != null ? a.getRecommendationScore() : 0.0
        ));
        List<Post> result = scored.subList(0, Math.min(topN, scored.size()));
        fillUserInfoAndLikeStatus(result, viewerUserId);
        return result;
    }

    /**
     * 在保持分数大致优先的前提下，限制连续高分中同一作者出现次数，将超出部分顺延到后面。
     */
    private List<Post> diversifyPostsByAuthor(List<Post> sortedByScore, int maxPerAuthor) {
        if (sortedByScore == null || sortedByScore.isEmpty() || maxPerAuthor < 1) {
            return sortedByScore;
        }
        List<Post> taken = new ArrayList<>();
        List<Post> deferred = new ArrayList<>();
        Map<Long, Integer> authorCount = new HashMap<>();
        for (Post p : sortedByScore) {
            if (p == null) {
                continue;
            }
            if (p.getUserId() == null) {
                deferred.add(p);
                continue;
            }
            long uid = p.getUserId();
            if (authorCount.getOrDefault(uid, 0) < maxPerAuthor) {
                taken.add(p);
                authorCount.merge(uid, 1, Integer::sum);
            } else {
                deferred.add(p);
            }
        }
        taken.addAll(deferred);
        return taken;
    }

    /**
     * 基于用户协同过滤（User-Based CF）推荐。
     * <p>
     * 实现逻辑：
     * 1) 构建用户-帖子互动评分矩阵（点赞/收藏/评论聚合成分值）；
     * 2) 用余弦相似度计算目标用户与其它用户的相似度；
     * 3) 选取 TopK 相似用户，汇总其高分帖子做加权预测；
     * 4) 过滤目标用户已互动帖子，按预测分降序返回；
     * 5) 冷启动（互动少于3条）或推荐结果为空时，回退热度推荐。
     */
    @Override
    public List<Post> getCollaborativeRecommendations(Long userId, int limit) {
        int topN = (limit <= 0) ? 20 : Math.min(limit, 100);
        if (userId == null || userId <= 0) {
            log.warn("协同过滤入参异常，userId={}, 回退热度推荐", userId);
            return getHotPostsInternal(topN, userId);
        }

        try {
            // 1) 读取行为权重配置（可在DB里改，不用改代码）
            Map<String, Double> interactionWeights = loadInteractionWeights();

            // 2) 优先从 user_post_interaction 构建矩阵；无数据时回退到旧表聚合
            Map<Long, Map<Long, Double>> userItemMatrix = buildMatrixFromInteractionTable(interactionWeights);
            if (userItemMatrix.isEmpty()) {
                log.warn("user_post_interaction 无可用数据，回退旧表聚合构建矩阵");
                userItemMatrix = buildMatrixFromLegacyTables(interactionWeights);
            }

            Map<Long, Double> targetVector = userItemMatrix.get(userId);
            // 冷启动：互动少于3条，直接走热度推荐
            if (targetVector == null || targetVector.size() < 3) {
                log.info("用户{}互动不足({})，回退热度推荐", userId, targetVector == null ? 0 : targetVector.size());
                return getHotPostsInternal(topN, userId);
            }

            // 2. 计算目标用户与其它用户的余弦相似度
            Map<Long, Double> userSimilarities = new HashMap<>();
            for (Map.Entry<Long, Map<Long, Double>> entry : userItemMatrix.entrySet()) {
                Long otherUserId = entry.getKey();
                if (Objects.equals(otherUserId, userId)) {
                    continue;
                }
                double sim = cosineSimilarity(targetVector, entry.getValue());
                if (sim > 0) {
                    userSimilarities.put(otherUserId, sim);
                }
            }

            if (userSimilarities.isEmpty()) {
                log.info("用户{}没有可用相似用户，回退热度推荐", userId);
                return getHotPostsInternal(topN, userId);
            }

            // 3. 选取 TopK 相似用户（数据量不大，固定K即可）
            final int topK = 20;
            List<Map.Entry<Long, Double>> nearestUsers = userSimilarities.entrySet().stream()
                    .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                    .limit(topK)
                    .collect(Collectors.toList());

            // 4. 基于相似用户做帖子预测评分
            // 预测公式：pred(i) = sum(sim(u,v) * score(v,i)) / sum(|sim(u,v)|)
            Set<Long> seenPostIds = targetVector.keySet();
            Map<Long, Double> weightedScoreSum = new HashMap<>();
            Map<Long, Double> similarityAbsSum = new HashMap<>();

            for (Map.Entry<Long, Double> neighbor : nearestUsers) {
                Long neighborId = neighbor.getKey();
                Double similarity = neighbor.getValue();
                Map<Long, Double> neighborVector = userItemMatrix.get(neighborId);
                if (neighborVector == null || neighborVector.isEmpty()) {
                    continue;
                }

                for (Map.Entry<Long, Double> itemScore : neighborVector.entrySet()) {
                    Long postId = itemScore.getKey();
                    if (seenPostIds.contains(postId)) {
                        // 排除目标用户已互动帖子
                        continue;
                    }
                    double score = itemScore.getValue();
                    weightedScoreSum.merge(postId, similarity * score, Double::sum);
                    similarityAbsSum.merge(postId, Math.abs(similarity), Double::sum);
                }
            }

            Map<Long, Double> predictedScores = new HashMap<>();
            for (Map.Entry<Long, Double> e : weightedScoreSum.entrySet()) {
                Long postId = e.getKey();
                double denom = similarityAbsSum.getOrDefault(postId, 0.0);
                if (denom > 0) {
                    predictedScores.put(postId, e.getValue() / denom);
                }
            }

            if (predictedScores.isEmpty()) {
                log.info("用户{}协同过滤预测为空，回退热度推荐", userId);
                return getHotPostsInternal(topN, userId);
            }

            // 5. 拉取帖子并按预测分排序，只保留正常状态帖子
            List<Long> sortedPostIds = predictedScores.entrySet().stream()
                    .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                    .limit(topN * 3L)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            if (sortedPostIds.isEmpty()) {
                return getHotPostsInternal(topN, userId);
            }

            List<Post> candidatePosts = postMapper.selectList(
                    new QueryWrapper<Post>()
                            .in("id", sortedPostIds)
                            .eq("status", 1)
            );

            Map<Long, Post> postMap = candidatePosts.stream()
                    .collect(Collectors.toMap(Post::getId, p -> p, (a, b) -> a));

            List<Post> result = sortedPostIds.stream()
                    .map(postMap::get)
                    .filter(Objects::nonNull)
                    .peek(post -> post.setRecommendationScore(predictedScores.getOrDefault(post.getId(), 0.0)))
                    .limit(topN)
                    .collect(Collectors.toList());

            if (result.isEmpty()) {
                log.info("用户{}协同过滤命中帖子为空，回退热度推荐", userId);
                return getHotPostsInternal(topN, userId);
            }

            // 填充作者信息、点赞状态等前端依赖字段
            fillUserInfoAndLikeStatus(result, userId);
            log.info("用户{}协同过滤推荐完成，返回{}条", userId, result.size());
            return result;
        } catch (Exception e) {
            log.error("用户{}协同过滤推荐异常，回退热度推荐", userId, e);
            return getHotPostsInternal(topN, userId);
        }
    }

    /**
     * 从配置表读取行为权重。读取失败或缺失时使用默认值兜底。
     */
    private Map<String, Double> loadInteractionWeights() {
        Map<String, Double> weights = new HashMap<>();
        weights.put("like", DEFAULT_LIKE_SCORE);
        weights.put("comment", DEFAULT_COMMENT_SCORE);
        weights.put("collect", DEFAULT_COLLECT_SCORE);
        weights.put("view", DEFAULT_VIEW_SCORE);

        try {
            List<RecommendationInteractionWeight> dbWeights = recommendationInteractionWeightMapper.selectList(
                    new QueryWrapper<RecommendationInteractionWeight>().eq("status", 1)
            );
            for (RecommendationInteractionWeight config : dbWeights) {
                if (config.getInteractionType() == null || config.getWeight() == null) {
                    continue;
                }
                String type = config.getInteractionType().trim().toLowerCase();
                weights.put(type, config.getWeight().doubleValue());
            }
        } catch (Exception e) {
            log.warn("读取 recommendation_interaction_weight 失败，使用默认权重", e);
        }
        return weights;
    }

    /**
     * 从统一行为表 user_post_interaction 构建用户-帖子评分矩阵。
     * score 字段优先使用；若为空则根据 interaction_type 用配置权重映射。
     */
    private Map<Long, Map<Long, Double>> buildMatrixFromInteractionTable(Map<String, Double> weights) {
        Map<Long, Map<Long, Double>> matrix = new HashMap<>();
        try {
            List<UserPostInteraction> interactions = userPostInteractionMapper.selectList(
                    new QueryWrapper<UserPostInteraction>()
                            .select("user_id", "post_id", "interaction_type", "score")
            );

            for (UserPostInteraction interaction : interactions) {
                if (interaction.getUserId() == null || interaction.getPostId() == null) {
                    continue;
                }
                double score;
                if (interaction.getScore() != null) {
                    score = interaction.getScore().doubleValue();
                } else {
                    String type = interaction.getInteractionType() == null
                            ? ""
                            : interaction.getInteractionType().trim().toLowerCase();
                    score = weights.getOrDefault(type, 1.0);
                }
                matrix.computeIfAbsent(interaction.getUserId(), k -> new HashMap<>())
                        .merge(interaction.getPostId(), score, Double::sum);
            }
        } catch (Exception e) {
            log.warn("查询 user_post_interaction 失败，将回退旧表聚合", e);
        }
        return matrix;
    }

    /**
     * 兼容旧数据结构：从点赞/收藏/评论三张表聚合构建评分矩阵。
     */
    private Map<Long, Map<Long, Double>> buildMatrixFromLegacyTables(Map<String, Double> weights) {
        Map<Long, Map<Long, Double>> matrix = new HashMap<>();
        double likeWeight = weights.getOrDefault("like", DEFAULT_LIKE_SCORE);
        double collectWeight = weights.getOrDefault("collect", DEFAULT_COLLECT_SCORE);
        double commentWeight = weights.getOrDefault("comment", DEFAULT_COMMENT_SCORE);

        List<PostLike> allLikes = postLikeMapper.selectList(
                new QueryWrapper<PostLike>().select("user_id", "post_id")
        );
        for (PostLike like : allLikes) {
            if (like.getUserId() == null || like.getPostId() == null) {
                continue;
            }
            matrix.computeIfAbsent(like.getUserId(), k -> new HashMap<>())
                    .merge(like.getPostId(), likeWeight, Double::sum);
        }

        List<PostCollect> allCollects = postCollectMapper.selectList(
                new QueryWrapper<PostCollect>().select("user_id", "post_id")
        );
        for (PostCollect collect : allCollects) {
            if (collect.getUserId() == null || collect.getPostId() == null) {
                continue;
            }
            matrix.computeIfAbsent(collect.getUserId(), k -> new HashMap<>())
                    .merge(collect.getPostId(), collectWeight, Double::sum);
        }

        List<Comment> allComments = commentMapper.selectList(
                new QueryWrapper<Comment>()
                        .select("user_id", "post_id")
                        .isNotNull("post_id")
                        .eq("status", 1)
        );
        for (Comment comment : allComments) {
            if (comment.getUserId() == null || comment.getPostId() == null) {
                continue;
            }
            matrix.computeIfAbsent(comment.getUserId(), k -> new HashMap<>())
                    .merge(comment.getPostId(), commentWeight, Double::sum);
        }

        return matrix;
    }

    /**
     * 计算两个稀疏向量的余弦相似度
     */
    private double cosineSimilarity(Map<Long, Double> a, Map<Long, Double> b) {
        if (a == null || b == null || a.isEmpty() || b.isEmpty()) {
            return 0.0;
        }

        Map<Long, Double> small = a.size() <= b.size() ? a : b;
        Map<Long, Double> large = a.size() <= b.size() ? b : a;

        double dot = 0.0;
        for (Map.Entry<Long, Double> entry : small.entrySet()) {
            Double other = large.get(entry.getKey());
            if (other != null) {
                dot += entry.getValue() * other;
            }
        }

        if (dot == 0.0) {
            return 0.0;
        }

        double normA = 0.0;
        for (Double v : a.values()) {
            normA += v * v;
        }
        double normB = 0.0;
        for (Double v : b.values()) {
            normB += v * v;
        }
        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}

