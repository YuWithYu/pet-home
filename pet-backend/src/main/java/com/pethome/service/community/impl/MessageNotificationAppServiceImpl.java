package com.pethome.service.community.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.pethome.common.BusinessException;
import com.pethome.entity.Comment;
import com.pethome.entity.Message;
import com.pethome.entity.Post;
import com.pethome.entity.PostCollect;
import com.pethome.entity.PostLike;
import com.pethome.entity.User;
import com.pethome.entity.UserFollow;
import com.pethome.service.community.CommentService;
import com.pethome.service.community.IMessageService;
import com.pethome.service.community.MessageNotificationAppService;
import com.pethome.service.community.PostCollectService;
import com.pethome.service.community.PostLikeService;
import com.pethome.service.community.PostService;
import com.pethome.service.user.UserFollowService;
import com.pethome.service.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 消息通知应用服务实现：原 MessageController 的已读标记与点赞/收藏、关注、评论通知聚合逻辑下沉至此。
 */
@Service
public class MessageNotificationAppServiceImpl implements MessageNotificationAppService {

    private static final Logger log = LoggerFactory.getLogger(MessageNotificationAppServiceImpl.class);

    @Autowired
    private IMessageService messageService;

    @Autowired
    private PostService postService;

    @Autowired
    private PostLikeService postLikeService;

    @Autowired
    private PostCollectService postCollectService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserFollowService userFollowService;

    @Autowired(required = false)
    private UserService userService;

    @Override
    public String markMessagesAsRead(Map<String, Object> request, boolean staffOrAdmin, Long loginUserId) {
        Long userId = Long.valueOf(request.get("userId").toString());
        String conversationId = request.get("conversationId").toString();

        // 安全修复保留：已读标记以登录态为准，且会话必须归属本人（防止恶意把他人会话标记已读）
        if (!staffOrAdmin) {
            if (loginUserId == null) {
                throw new BusinessException(401, "用户未登录或token无效");
            }
            userId = loginUserId;
            if (conversationId != null && !conversationId.trim().isEmpty()
                    && !MessageConversationGuard.owns(conversationId.trim(), loginUserId)) {
                throw new BusinessException(403, "无权操作此会话");
            }
        }

        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        if (conversationId == null || conversationId.trim().isEmpty()) {
            throw new BusinessException("会话ID不能为空");
        }

        // 获取该会话的所有未读消息
        List<Message> unreadMessages = messageService.findLetters(conversationId, 0, 1000);

        // 过滤出当前用户（或客服）的未读消息
        // 如果userId是负数，说明是客服ID，需要标记发送给客服的未读消息
        List<Long> messageIds = new ArrayList<>();
        for (Message msg : unreadMessages) {
            if (msg.getToId() != null && msg.getToId().equals(userId)
                    && msg.getStatus() != null && msg.getStatus() == 0) {
                messageIds.add(msg.getId());
            }
        }

        // 标记为已读
        if (!messageIds.isEmpty()) {
            int result = messageService.readMessage(messageIds);
            if (result > 0) {
                return "标记成功，已读 " + result + " 条消息";
            }
        }
        return "没有未读消息";
    }

