package com.pethome.service.community;

import com.pethome.entity.Post;

import java.util.Map;

/**
 * 社区帖子写入应用服务：发布、编辑、删除。
 * <p>
 * 归属校验（只能操作自己的帖子）在服务层完成，Controller 只传入登录态用户 ID。
 */
public interface CommunityPostAppService {

    /** 发布帖子（入参为前端原始请求体，userId 必须来自登录态） */
    Post publishPost(Map<String, Object> requestData, Long userId);

    /** 更新帖子：仅允许修改内容字段，归属/审核/互动统计沿用库中现值 */
    Post updatePost(Long postId, Post post, Long userId);

    /** 删除帖子（软删除，status=0） */
    boolean deletePost(Long postId, Long userId);
}
