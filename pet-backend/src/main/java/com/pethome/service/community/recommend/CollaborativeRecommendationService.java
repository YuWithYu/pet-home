package com.pethome.service.community.recommend;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pethome.entity.Comment;
import com.pethome.entity.Post;
import com.pethome.entity.PostCollect;
import com.pethome.entity.PostLike;
import com.pethome.entity.RecommendationInteractionWeight;
import com.pethome.entity.UserPostInteraction;
import com.pethome.mapper.CommentMapper;
import com.pethome.mapper.PostCollectMapper;
import com.pethome.mapper.PostLikeMapper;
import com.pethome.mapper.PostMapper;
import com.pethome.mapper.RecommendationInteractionWeightMapper;
import com.pethome.mapper.UserPostInteractionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 基于用户协同过滤（User-Based CF）推荐组件。
 * 纯算法实现；热度兜底与作者信息填充由调用方通过回调注入，避免包间反向依赖。
 */
@Component
public class CollaborativeRecommendationService {

    private static final Logger log = LoggerFactory.getLogger(CollaborativeRecommendationService.class);

    @Autowired
    private RecommendationInteractionWeightMapper recommendationInteractionWeightMapper;

    @Autowired
    private UserPostInteractionMapper userPostInteractionMapper;

    @Autowired
    private PostLikeMapper postLikeMapper;

    @Autowired
    private PostCollectMapper postCollectMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private PostMapper postMapper;

    // 协同过滤行为权重默认值（当配置表无数据时兜底）
    private static final double DEFAULT_LIKE_SCORE = 3.0;
    private static final double DEFAULT_COMMENT_SCORE = 2.0;
    private static final double DEFAULT_COLLECT_SCORE = 4.0;
    private static final double DEFAULT_VIEW_SCORE = 1.0;

    /**
     * 基于用户协同过滤（User-Based CF）推荐。
     * <p>
     * 实现逻辑：
     * 1) 构建用户-帖子互动评分矩阵（点赞/收藏/评论聚合成分值）；
     * 2) 用余弦相似度计算目标用户与其它用户的相似度；
     * 3) 选取 TopK 相似用户，汇总其高分帖子做加权预测；
     * 4) 过滤目标用户已互动帖子，按预测分降序返回；
     * 5) 冷启动（互动少于3条）或推荐结果为空时，回退热度推荐。
     *
     * @param hotFallback    热度推荐兜底：入参为截断条数，返回热度榜帖子
     * @param userInfoFiller 作者信息与点赞状态填充：入参为帖子列表与当前用户
     */
    public List<Post> getCollaborativeRecommendations(Long userId, int limit,
                                                      Function<Integer, List<Post>> hotFallback,
                                                      BiConsumer<List<Post>, Long> userInfoFiller) {
        int topN = (limit <= 0) ? 20 : Math.min(limit, 100);
        if (userId == null || userId <= 0) {
            log.warn("协同过滤入参异常，userId={}, 回退热度推荐", userId);
            return hotFallback.apply(topN);
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
                return hotFallback.apply(topN);
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
                return hotFallback.apply(topN);
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
                return hotFallback.apply(topN);
            }

            // 5. 拉取帖子并按预测分排序，只保留正常状态帖子
            List<Long> sortedPostIds = predictedScores.entrySet().stream()
                    .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                    .limit(topN * 3L)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            if (sortedPostIds.isEmpty()) {
                return hotFallback.apply(topN);
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
                return hotFallback.apply(topN);
            }

            // 填充作者信息、点赞状态等前端依赖字段
            userInfoFiller.accept(result, userId);
            log.info("用户{}协同过滤推荐完成，返回{}条", userId, result.size());
            return result;
        } catch (Exception e) {
            log.error("用户{}协同过滤推荐异常，回退热度推荐", userId, e);
            return hotFallback.apply(topN);
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
