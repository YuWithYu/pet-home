package com.pethome.service.community.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pethome.entity.Post;
import com.pethome.entity.Comment;
import com.pethome.entity.PostCollect;
import com.pethome.entity.PostLike;
import com.pethome.entity.UserFollow;
import com.pethome.mapper.CommentMapper;
import com.pethome.mapper.PostCollectMapper;
import com.pethome.mapper.PostLikeMapper;
import com.pethome.mapper.PostMapper;
import com.pethome.mapper.UserFollowMapper;
import com.pethome.service.community.PostRecommendationService;
import com.pethome.service.community.PostService;
import com.pethome.service.community.recommend.CollaborativeRecommendationService;
import com.pethome.service.community.recommend.RecommendationInterestTagService;
import com.pethome.service.community.recommend.RecommendationScoringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 帖子推荐服务实现
 * 负责推荐流程编排；打分、兴趣标签、协同过滤等算法由 recommend 包组件承担。
 */
@Service
public class PostRecommendationServiceImpl implements PostRecommendationService {

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
    private RecommendationScoringService recommendationScoringService;

    @Autowired
    private RecommendationInterestTagService recommendationInterestTagService;

    @Autowired
    private CollaborativeRecommendationService collaborativeRecommendationService;

    // 小样本场景参数：冷启动阈值与探索比例
    private static final int COLD_START_INTERACTION_THRESHOLD = 8;
    private static final double COLD_START_EXPLORE_RATIO = 0.35;
    private static final double NORMAL_EXPLORE_RATIO = 0.15;

    // 推荐候选池上限：限制召回规模，避免帖子量增长后全表加载进内存（OOM/长尾延迟）
    private static final int RECOMMEND_CANDIDATE_LIMIT = 500;

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
        if (postService instanceof com.pethome.service.community.impl.PostServiceImpl) {
            ((com.pethome.service.community.impl.PostServiceImpl) postService).fillUserInfoAndLikeStatus(posts, currentUserId);
        }
    }

    @Override
    public Double calculateFollowFeedScore(Post post, Long userId) {
        return recommendationScoringService.calculateFollowFeedScore(post, userId);
    }

    @Override
    public Double calculateExploreScore(Post post, Long userId) {
        return recommendationScoringService.calculateExploreScore(post, userId);
    }

    @Override
    public List<String> getUserInterestTags(Long userId) {
        return recommendationInterestTagService.getUserInterestTags(userId);
    }

    @Override
    public Double calculateTagMatchScore(String postTags, List<String> userTags) {
        return recommendationScoringService.calculateTagMatchScore(postTags, userTags);
    }

    // ---------- 发现页热度推荐（更强热度算法） ----------

    @Override
    public List<Post> getRecommendPosts(Long userId, Integer page, Integer size, String tag) {
        int pageNum = (page == null || page < 1) ? 1 : page;
        int pageSize = (size == null || size < 1) ? 20 : Math.min(size, 50);

        // 1. 查询正常帖子（按时间倒序取最近 RECOMMEND_CANDIDATE_LIMIT 条作为候选池，
        //    避免帖子量增长后全表加载进内存；命中 idx_status_create 索引）
        QueryWrapper<Post> qw = new QueryWrapper<>();
        qw.eq("status", 1).orderByDesc("create_time").last("LIMIT " + RECOMMEND_CANDIDATE_LIMIT);
        List<Post> allPosts = postMapper.selectList(qw);
        String normalizedTag = (tag == null) ? null : tag.trim();
        if (normalizedTag != null && !normalizedTag.isEmpty() && !"推荐".equals(normalizedTag)) {
            allPosts = allPosts.stream().filter(p -> {
                // 「视频」：有视频资源即入栏（不必手选 #视频）
                if (recommendationScoringService.isDiscoverVideoTabTag(normalizedTag)
                        && recommendationScoringService.isPostVideoContent(p)) {
                    return true;
                }
                // 「猫咪 / 狗狗 / 养宠知识」：手选标签命中 **或** 正文/标题语义命中（小红书式：内容信号 + 用户标签）
                if (recommendationScoringService.isDiscoverCatTabTag(normalizedTag)
                        && recommendationScoringService.matchesCatColumnSemantic(p)) {
                    return true;
                }
                if (recommendationScoringService.isDiscoverDogTabTag(normalizedTag)
                        && recommendationScoringService.matchesDogColumnSemantic(p)) {
                    return true;
                }
                if (recommendationScoringService.isDiscoverKnowledgeTabTag(normalizedTag)
                        && recommendationScoringService.matchesKnowledgeColumnSemantic(p)) {
                    return true;
                }
                String postTags = p.getTags() == null ? "" : p.getTags();
                String category = p.getCategory() == null ? "" : p.getCategory();
                return recommendationScoringService.containsTag(postTags, normalizedTag)
                        || normalizedTag.equalsIgnoreCase(category.trim());
            }).collect(Collectors.toList());
        }
        if (allPosts == null || allPosts.isEmpty()) {
            fillUserInfoAndLikeStatus(new ArrayList<>(), userId);
            return new ArrayList<>();
        }

        // 2. 用户兴趣标签：优先 user.interest_tags，否则从点赞行为推断
        List<String> userInterestTags = recommendationInterestTagService.getUserInterestTagsForRecommend(userId);

        // 3. 计算热度分（互动质量 + 时间衰减 + 兴趣匹配 + 作者质量）
        // 批量查询收藏数，单次 GROUP BY 替代逐帖 COUNT，消除 N+1
        Map<Long, Integer> collectCountMap = countCollectByPostIds(allPosts);

        List<Post> scored = new ArrayList<>();
        for (Post post : allPosts) {
            int collectCount = collectCountMap.getOrDefault(post.getId(), 0);
            double score = recommendationScoringService.calculateBestHotScore(post, userInterestTags, collectCount);
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
     * 批量统计帖子收藏数；单次 GROUP BY 查询替代逐帖 COUNT，消除 N+1。
     */
    private Map<Long, Integer> countCollectByPostIds(List<Post> posts) {
        List<Long> postIds = posts.stream()
                .map(Post::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (postIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, Integer> countMap = new HashMap<>();
        for (Map<String, Object> row : postCollectMapper.countByPostIds(postIds)) {
            countMap.put(((Number) row.get("postId")).longValue(), ((Number) row.get("cnt")).intValue());
        }
        return countMap;
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

        // 批量查询收藏数，单次 GROUP BY 替代逐帖 COUNT，消除 N+1
        Map<Long, Integer> collectCountMap = countCollectByPostIds(candidates);

        List<Post> scored = new ArrayList<>();
        for (Post post : candidates) {
            int collectCount = collectCountMap.getOrDefault(post.getId(), 0);
            post.setRecommendationScore(recommendationScoringService.calculateBestHotScore(post, Collections.emptyList(), collectCount));
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

    @Override
    public List<Post> getCollaborativeRecommendations(Long userId, int limit) {
        return collaborativeRecommendationService.getCollaborativeRecommendations(
                userId, limit,
                topN -> getHotPostsInternal(topN, userId),
                this::fillUserInfoAndLikeStatus);
    }
}
