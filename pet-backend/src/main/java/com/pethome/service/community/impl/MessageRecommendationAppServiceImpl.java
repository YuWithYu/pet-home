package com.pethome.service.community.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pethome.entity.Post;
import com.pethome.entity.PostLike;
import com.pethome.entity.User;
import com.pethome.entity.UserFollow;
import com.pethome.service.community.MessageRecommendationAppService;
import com.pethome.service.community.PostLikeService;
import com.pethome.service.community.PostService;
import com.pethome.service.user.UserFollowService;
import com.pethome.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 私信页推荐应用服务实现：原 MessageController 的推荐用户组装逻辑下沉至此。
 */
@Service
public class MessageRecommendationAppServiceImpl implements MessageRecommendationAppService {

    @Autowired
    private PostService postService;

    @Autowired
    private PostLikeService postLikeService;

    @Autowired
    private UserFollowService userFollowService;

    @Autowired(required = false)
    private UserService userService;

    @Override
    public Map<String, Object> getRecommendedUsers(Long userId, Integer limit) {
        List<Map<String, Object>> usersList = new ArrayList<>();

        if (userService == null) {
            Map<String, Object> result = new HashMap<>();
            result.put("users", usersList);
            return result;
        }

        // 获取当前用户已关注的用户ID列表
        List<UserFollow> followingList = userFollowService.getFollowingList(userId);
        Set<Long> followingIds = followingList.stream()
                .map(UserFollow::getFollowingId)
                .collect(Collectors.toSet());
        followingIds.add(userId); // 排除自己

        // 从数据库查询可推荐用户：状态正常、排除自己，按 id 倒序取候选后再过滤已关注
        List<User> users = new ArrayList<>();
        int wanted = Math.min(Math.max(limit != null ? limit : 10, 1), 50);
        // 多取一些候选，保证过滤掉已关注后仍能凑够 wanted 个
        int candidateLimit = Math.min(wanted + followingIds.size(), 200);
        List<User> candidates = userService.getRecentUsers(userId, true, candidateLimit);
        for (User candidate : candidates) {
            if (candidate == null || candidate.getId() == null) {
                continue;
            }
            if (followingIds.contains(candidate.getId())) {
                continue;
            }
            users.add(candidate);
            if (users.size() >= wanted) {
                break;
            }
        }

        for (User user : users) {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", user.getId());
            userMap.put("nickname", user.getNickname());
            // 安全修复：username 可能是手机号等账号标识，对外只回传展示名
            String displayName = user.getNickname();
            if (displayName == null || displayName.isEmpty()) {
                displayName = "用户" + user.getId();
            }
            userMap.put("username", displayName);
            userMap.put("avatar", user.getAvatar());
            userMap.put("gender", user.getGender());

            // 获取粉丝数
            int fansCount = userFollowService.getFollowerCount(user.getId());
            userMap.put("fansCount", fansCount);

            // 获取点赞数（需要从帖子点赞统计）
            try {
                QueryWrapper<Post> postWrapper = new QueryWrapper<>();
                postWrapper.eq("user_id", user.getId());
                postWrapper.eq("status", 1);
                List<Post> userPosts = postService.list(postWrapper);
                long totalLikes = 0;
                for (Post post : userPosts) {
                    QueryWrapper<PostLike> likeWrapper = new QueryWrapper<>();
                    likeWrapper.eq("post_id", post.getId());
                    totalLikes += postLikeService.count(likeWrapper);
                }
                userMap.put("likeCount", (int) totalLikes);
            } catch (Exception e) {
                userMap.put("likeCount", 0);
            }

            // 获取帖子数
            try {
                QueryWrapper<Post> postWrapper = new QueryWrapper<>();
                postWrapper.eq("user_id", user.getId());
                postWrapper.eq("status", 1);
                long postCount = postService.count(postWrapper);
                userMap.put("postCount", (int) postCount);
            } catch (Exception e) {
                userMap.put("postCount", 0);
            }

            // 检查是否已关注
            boolean isFollowed = userFollowService.isFollowing(userId, user.getId());
            userMap.put("isFollowed", isFollowed);

            usersList.add(userMap);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("users", usersList);
        return result;
    }
}