    @Override
    public Map<String, Object> getLikesAndCollections(Long userId, Integer page, Integer size, Long lastSeenAt,
                                                      boolean staffOrAdmin, Long loginUserId) {
        // 安全修复保留：互动通知属个人数据，非管理端强制以登录态为准（防止枚举他人收到的点赞/收藏）
        if (!staffOrAdmin) {
            userId = loginUserId;
        }
        if (userId == null) {
            Map<String, Object> empty = new HashMap<>();
            empty.put("likes", new ArrayList<>());
            empty.put("total", 0);
            empty.put("page", page);
            empty.put("size", size);
            return empty;
        }
        List<Map<String, Object>> likesList = new ArrayList<>();

        // 获取该用户所有帖子的ID
        QueryWrapper<Post> postWrapper = new QueryWrapper<>();
        postWrapper.eq("user_id", userId);
        postWrapper.eq("status", 1);
        List<Post> userPosts = postService.list(postWrapper);
        List<Long> postIds = userPosts.stream().map(Post::getId).collect(Collectors.toList());

        if (postIds.isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            result.put("likes", likesList);
            result.put("total", 0);
            result.put("page", page);
            result.put("size", size);
            return result;
        }

        // 获取点赞记录
        QueryWrapper<PostLike> likeWrapper = new QueryWrapper<>();
        likeWrapper.in("post_id", postIds);
        likeWrapper.orderByDesc("create_time");
        List<PostLike> likes = postLikeService.list(likeWrapper);

        // 获取收藏记录
        QueryWrapper<PostCollect> collectWrapper = new QueryWrapper<>();
        collectWrapper.in("post_id", postIds);
        collectWrapper.orderByDesc("create_time");
        List<PostCollect> collects = new ArrayList<>();
        try {
            if (postCollectService instanceof IService) {
                @SuppressWarnings("unchecked")
                IService<PostCollect> service = (IService<PostCollect>) postCollectService;
                collects = service.list(collectWrapper);
            }
        } catch (Exception e) {
            // 如果PostCollect不存在，忽略
            log.warn("查询收藏列表失败（可能表不存在）", e);
        }

        LocalDateTime lastSeenTime = lastSeenAt != null && lastSeenAt > 0
                ? LocalDateTime.ofInstant(Instant.ofEpochMilli(lastSeenAt), ZoneId.systemDefault()) : null;

        // 合并点赞和收藏记录（只统计别人对我的：排除自己点赞/收藏自己的帖子）
        for (PostLike like : likes) {
            if (userId.equals(like.getUserId())) {
                continue;
            }
            Map<String, Object> item = new HashMap<>();
            item.put("id", like.getId());
            item.put("type", "like");
            item.put("postId", like.getPostId());
            item.put("userId", like.getUserId());
            item.put("createTime", like.getCreateTime());

            // 获取用户信息
            if (userService != null) {
                try {
                    User user = userService.getUserById(like.getUserId());
                    if (user != null) {
                        item.put("userName", user.getNickname() != null ? user.getNickname() : user.getUsername());
                        item.put("userAvatar", user.getAvatar());
                    }
                } catch (Exception e) {
                    // 忽略
                }
            }

            // 获取帖子信息
            Post post = postService.getById(like.getPostId());
            if (post != null) {
                item.put("postContent", post.getContent());
                if (post.getImages() != null && !post.getImages().isEmpty()) {
                    String[] images = post.getImages().split(",");
                    if (images.length > 0) {
                        item.put("postImage", images[0]);
                    }
                }
            }

            likesList.add(item);
        }

        // 添加收藏记录（排除自己收藏自己的帖子）
        for (PostCollect collect : collects) {
            if (collect == null || userId.equals(collect.getUserId())) {
                continue;
            }
            Map<String, Object> item = new HashMap<>();
            item.put("id", collect.getId() != null ? collect.getId() : 0L);
            item.put("type", "collect");
            item.put("postId", collect.getPostId() != null ? collect.getPostId() : 0L);
            item.put("userId", collect.getUserId() != null ? collect.getUserId() : 0L);
            item.put("createTime", collect.getCreateTime());

            // 获取用户信息
            if (userService != null) {
                try {
                    User user = userService.getUserById(collect.getUserId());
                    if (user != null) {
                        item.put("userName", user.getNickname() != null ? user.getNickname() : user.getUsername());
                        item.put("userAvatar", user.getAvatar());
                    }
                } catch (Exception e) {
                    // 忽略
                }
            }

            // 获取帖子信息
            Long postId = collect.getPostId();
            if (postId != null) {
                Post post = postService.getById(postId);
                if (post != null) {
                    item.put("postContent", post.getContent());
                    if (post.getImages() != null && !post.getImages().isEmpty()) {
                        String[] images = post.getImages().split(",");
                        if (images.length > 0) {
                            item.put("postImage", images[0]);
                        }
                    }
                }
            }

            likesList.add(item);
        }

        // 按时间排序
        likesList.sort((a, b) -> {
            LocalDateTime timeA = (LocalDateTime) a.get("createTime");
            LocalDateTime timeB = (LocalDateTime) b.get("createTime");
            if (timeA == null || timeB == null) {
                return 0;
            }
            return timeB.compareTo(timeA);
        });

        // 分页；未读数：传了 lastSeenAt 时只统计该时间之后的条数
        int offset = (page - 1) * size;
        int total = lastSeenTime == null ? likesList.size()
                : (int) likesList.stream().filter(m -> {
                    LocalDateTime t = (LocalDateTime) m.get("createTime");
                    return t != null && t.isAfter(lastSeenTime);
                }).count();
        List<Map<String, Object>> pagedList = likesList.stream()
                .skip(offset)
                .limit(size)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("likes", pagedList);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        return result;
    }

    @Override
    public Map<String, Object> getNewFollows(Long userId, Integer page, Integer size, Long lastSeenAt,
                                             boolean staffOrAdmin, Long loginUserId) {
        // 安全修复保留：关注通知属个人数据，非管理端强制以登录态为准
        Long resolvedUserId = userId;
        if (!staffOrAdmin) {
            resolvedUserId = loginUserId;
        }
        final Long targetUserId = resolvedUserId;
        if (targetUserId == null) {
            Map<String, Object> empty = new HashMap<>();
            empty.put("follows", new ArrayList<>());
            empty.put("total", 0);
            empty.put("page", page);
            empty.put("size", size);
            return empty;
        }
        LocalDateTime lastSeenTime = lastSeenAt != null && lastSeenAt > 0
                ? LocalDateTime.ofInstant(Instant.ofEpochMilli(lastSeenAt), ZoneId.systemDefault()) : null;
        // 查询关注该用户的记录（follower_id是关注者，following_id是被关注者）= 别人关注我
        QueryWrapper<UserFollow> wrapper = new QueryWrapper<>();
        wrapper.eq("following_id", targetUserId);
        wrapper.orderByDesc("create_time");

        List<UserFollow> follows = userFollowService.list(wrapper);

        List<Map<String, Object>> followsList = new ArrayList<>();

        for (UserFollow follow : follows) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", follow.getId());
            item.put("userId", follow.getFollowerId());
            item.put("createTime", follow.getCreateTime());

            // 获取用户信息
            if (userService != null) {
                try {
                    User user = userService.getUserById(follow.getFollowerId());
                    if (user != null) {
                        item.put("userName", user.getNickname() != null ? user.getNickname() : user.getUsername());
                        item.put("userAvatar", user.getAvatar());
                    }
                } catch (Exception e) {
                    // 忽略
                }
            }

            // 检查当前用户是否已关注该用户（回粉状态）
            boolean isFollowed = userFollowService.isFollowing(targetUserId, follow.getFollowerId());
            item.put("isFollowed", isFollowed);

            followsList.add(item);
        }

