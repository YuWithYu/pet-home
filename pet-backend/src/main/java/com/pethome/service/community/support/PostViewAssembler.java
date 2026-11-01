package com.pethome.service.community.support;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pethome.entity.Post;
import com.pethome.entity.PostLike;
import com.pethome.entity.User;
import com.pethome.mapper.PostLikeMapper;
import com.pethome.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 帖子视图装配组件。
 * <p>
 * 批量补齐作者昵称/头像、视频封面与当前用户的点赞状态，不参与帖子查询与计数维护。
 */
@Service
public class PostViewAssembler {

    private static final Logger log = LoggerFactory.getLogger(PostViewAssembler.class);

    /** ObjectMapper 线程安全且创建昂贵，静态复用避免逐帖实例化 */
    private static final com.fasterxml.jackson.databind.ObjectMapper OBJECT_MAPPER =
            new com.fasterxml.jackson.databind.ObjectMapper();

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PostLikeMapper postLikeMapper;

    /**
     * 填充用户信息和点赞状态
     * 改为public，供其他服务调用
     * 批量查询作者信息与点赞状态，避免逐帖查询的 N+1
     */
    public void fillUserInfoAndLikeStatus(List<Post> posts, Long currentUserId) {
        if (posts == null || posts.isEmpty()) {
            return;
        }

        // 批量查询作者信息：单次 selectBatchIds 替代逐帖 selectById
        Set<Long> userIds = new HashSet<>();
        for (Post post : posts) {
            if (post.getUserId() != null) {
                userIds.add(post.getUserId());
            }
        }
        Map<Long, User> userMap = userIds.isEmpty()
                ? Collections.emptyMap()
                : userMapper.selectBatchIds(userIds).stream()
                        .collect(Collectors.toMap(User::getId, user -> user));

        // 批量查询点赞状态：单次 IN 查询替代逐帖 isLiked
        Set<Long> likedPostIds = new HashSet<>();
        if (currentUserId != null) {
            List<Long> postIds = new ArrayList<>();
            for (Post post : posts) {
                if (post.getId() != null) {
                    postIds.add(post.getId());
                }
            }
            if (!postIds.isEmpty()) {
                try {
                    List<PostLike> likes = postLikeMapper.selectList(
                            new QueryWrapper<PostLike>().eq("user_id", currentUserId).in("post_id", postIds));
                    for (PostLike like : likes) {
                        if (like.getPostId() != null) {
                            likedPostIds.add(like.getPostId());
                        }
                    }
                } catch (Exception e) {
                    // 查询失败时保持全部未点赞，不影响列表返回
                    log.error("批量查询点赞状态失败", e);
                }
            }
        }

        for (Post post : posts) {
            User user = userMap.get(post.getUserId());
            if (user != null) {
                // 只使用昵称，如果昵称为空则使用默认值（不使用手机号）
                String displayName = null;
                if (user.getNickname() != null && !user.getNickname().trim().isEmpty()) {
                    displayName = user.getNickname().trim();
                }
                // 如果昵称为空，使用默认格式，绝不使用手机号（username）
                post.setUserName(displayName != null && !displayName.isEmpty() ? displayName : "用户" + post.getUserId());
                // 优先使用avatar字段，但需要排除临时路径
                String avatar = (user.getAvatar() != null && !user.getAvatar().isEmpty()) ? user.getAvatar() : null;

                // 检查是否是临时路径（tmp路径）
                if (avatar != null && (avatar.contains("/tmp/") || avatar.contains("/__tmp__/") ||
                    avatar.startsWith("http://tmp/") || avatar.startsWith("http://__tmp__/") ||
                    avatar.startsWith("https://tmp/") || avatar.startsWith("https://__tmp__/"))) {
                    // 如果是临时路径，使用默认头像
                    avatar = null;
                    log.warn("检测到用户头像为临时路径，使用默认头像。用户ID: {}, 临时路径: {}", user.getId(), user.getAvatar());
                }

                // 使用正确的默认头像路径（默认头像可以使用静态资源路径）
                // 注意：用户上传的头像应保存在 /upload/ 目录，只有默认头像使用 /static/
                post.setUserAvatar(avatar != null ? avatar : "/static/images/garfield-default-avatar.png");
            }

            // 视频帖无封面时从 videos[0] 的 thumb/customThumb/cover 补全，方便列表展示
            if ((post.getCoverImage() == null || post.getCoverImage().trim().isEmpty()) && post.getVideos() != null && !post.getVideos().trim().isEmpty()) {
                try {
                    java.util.List<?> list = OBJECT_MAPPER.readValue(post.getVideos(), java.util.List.class);
                    if (list != null && !list.isEmpty() && list.get(0) instanceof java.util.Map) {
                        @SuppressWarnings("unchecked")
                        java.util.Map<String, Object> first = (java.util.Map<String, Object>) list.get(0);
                        String thumb = first.get("thumb") != null ? String.valueOf(first.get("thumb")).trim() : null;
                        if (thumb == null || thumb.isEmpty()) thumb = first.get("customThumb") != null ? String.valueOf(first.get("customThumb")).trim() : null;
                        if (thumb == null || thumb.isEmpty()) thumb = first.get("cover") != null ? String.valueOf(first.get("cover")).trim() : null;
                        if (thumb != null && !thumb.isEmpty()) post.setCoverImage(thumb);
                    }
                } catch (Exception ignored) {}
            }

            // 点赞状态：批量结果中命中即已点赞
            post.setIsLiked(likedPostIds.contains(post.getId()));
        }
    }
}
