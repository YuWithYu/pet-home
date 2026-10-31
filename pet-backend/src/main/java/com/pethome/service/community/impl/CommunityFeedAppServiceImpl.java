package com.pethome.service.community.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.common.BusinessException;
import com.pethome.entity.Banner;
import com.pethome.entity.Comment;
import com.pethome.entity.Post;
import com.pethome.entity.User;
import com.pethome.entity.UserFollow;
import com.pethome.service.common.BannerService;
import com.pethome.service.community.CommentService;
import com.pethome.service.community.CommunityFeedAppService;
import com.pethome.service.community.PostRecommendationService;
import com.pethome.service.community.PostService;
import com.pethome.service.user.UserFollowService;
import com.pethome.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 社区内容读取应用服务实现。
 * <p>
 * 原 CommunityController 中的列表流、详情、专题与视图组装逻辑整体下沉至此，
 * Controller 只保留身份解析与 Result 包装。
 */
@Service
public class CommunityFeedAppServiceImpl implements CommunityFeedAppService {

    @Autowired
    private PostService postService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserFollowService userFollowService;

    @Autowired
    private PostRecommendationService postRecommendationService;

    @Autowired
    private BannerService bannerService;

    @Override
    public Map<String, Object> getVideoFeed(Integer page, Integer size, Long userId) {
        Map<String, Object> resp = new HashMap<>();
        int pageNum = (page == null || page < 1) ? 1 : page;
        int pageSize = (size == null || size < 1) ? 10 : Math.min(size, 50);

        // 登录用户：按个性化推荐（视频标签）拉流；未登录：回退热度视频流
        if (userId != null && userId > 0) {
            List<Post> personalized = postRecommendationService.getRecommendPosts(userId, pageNum, pageSize, "视频");
            resp.put("postIds", toPostIds(personalized));
            resp.put("total", null);
            resp.put("hasMore", personalized.size() >= pageSize);
            resp.put("nextPage", pageNum + 1);
            resp.put("algorithm", "personalized-video");
        } else {
            Page<Post> postPage = new Page<>(pageNum, pageSize);
            IPage<Post> result = postService.getVideoFeed(postPage);
            resp.put("postIds", toPostIds(result.getRecords()));
            resp.put("total", result.getTotal());
            resp.put("hasMore", result.getCurrent() < result.getPages());
            resp.put("nextPage", result.getCurrent() + 1);
            resp.put("algorithm", "hot-video");
        }
        return resp;
    }

