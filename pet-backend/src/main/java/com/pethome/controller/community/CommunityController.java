package com.pethome.controller.community;

import com.pethome.common.Result;
import com.pethome.common.SafeCall;
import com.pethome.entity.Comment;
import com.pethome.entity.Post;
import com.pethome.service.community.CommunityFeedAppService;
import com.pethome.service.community.CommunityInteractionAppService;
import com.pethome.service.community.CommunityPostAppService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/community")
@Tag(name = "社区管理")
public class CommunityController {

    private static final Logger log = LoggerFactory.getLogger(CommunityController.class);

    /** 从登录态解析当前用户ID（RoleInterceptor 注入）。写操作一律以登录态为准，禁止信任前端传入的 userId */
    private Long currentUserId(HttpServletRequest request) {
        Object attr = request.getAttribute("userId");
        return attr instanceof Number ? ((Number) attr).longValue() : null;
    }

    @Autowired
    private CommunityFeedAppService communityFeedAppService;

    @Autowired
    private CommunityPostAppService communityPostAppService;

    @Autowired
    private CommunityInteractionAppService communityInteractionAppService;

    @GetMapping("/posts/video-feed")
    @Operation(summary = "视频推荐流（类抖音）：仅视频帖，按热度分页")
    public Result<Map<String, Object>> getVideoFeed(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long userId) {
        return SafeCall.of(() -> Result.success(
                communityFeedAppService.getVideoFeed(page, size, userId)),
                "获取视频推荐流失败，请稍后重试", log);
    }

