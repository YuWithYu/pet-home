package com.pethome.service.admin.support;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pethome.mapper.CommentLikeMapper;
import com.pethome.mapper.CommentMapper;
import com.pethome.mapper.PostCollectMapper;
import com.pethome.mapper.PostLikeMapper;
import com.pethome.mapper.PostMapper;
import com.pethome.mapper.UserFollowMapper;
import com.pethome.service.user.SignInService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * 每日任务进度计算器：按任务类型从业务表实时统计今日完成量。
 *
 * 说明：SignInService 反向依赖 TaskService（其 @Lazy 注入），本类同样以 @Lazy 注入以避免循环依赖。
 */
@Service
public class TaskProgressCalculator {

    private static final Logger log = LoggerFactory.getLogger(TaskProgressCalculator.class);

    @Autowired
    @Lazy
    private SignInService signInService;

    @Autowired
    private UserFollowMapper userFollowMapper;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private PostCollectMapper postCollectMapper;

    @Autowired
    private PostLikeMapper postLikeMapper;

    @Autowired
    private CommentLikeMapper commentLikeMapper;

    /**
     * 任务列表展示用进度：各分支独立容错，查询失败仅记录错误日志并返回 0。
     */
    public int calculateDisplayProgress(Long userId, String taskType, LocalDate date) {
        int progress = 0;
        if ("daily_signin".equals(taskType)) {
            // 检查今日是否已签到
            progress = signInService.hasSignedIn(userId, date) ? 1 : 0;
        } else if ("follow_user".equals(taskType)) {
            // 查询用户今日已关注的用户数量
            try {
                QueryWrapper<com.pethome.entity.UserFollow> followWrapper =
                    new QueryWrapper<>();
                followWrapper.eq("follower_id", userId);
                followWrapper.apply("DATE(create_time) = CURDATE()");
                progress = Math.toIntExact(userFollowMapper.selectCount(followWrapper));
            } catch (Exception e) {
                log.error("查询关注用户任务进度失败", e);
            }
        } else if ("post_content".equals(taskType)) {
            progress = queryTodayPostContentProgress(userId);
        } else if ("comment_content".equals(taskType)) {
            // 查询用户今日已发布的顶级评论数量
            try {
                QueryWrapper<com.pethome.entity.Comment> commentWrapper =
                    new QueryWrapper<>();
                commentWrapper.eq("user_id", userId);
                commentWrapper.and(w -> w.isNull("parent_id").or().eq("parent_id", 0));
                commentWrapper.apply("DATE(create_time) = CURDATE()");
                progress = Math.toIntExact(commentMapper.selectCount(commentWrapper));
            } catch (Exception e) {
                log.error("查询评论内容任务进度失败", e);
            }
        } else if ("collect_content".equals(taskType)) {
            // 查询用户今日已收藏的帖子数量
            try {
                QueryWrapper<com.pethome.entity.PostCollect> collectWrapper =
                    new QueryWrapper<>();
                collectWrapper.eq("user_id", userId);
                collectWrapper.apply("DATE(create_time) = CURDATE()");
                progress = Math.toIntExact(postCollectMapper.selectCount(collectWrapper));
            } catch (Exception e) {
                log.error("查询收藏内容任务进度失败", e);
            }
        } else if ("like_content".equals(taskType)) {
            // 查询用户今日已点赞的帖子数量
            try {
                progress = postLikeMapper.getTodayLikeCount(userId);
            } catch (Exception e) {
                log.error("查询点赞内容任务进度失败", e);
            }
        } else if ("like_comment".equals(taskType)) {
            // 查询用户今日已点赞的评论数量
            try {
                QueryWrapper<com.pethome.entity.CommentLike> likeCommentWrapper =
                    new QueryWrapper<>();
                likeCommentWrapper.eq("user_id", userId);
                likeCommentWrapper.apply("DATE(create_time) = CURDATE()");
                progress = Math.toIntExact(commentLikeMapper.selectCount(likeCommentWrapper));
            } catch (Exception e) {
                log.error("查询点赞评论任务进度失败", e);
            }
        }
        return progress;
    }

    /**
     * 领取奖励校验用进度：查询异常直接抛出（与领取流程原有行为一致，不做兜底）。
     */
    public int calculateClaimProgress(Long userId, String taskType, LocalDate date) {
        if ("daily_signin".equals(taskType)) {
            return signInService.hasSignedIn(userId, date) ? 1 : 0;
        }
        if ("follow_user".equals(taskType)) {
            QueryWrapper<com.pethome.entity.UserFollow> followWrapper = new QueryWrapper<>();
            followWrapper.eq("follower_id", userId);
            followWrapper.apply("DATE(create_time) = CURDATE()");
            return Math.toIntExact(userFollowMapper.selectCount(followWrapper));
        }
        if ("post_content".equals(taskType)) {
            QueryWrapper<com.pethome.entity.Post> postWrapper = new QueryWrapper<>();
            postWrapper.eq("user_id", userId);
            postWrapper.in("status", java.util.Arrays.asList(1, 2));
            postWrapper.apply("DATE(create_time) = CURDATE()");
            return Math.toIntExact(postMapper.selectCount(postWrapper));
        }
        if ("comment_content".equals(taskType)) {
            QueryWrapper<com.pethome.entity.Comment> commentWrapper = new QueryWrapper<>();
            commentWrapper.eq("user_id", userId);
            commentWrapper.and(w -> w.isNull("parent_id").or().eq("parent_id", 0));
            commentWrapper.apply("DATE(create_time) = CURDATE()");
            return Math.toIntExact(commentMapper.selectCount(commentWrapper));
        }
        if ("collect_content".equals(taskType)) {
            QueryWrapper<com.pethome.entity.PostCollect> collectWrapper = new QueryWrapper<>();
            collectWrapper.eq("user_id", userId);
            collectWrapper.apply("DATE(create_time) = CURDATE()");
            return Math.toIntExact(postCollectMapper.selectCount(collectWrapper));
        }
        if ("like_content".equals(taskType)) {
            return postLikeMapper.getTodayLikeCount(userId);
        }
        if ("like_comment".equals(taskType)) {
            QueryWrapper<com.pethome.entity.CommentLike> likeCommentWrapper = new QueryWrapper<>();
            likeCommentWrapper.eq("user_id", userId);
            likeCommentWrapper.apply("DATE(create_time) = CURDATE()");
            return Math.toIntExact(commentLikeMapper.selectCount(likeCommentWrapper));
        }
        return 0;
    }

    /** 今日已发帖数：正常 + 待审核（与 PostServiceImpl.getTodayPostCount 一致） */
    public int queryTodayPostContentProgress(Long userId) {
        if (userId == null || postMapper == null) {
            return 0;
        }
        try {
            QueryWrapper<com.pethome.entity.Post> postWrapper = new QueryWrapper<>();
            postWrapper.eq("user_id", userId);
            postWrapper.in("status", java.util.Arrays.asList(1, 2));
            postWrapper.apply("DATE(create_time) = CURDATE()");
            return Math.toIntExact(postMapper.selectCount(postWrapper));
        } catch (Exception e) {
            log.error("queryTodayPostContentProgress 失败", e);
            return 0;
        }
    }
}
