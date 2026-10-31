package com.pethome.service.community.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pethome.common.BusinessException;
import com.pethome.entity.Post;
import com.pethome.service.admin.TaskService;
import com.pethome.service.common.RedisCacheService;
import com.pethome.service.community.CommunityPostAppService;
import com.pethome.service.community.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 社区帖子写入应用服务实现。
 * <p>
 * 原 CommunityController 的发布/编辑/删除编排与校验逻辑下沉至此，
 * 归属校验、敏感字段保护、标签与任务进度联动保持不变。
 */
@Service
public class CommunityPostAppServiceImpl implements CommunityPostAppService {

    @Autowired
    private PostService postService;

    @Autowired(required = false)
    private RedisCacheService redisCacheService;

    @Autowired(required = false)
    private TaskService taskService;

    @Override
    public Post publishPost(Map<String, Object> requestData, Long userId) {
        // 安全修复：发帖人以登录态为准，忽略请求体中的 userId（防止替他人发帖）
        if (userId == null || userId <= 0) {
            throw new BusinessException(401, "请先登录");
        }
        Post post = new Post();
        post.setUserId(userId);
        post.setTitle((String) requestData.get("title"));
        post.setContent((String) requestData.get("content"));
        post.setCategory((String) requestData.getOrDefault("category", "推荐"));
        post.setImages((String) requestData.get("images"));
        post.setImageCount(requestData.get("imageCount") != null
                ? ((Number) requestData.get("imageCount")).intValue() : 0);

        // 先获取用户传入的封面图（如果有）
        String userCoverImage = (String) requestData.get("coverImage");

        // 处理视频信息
        Object videosObj = requestData.get("videos");
        if (videosObj != null) {
            String videosStr = null;
            if (videosObj instanceof String) {
                videosStr = (String) videosObj;
            } else {
                // 如果是对象或数组，转换为JSON字符串
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    videosStr = objectMapper.writeValueAsString(videosObj);
                } catch (Exception e) {
                    // 转换视频数据失败，保持 videosStr 为 null
                }
            }
            post.setVideos(videosStr);

            // 如果有视频但没有用户指定的封面图，使用视频封面作为封面图
            if ((userCoverImage == null || userCoverImage.trim().isEmpty())
                    && (post.getCoverImage() == null || post.getCoverImage().trim().isEmpty())) {
                try {
                    if (videosStr != null && !videosStr.trim().isEmpty()) {
                        ObjectMapper objectMapper = new ObjectMapper();
                        List<Map<String, Object>> videosList = objectMapper.readValue(videosStr,
                                objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));
                        if (videosList != null && !videosList.isEmpty() && videosList.get(0).containsKey("thumb")) {
                            String videoThumb = (String) videosList.get(0).get("thumb");
                            if (videoThumb != null && !videoThumb.trim().isEmpty()) {
                                post.setCoverImage(videoThumb);
                            }
                        }
                    }
                } catch (Exception e) {
                    // 解析视频封面失败，忽略
                }
            }
        }

        // 如果用户指定了封面图，优先使用用户指定的
        if (userCoverImage != null && !userCoverImage.trim().isEmpty()) {
            post.setCoverImage(userCoverImage);
        }

        post.setCreateTime(LocalDateTime.now());
        post.setUpdateTime(LocalDateTime.now());
        post.setStatus(2); // 新帖子默认为待审核，管理员审核通过后变为正常(1)

        boolean success = postService.save(post);
        if (!success) {
            throw new BusinessException("发布帖子失败");
        }

        // 清除用户统计缓存，确保统计数据实时更新
        Long postUserId = post.getUserId();
        if (postUserId != null && redisCacheService != null) {
            redisCacheService.clearUserStats(postUserId);
        }

        // 更新任务进度：发布内容任务（失败不影响发布流程）
        try {
            if (postUserId != null) {
                int todayPostCount = postService.getTodayPostCount(postUserId);
                if (taskService != null) {
                    taskService.updateTaskProgress(postUserId, "post_content", todayPostCount);
                }
            }
        } catch (Exception e) {
            // 任务进度更新失败不影响发布流程
        }

        // 处理标签：标签创建、热度累加、关联写入、post.tags 回写统一由服务层完成
        if (requestData.containsKey("tags")) {
            postService.savePostTags(post, parseTagNames(requestData.get("tags")));
        }
        return post;
    }

    @Override
    public Post updatePost(Long postId, Post post, Long userId) {
        // 安全修复：userId 从登录态解析，防止伪造 userId 修改他人帖子
        if (userId == null) {
            throw new BusinessException(401, "用户未登录或token无效");
        }
        Post existingPost = postService.getById(postId);
        if (existingPost == null || !existingPost.getUserId().equals(userId)) {
            throw new BusinessException("无权限修改此帖子");
        }

        post.setId(postId);
        // 安全修复：编辑仅允许改内容字段；归属/审核状态/互动统计等敏感字段以库中现值为准。
        // Post 数字/布尔字段有非 null 默认值（likesCount=0、status=1 等），
        // updateById 会全部带上——不覆盖会导致编辑后点赞数清零、待审核帖(status=2)直接变正常、置顶/热门标记被清掉。
        post.setUserId(existingPost.getUserId());
        post.setStatus(existingPost.getStatus());
        post.setLikesCount(existingPost.getLikesCount());
        post.setCommentsCount(existingPost.getCommentsCount());
        post.setViewsCount(existingPost.getViewsCount());
        post.setSharesCount(existingPost.getSharesCount());
        post.setIsTop(existingPost.getIsTop());
        post.setIsHot(existingPost.getIsHot());
        post.setExposureScore(existingPost.getExposureScore());
        post.setRecommendationScore(existingPost.getRecommendationScore());
        post.setCreateTime(existingPost.getCreateTime());
        post.setUpdateTime(LocalDateTime.now());
        boolean success = postService.updateById(post);
        return success ? post : null;
    }

    @Override
    public boolean deletePost(Long postId, Long userId) {
        // 安全修复：userId 从登录态解析，防止伪造 userId 删除他人帖子
        if (userId == null) {
            throw new BusinessException(401, "用户未登录或token无效");
        }
        Post post = postService.getById(postId);
        if (post == null || !post.getUserId().equals(userId)) {
            throw new BusinessException("无权限删除此帖子");
        }

        post.setStatus(0);
        post.setUpdateTime(LocalDateTime.now());
        boolean success = postService.updateById(post);
        if (success && redisCacheService != null) {
            redisCacheService.clearUserStats(post.getUserId());
        }
        return success;
    }

    /** 标签入参兼容 List 与逗号分隔字符串 */
    private List<String> parseTagNames(Object tagsObj) {
        List<String> tagNames = new ArrayList<>();
        if (tagsObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> tagsList = (List<Object>) tagsObj;
            for (Object tag : tagsList) {
                if (tag != null) {
                    tagNames.add(tag.toString().trim());
                }
            }
        } else if (tagsObj instanceof String) {
            String tagsStr = (String) tagsObj;
            if (!tagsStr.isEmpty()) {
                String[] tagsArray = tagsStr.split(",");
                for (String tag : tagsArray) {
                    if (!tag.trim().isEmpty()) {
                        tagNames.add(tag.trim());
                    }
                }
            }
        }
        return tagNames;
    }
}