    @Override
    public Map<String, Object> getPostList(Integer page, Integer size, String category, Long userId, Long checkUserId) {
        Page<Post> postPage = new Page<>(page, size);
        IPage<Post> result = postService.getPostList(postPage, category, userId);

        // 填充作者信息；传入 checkUserId 时同时填充点赞状态
        if (result.getRecords() != null) {
            ((PostServiceImpl) postService).fillUserInfoAndLikeStatus(result.getRecords(), checkUserId);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("posts", result.getRecords());
        response.put("total", result.getTotal());
        response.put("pages", result.getPages());
        response.put("current", result.getCurrent());
        response.put("size", result.getSize());
        return response;
    }

    @Override
    public List<Map<String, Object>> getMentionUsers(Long postId, Long userId) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        Post post = postService.getPostDetail(postId, userId);
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }
        List<Map<String, Object>> result = new ArrayList<>();
        Set<Long> added = new HashSet<>();
        // 1. 帖子作者优先（排除自己）
        if (post.getUserId() != null && !post.getUserId().equals(userId)) {
            User author = userService.getUserById(post.getUserId());
            if (author != null) {
                Map<String, Object> m = new HashMap<>();
                m.put("id", author.getId());
                // 安全修复：昵称为空时不得回退 username（历史用户 username 即手机号，会造成 PII 泄露）
                m.put("userName", author.getNickname() != null && !author.getNickname().isEmpty()
                        ? author.getNickname() : "用户" + author.getId());
                m.put("userAvatar", author.getAvatar());
                result.add(m);
                added.add(author.getId());
            }
        }
        // 2. 评论区用户（按评论时间近的优先，去重）
        List<Comment> comments = commentService.getCommentsByPostId(postId, userId);
        if (comments != null) {
            for (Comment c : comments) {
                if (c.getUserId() == null || c.getUserId().equals(userId) || added.contains(c.getUserId())) {
                    continue;
                }
                Map<String, Object> m = new HashMap<>();
                m.put("id", c.getUserId());
                m.put("userName", c.getUserName() != null ? c.getUserName() : "用户");
                m.put("userAvatar", c.getUserAvatar());
                result.add(m);
                added.add(c.getUserId());
            }
        }
        // 3. 关注的人（补充，最多再取10个）
        List<UserFollow> followList = userFollowService.getFollowingList(userId);
        int limit = 20 - result.size();
        if (followList != null && limit > 0) {
            for (UserFollow f : followList) {
                if (limit <= 0) {
                    break;
                }
                Long fid = f.getFollowingId();
                if (fid == null || fid.equals(userId) || added.contains(fid)) {
                    continue;
                }
                Map<String, Object> m = new HashMap<>();
                m.put("id", fid);
                m.put("userName", f.getFollowingName() != null ? f.getFollowingName() : "用户");
                m.put("userAvatar", f.getFollowingAvatar());
                result.add(m);
                added.add(fid);
                limit--;
            }
        }
        return result;
    }

    @Override
    public List<Comment> getPostComments(Long postId, Long userId) {
        return commentService.getCommentsByPostId(postId, userId);
    }

    @Override
    public List<Comment> getCommentReplies(Long commentId, Long userId) {
        return commentService.getRepliesByCommentId(commentId, userId);
    }

    @Override
    public Post getPostDetail(Long postId, Long userId) {
        Post post = postService.getPostDetail(postId, userId);
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }
        // 更新曝光分数（浏览）
        postService.updateExposureScoreOnInteraction(postId, "view");
        return post;
    }

    @Override
    public List<Post> getHotPosts(Integer limit) {
        return postService.getHotPosts(limit);
    }

    @Override
    public Map<String, List<Post>> getPostFeed(Integer topLimit, Integer hotLimit, Integer latestLimit,
                                               Integer followLimit, Integer exploreLimit, Long userId) {
        Map<String, List<Post>> feed = new HashMap<>();

        // 获取置顶帖
        feed.put("topPosts", postService.getTopPosts(topLimit));
        // 获取热门帖（按热度分数排序）
        feed.put("hotPosts", postService.getHotPostsByScore(hotLimit));
        // 获取最新帖
        feed.put("latestPosts", postService.getLatestPosts(latestLimit));

        // 获取关注区帖子（个性化排序）
        if (userId != null) {
            feed.put("followPosts", postRecommendationService.getFollowFeedPosts(userId, followLimit));
        } else {
            feed.put("followPosts", new ArrayList<Post>());
        }

        // 获取发现区帖子（智能推荐）；未登录返回普通热门帖
        if (userId != null) {
            feed.put("explorePosts", postRecommendationService.getExplorePosts(userId, exploreLimit));
        } else {
            feed.put("explorePosts", postService.getHotPostsByScore(exploreLimit));
        }
        return feed;
    }

    @Override
    public Map<String, Object> getRecommendPosts(Long userId, Integer page, Integer size, String tag) {
        List<Post> posts = postRecommendationService.getRecommendPosts(userId, page, size, tag);
        Map<String, Object> response = new HashMap<>();
        response.put("posts", posts);
        response.put("current", page);
        response.put("size", size);
        response.put("tag", tag);
        return response;
    }

    @Override
    public Map<String, Object> getFollowingPosts(Long userId, Integer page, Integer size) {
        Page<Post> postPage = new Page<>(page, size);
        IPage<Post> result = postService.getFollowingPosts(userId, postPage);

        Map<String, Object> response = new HashMap<>();
        response.put("posts", result.getRecords());
        response.put("total", result.getTotal());
        response.put("pages", result.getPages());
        return response;
    }

    @Override
    public Map<String, Object> getDailyTopics() {
        // 获取Banner作为轮播图（最多5个）
        List<Banner> allBanners = bannerService.getAllBanners();
        List<Banner> banners = allBanners != null && allBanners.size() > 5
                ? allBanners.subList(0, 5)
                : (allBanners != null ? allBanners : new ArrayList<>());

        // 获取热门帖子作为文章（最多5个）
        List<Post> hotPosts = postService.getHotPosts(5);

        // 获取今日主题（根据星期几，0=周日，1=周一...6=周六）
        String[] themes = {
                "猫咪护理", "狗狗训练", "宠物健康", "宠物美容",
                "宠物饮食", "宠物玩具", "宠物训练"
        };
        // getDayOfWeek().getValue() 返回 1-7 (周一到周日)，需要转换为 0-6
        int dayOfWeek = (java.time.LocalDate.now().getDayOfWeek().getValue() - 1) % 7;
        String todayTheme = themes[dayOfWeek];

        Map<String, Object> response = new HashMap<>();
        response.put("banners", banners);
        response.put("posts", hotPosts);
        response.put("todayTheme", todayTheme);
        response.put("date", java.time.LocalDate.now().toString());
        return response;
    }

    private List<Long> toPostIds(List<Post> posts) {
        return posts.stream()
                .map(Post::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
