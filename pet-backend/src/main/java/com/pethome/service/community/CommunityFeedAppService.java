package com.pethome.service.community;

import com.pethome.entity.Comment;
import com.pethome.entity.Post;

import java.util.List;
import java.util.Map;

/**
 * 社区内容读取应用服务：列表流、详情、推荐、专题与@提及用户。
 * <p>
 * Controller 只解析身份（登录用户 ID、前端筛选参数），服务层负责聚合多个领域服务并组装前端视图字段。
 */
public interface CommunityFeedAppService {

    /** 视频推荐流（类抖音）：仅视频帖，按热度/个性化分页 */
    Map<String, Object> getVideoFeed(Integer page, Integer size, Long userId);

    /** 帖子列表（分页 + 分类筛选，按需填充作者信息与点赞状态） */
    Map<String, Object> getPostList(Integer page, Integer size, String category, Long userId, Long checkUserId);

    /** @提及用户候选列表：帖子作者 > 评论区用户 > 关注的人 */
    List<Map<String, Object>> getMentionUsers(Long postId, Long userId);

    /** 帖子评论列表 */
    List<Comment> getPostComments(Long postId, Long userId);

    /** 评论的回复列表 */
    List<Comment> getCommentReplies(Long commentId, Long userId);

    /** 帖子详情（含作者信息与点赞状态，并累计曝光分） */
    Post getPostDetail(Long postId, Long userId);

    /** 热门帖子 */
    List<Post> getHotPosts(Integer limit);

    /** 首页 Feed：置顶 + 热门 + 最新 + 关注区 + 发现区 */
    Map<String, List<Post>> getPostFeed(Integer topLimit, Integer hotLimit, Integer latestLimit,
                                        Integer followLimit, Integer exploreLimit, Long userId);

    /** 发现页规则型推荐 */
    Map<String, Object> getRecommendPosts(Long userId, Integer page, Integer size, String tag);

    /** 关注用户的帖子 */
    Map<String, Object> getFollowingPosts(Long userId, Integer page, Integer size);

    /** 每日专题：Banner 轮播 + 热门文章 + 今日主题 */
    Map<String, Object> getDailyTopics();
}
