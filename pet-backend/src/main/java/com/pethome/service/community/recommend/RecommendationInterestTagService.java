package com.pethome.service.community.recommend;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pethome.entity.Post;
import com.pethome.entity.PostLike;
import com.pethome.entity.User;
import com.pethome.mapper.PostLikeMapper;
import com.pethome.mapper.PostMapper;
import com.pethome.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 推荐兴趣标签组件：基于用户画像与历史行为提取兴趣标签。
 */
@Component
public class RecommendationInterestTagService {

    @Autowired
    private PostLikeMapper postLikeMapper;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private UserMapper userMapper;

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

    /** 用户兴趣：优先 user.interest_tags，否则从点赞行为推断 */
    public List<String> getUserInterestTagsForRecommend(Long userId) {
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
}
