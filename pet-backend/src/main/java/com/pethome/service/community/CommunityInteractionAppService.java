package com.pethome.service.community;

import com.pethome.entity.Comment;

import java.util.Map;

/**
 * 社区互动应用服务：点赞、收藏与评论增删。
 * <p>
 * 所有写操作的用户身份均来自登录态（由 Controller 解析后传入），服务层负责校验与聚合。
 */
public interface CommunityInteractionAppService {

    /** 点赞帖子（成功时累计曝光分） */
    boolean likePost(Long postId, Long userId);

    /** 取消点赞帖子 */
    boolean unlikePost(Long postId, Long userId);

    /** 收藏帖子，重复收藏抛出业务异常 */
    boolean collectPost(Long postId, Long userId);

    /** 取消收藏帖子 */
    boolean uncollectPost(Long postId, Long userId);

    /** 当前用户是否已收藏该帖子 */
    boolean isCollected(Long postId, Long userId);

    /** 当前用户的收藏帖子列表（分页，含作者信息与点赞状态） */
    Map<String, Object> getCollectedPosts(Long userId, Integer page, Integer size, String type);

    /** 点赞评论 */
    boolean likeComment(Long commentId, Long userId);

    /** 取消点赞评论 */
    boolean unlikeComment(Long commentId, Long userId);

    /** 添加评论（回复时携带 parentId） */
    Comment addComment(Long postId, Map<String, Object> data, Long userId);

    /** 删除评论：本人或帖子作者可删 */
    boolean deleteComment(Long commentId, Long userId);
}