        // 分页；未读数：传了 lastSeenAt 时只统计该时间之后的条数
        int offset = (page - 1) * size;
        int total = lastSeenTime == null ? followsList.size()
                : (int) followsList.stream().filter(m -> {
                    LocalDateTime t = (LocalDateTime) m.get("createTime");
                    return t != null && t.isAfter(lastSeenTime);
                }).count();
        List<Map<String, Object>> pagedList = followsList.stream()
                .skip(offset)
                .limit(size)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("follows", pagedList);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        return result;
    }

    @Override
    public Map<String, Object> getCommentsAndMentions(Long userId, Integer page, Integer size, Long lastSeenAt,
                                                      boolean staffOrAdmin, Long loginUserId) {
        // 安全修复保留：评论通知属个人数据，非管理端强制以登录态为准
        Long resolvedUserId = userId;
        if (!staffOrAdmin) {
            resolvedUserId = loginUserId;
        }
        final Long targetUserId = resolvedUserId;
        if (targetUserId == null) {
            Map<String, Object> empty = new HashMap<>();
            empty.put("comments", new ArrayList<>());
            empty.put("total", 0);
            empty.put("page", page);
            empty.put("size", size);
            return empty;
        }
        LocalDateTime lastSeenTime = lastSeenAt != null && lastSeenAt > 0
                ? LocalDateTime.ofInstant(Instant.ofEpochMilli(lastSeenAt), ZoneId.systemDefault()) : null;
        // 获取该用户所有帖子的ID
        QueryWrapper<Post> postWrapper = new QueryWrapper<>();
        postWrapper.eq("user_id", targetUserId);
        postWrapper.eq("status", 1);
        List<Post> userPosts = postService.list(postWrapper);
        List<Long> postIds = userPosts.stream().map(Post::getId).collect(Collectors.toList());

        if (postIds.isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            result.put("comments", new ArrayList<>());
            result.put("total", 0);
            result.put("page", page);
            result.put("size", size);
            return result;
        }

        // 获取评论记录
        QueryWrapper<Comment> commentWrapper = new QueryWrapper<>();
        commentWrapper.in("post_id", postIds);
        commentWrapper.eq("status", 1);
        commentWrapper.orderByDesc("create_time");
        List<Comment> comments = commentService.list(commentWrapper);

        List<Map<String, Object>> commentsList = new ArrayList<>();

        for (Comment comment : comments) {
            // 只统计别人对我的：排除自己评论自己的帖子
            if (targetUserId.equals(comment.getUserId())) {
                continue;
            }
            Map<String, Object> item = new HashMap<>();
            item.put("id", comment.getId());
            item.put("type", "comment");
            item.put("postId", comment.getPostId());
            item.put("userId", comment.getUserId());
            item.put("content", comment.getContent());
            item.put("createTime", comment.getCreateTime());

            // 获取用户信息
            if (userService != null) {
                try {
                    User user = userService.getUserById(comment.getUserId());
                    if (user != null) {
                        item.put("userName", user.getNickname() != null ? user.getNickname() : user.getUsername());
                        item.put("userAvatar", user.getAvatar());
                    }
                } catch (Exception e) {
                    // 忽略
                }
            }

            // 获取帖子信息
            Post post = postService.getById(comment.getPostId());
            if (post != null) {
                item.put("postContent", post.getContent());
                if (post.getImages() != null && !post.getImages().isEmpty()) {
                    String[] images = post.getImages().split(",");
                    if (images.length > 0) {
                        item.put("postImage", images[0]);
                    }
                }
            }

            commentsList.add(item);
        }

        // @ 提及未单独建模时无法列出；此处仅返回评论流，与接口说明一致

        // 分页；未读数：传了 lastSeenAt 时只统计该时间之后的条数
        int offset = (page - 1) * size;
        int total = lastSeenTime == null ? commentsList.size()
                : (int) commentsList.stream().filter(m -> {
                    LocalDateTime t = (LocalDateTime) m.get("createTime");
                    return t != null && t.isAfter(lastSeenTime);
                }).count();
        List<Map<String, Object>> pagedList = commentsList.stream()
                .skip(offset)
                .limit(size)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("comments", pagedList);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        return result;
    }
}