    @GetMapping("/posts")
    @Operation(summary = "获取帖子列表")
    public Result<Map<String, Object>> getPostList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long checkUserId) {
        return SafeCall.of(() -> Result.success(
                communityFeedAppService.getPostList(page, size, category, userId, checkUserId)),
                "获取帖子列表失败，请稍后重试", log);
    }

    @GetMapping("/posts/{id}/mention-users")
    @Operation(summary = "获取@提及用户列表（抖音式：输入@后弹出选择）排序：帖子作者>评论区用户>关注的人")
    public Result<List<Map<String, Object>>> getMentionUsers(
            @PathVariable Long id,
            @RequestParam(required = false) Long userId) {
        return SafeCall.of(() -> Result.success(
                communityFeedAppService.getMentionUsers(id, userId)),
                "获取@用户列表失败，请稍后重试", log);
    }

    @GetMapping("/posts/{id}/comments")
    @Operation(summary = "获取帖子评论列表")
    public Result<List<Comment>> getPostComments(
            @PathVariable Long id,
            @RequestParam(required = false) Long userId) {
        return SafeCall.of(() -> Result.success(
                communityFeedAppService.getPostComments(id, userId)),
                "获取评论失败，请稍后重试", log);
    }

    @PostMapping("/comments/{commentId}/like")
    @Operation(summary = "点赞评论")
    public Result<Boolean> likeComment(@PathVariable Long commentId, HttpServletRequest request) {
        return SafeCall.of(() -> Result.success(
                communityInteractionAppService.likeComment(commentId, currentUserId(request))),
                "点赞评论失败，请稍后重试", log);
    }

    @DeleteMapping("/comments/{commentId}/like")
    @Operation(summary = "取消点赞评论")
    public Result<Boolean> unlikeComment(@PathVariable Long commentId, HttpServletRequest request) {
        return SafeCall.of(() -> Result.success(
                communityInteractionAppService.unlikeComment(commentId, currentUserId(request))),
                "取消点赞评论失败，请稍后重试", log);
    }

    @GetMapping("/comments/{commentId}/replies")
    @Operation(summary = "获取评论的回复列表")
    public Result<List<Comment>> getCommentReplies(
            @PathVariable Long commentId,
            @RequestParam(required = false) Long userId) {
        return SafeCall.of(() -> Result.success(
                communityFeedAppService.getCommentReplies(commentId, userId)),
                "获取回复失败，请稍后重试", log);
    }

    @DeleteMapping("/comments/{commentId}")
    @Operation(summary = "删除评论（本人或帖子作者可删）")
    public Result<Boolean> deleteComment(@PathVariable Long commentId, HttpServletRequest request) {
        return SafeCall.of(() -> Result.success(
                communityInteractionAppService.deleteComment(commentId, currentUserId(request))),
                "删除评论失败，请稍后重试", log);
    }

    @PostMapping("/posts/{id}/comments")
    @Operation(summary = "添加评论")
    public Result<Comment> addComment(@PathVariable Long id, @RequestBody Map<String, Object> data,
                                      HttpServletRequest request) {
        return SafeCall.of(() -> Result.success(
                communityInteractionAppService.addComment(id, data, currentUserId(request))),
                "添加评论失败，请稍后重试", log);
    }

    @GetMapping("/posts/{id}")
    @Operation(summary = "获取帖子详情")
    public Result<Post> getPostDetail(@PathVariable Long id, @RequestParam(required = false) Long userId) {
        return SafeCall.of(() -> Result.success(
                communityFeedAppService.getPostDetail(id, userId)),
                "获取帖子详情失败，请稍后重试", log);
    }

    @PostMapping("/posts")
    @Operation(summary = "发布帖子")
    public Result<Post> publishPost(@RequestBody Map<String, Object> requestData, HttpServletRequest request) {
        return SafeCall.of(() -> Result.success(
                communityPostAppService.publishPost(requestData, currentUserId(request))),
                "发布帖子失败，请稍后重试", log);
    }

    @PutMapping("/posts/{id}")
    @Operation(summary = "更新帖子")
    public Result<Post> updatePost(@PathVariable Long id, @RequestBody Post post, HttpServletRequest request) {
        return SafeCall.of(() -> Result.success(
                communityPostAppService.updatePost(id, post, currentUserId(request))),
                "更新帖子失败，请稍后重试", log);
    }

    @DeleteMapping("/posts/{id}")
    @Operation(summary = "删除帖子")
    public Result<Boolean> deletePost(@PathVariable Long id, HttpServletRequest request) {
        return SafeCall.of(() -> Result.success(
                communityPostAppService.deletePost(id, currentUserId(request))),
                "删除帖子失败，请稍后重试", log);
    }

    @PostMapping("/posts/{id}/like")
    @Operation(summary = "点赞帖子")
    public Result<Boolean> likePost(@PathVariable Long id, HttpServletRequest request) {
        return SafeCall.of(() -> Result.success(
                communityInteractionAppService.likePost(id, currentUserId(request))),
                "点赞失败，请稍后重试", log);
    }

    @DeleteMapping("/posts/{id}/like")
    @Operation(summary = "取消点赞帖子")
    public Result<Boolean> unlikePost(@PathVariable Long id, HttpServletRequest request) {
        return SafeCall.of(() -> Result.success(
                communityInteractionAppService.unlikePost(id, currentUserId(request))),
                "取消点赞失败，请稍后重试", log);
    }

    @PostMapping("/posts/{id}/collect")
    @Operation(summary = "收藏帖子")
    public Result<Boolean> collectPost(@PathVariable Long id, HttpServletRequest request) {
        return SafeCall.of(() -> Result.success(
                communityInteractionAppService.collectPost(id, currentUserId(request))),
                "收藏失败，请稍后重试", log);
    }

    @DeleteMapping("/posts/{id}/collect")
    @Operation(summary = "取消收藏帖子")
    public Result<Boolean> uncollectPost(@PathVariable Long id, HttpServletRequest request) {
        return SafeCall.of(() -> Result.success(
                communityInteractionAppService.uncollectPost(id, currentUserId(request))),
                "取消收藏失败，请稍后重试", log);
    }

    @GetMapping("/posts/{id}/collect/status")
    @Operation(summary = "检查收藏状态")
    public Result<Boolean> checkCollectStatus(@PathVariable Long id, @RequestParam(required = false) Long userId,
                                              HttpServletRequest request) {
        // 安全修复：收藏状态属个人数据，以登录态为准（query userId 仅兼容旧前端）
        return SafeCall.of(() -> Result.success(
                communityInteractionAppService.isCollected(id, currentUserId(request))),
                "检查收藏状态失败，请稍后重试", log);
    }

    @GetMapping("/posts/collected")
    @Operation(summary = "获取用户收藏的帖子列表")
    public Result<Map<String, Object>> getCollectedPosts(
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String type,
            HttpServletRequest request) {
        // 安全修复：收藏列表属个人数据，以登录态为准（query userId 仅兼容旧前端），防止越权查看他人收藏
        return SafeCall.of(() -> Result.success(
                communityInteractionAppService.getCollectedPosts(currentUserId(request), page, size, type)),
                "获取收藏列表失败，请稍后重试", log);
    }

    @GetMapping("/posts/hot")
    @Operation(summary = "获取热门帖子")
    public Result<List<Post>> getHotPosts(@RequestParam(defaultValue = "10") Integer limit) {
        return SafeCall.of(() -> Result.success(
                communityFeedAppService.getHotPosts(limit)),
                "获取热门帖子失败，请稍后重试", log);
    }

    @GetMapping("/posts/feed")
    @Operation(summary = "获取帖子Feed（置顶+热门+最新+关注区+发现区）")
    public Result<Map<String, List<Post>>> getPostFeed(
            @RequestParam(defaultValue = "5") Integer topLimit,
            @RequestParam(defaultValue = "10") Integer hotLimit,
            @RequestParam(defaultValue = "10") Integer latestLimit,
            @RequestParam(defaultValue = "20") Integer followLimit,
            @RequestParam(defaultValue = "20") Integer exploreLimit,
            @RequestParam(required = false) Long userId) {
        return SafeCall.of(() -> Result.success(
                communityFeedAppService.getPostFeed(topLimit, hotLimit, latestLimit, followLimit, exploreLimit, userId)),
                "获取帖子Feed失败，请稍后重试", log);
    }

    /**
     * 发现页规则型推荐（小红书风格）
     * score = 兴趣匹配(+5) + 点赞×2 + 收藏×3 + 评论×4 + 浏览×0.2 + 新帖24h(+20)
     */
    @GetMapping("/posts/recommend")
    @Operation(summary = "发现页推荐帖子（规则型排序）")
    public Result<Map<String, Object>> getRecommendPosts(
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String tag) {
        return SafeCall.of(() -> Result.success(
                communityFeedAppService.getRecommendPosts(userId, page, size, tag)),
                "获取推荐帖子失败，请稍后重试", log);
    }

    @GetMapping("/posts/following")
    @Operation(summary = "获取关注用户的帖子")
    public Result<Map<String, Object>> getFollowingPosts(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return SafeCall.of(() -> Result.success(
                communityFeedAppService.getFollowingPosts(userId, page, size)),
                "获取关注用户帖子失败，请稍后重试", log);
    }

    @GetMapping("/daily-topics")
    @Operation(summary = "获取每日专题")
    public Result<Map<String, Object>> getDailyTopics() {
        return SafeCall.of(() -> Result.success(
                communityFeedAppService.getDailyTopics()),
                "获取每日专题失败，请稍后重试", log);
    }
}
