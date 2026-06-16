package com.pethome.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pethome.entity.Address;
import com.pethome.entity.Appointment;
import com.pethome.entity.AppointmentCancellationRequest;
import com.pethome.entity.AppointmentChangeRequest;
import com.pethome.entity.Booking;
import com.pethome.entity.Cart;
import com.pethome.entity.Comment;
import com.pethome.entity.CommentLike;
import com.pethome.entity.DoorCleaningAppointment;
import com.pethome.entity.GroomingAppointment;
import com.pethome.entity.HospitalAppointment;
import com.pethome.entity.Notification;
import com.pethome.entity.Order;
import com.pethome.entity.OrderItem;
import com.pethome.entity.Pet;
import com.pethome.entity.PointsRecord;
import com.pethome.entity.Post;
import com.pethome.entity.PostCollect;
import com.pethome.entity.PostLike;
import com.pethome.entity.ProductFavorite;
import com.pethome.entity.RefundRequest;
import com.pethome.entity.SignInRecord;
import com.pethome.entity.TaskRecord;
import com.pethome.entity.TopicCollect;
import com.pethome.entity.TopicLike;
import com.pethome.entity.UserFollow;
import com.pethome.mapper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户注销时清理其业务数据（帖子、订单、预约等），避免仅删 user 行导致孤儿数据。
 */
@Service
public class UserCascadeDeletionService {

    private static final Logger log = LoggerFactory.getLogger(UserCascadeDeletionService.class);

    @Autowired(required = false)
    private PostMapper postMapper;
    @Autowired(required = false)
    private PostTagMapper postTagMapper;
    @Autowired(required = false)
    private CommentMapper commentMapper;
    @Autowired(required = false)
    private CommentLikeMapper commentLikeMapper;
    @Autowired(required = false)
    private PostLikeMapper postLikeMapper;
    @Autowired(required = false)
    private PostCollectMapper postCollectMapper;
    @Autowired(required = false)
    private TopicLikeMapper topicLikeMapper;
    @Autowired(required = false)
    private TopicCollectMapper topicCollectMapper;
    @Autowired(required = false)
    private UserFollowMapper userFollowMapper;
    @Autowired(required = false)
    private PetMapper petMapper;
    @Autowired(required = false)
    private OrderMapper orderMapper;
    @Autowired(required = false)
    private OrderItemMapper orderItemMapper;
    @Autowired(required = false)
    private CartMapper cartMapper;
    @Autowired(required = false)
    private AddressMapper addressMapper;
    @Autowired(required = false)
    private PointsRecordMapper pointsRecordMapper;
    @Autowired(required = false)
    private TaskRecordMapper taskRecordMapper;
    @Autowired(required = false)
    private SignInRecordMapper signInRecordMapper;
    @Autowired(required = false)
    private AppointmentMapper appointmentMapper;
    @Autowired(required = false)
    private HospitalAppointmentMapper hospitalAppointmentMapper;
    @Autowired(required = false)
    private GroomingAppointmentMapper groomingAppointmentMapper;
    @Autowired(required = false)
    private DoorCleaningAppointmentMapper doorCleaningAppointmentMapper;
    @Autowired(required = false)
    private BookingMapper bookingMapper;
    @Autowired(required = false)
    private ProductFavoriteMapper productFavoriteMapper;
    @Autowired(required = false)
    private NotificationMapper notificationMapper;
    @Autowired(required = false)
    private RefundRequestMapper refundRequestMapper;
    @Autowired(required = false)
    private AppointmentCancellationRequestMapper appointmentCancellationRequestMapper;
    @Autowired(required = false)
    private AppointmentChangeRequestMapper appointmentChangeRequestMapper;

    @Transactional(rollbackFor = Exception.class)
    public void deleteAllDataForUser(Long userId) {
        if (userId == null) {
            return;
        }
        log.info("开始级联删除用户数据 userId={}", userId);

        // —— 社区：先处理用户帖子下的评论与互动 ——
        if (postMapper != null) {
            List<Post> myPosts = postMapper.selectList(new QueryWrapper<Post>().eq("user_id", userId));
            List<Long> postIds = myPosts.stream().map(Post::getId).filter(id -> id != null).collect(Collectors.toList());
            if (!postIds.isEmpty()) {
                if (commentMapper != null) {
                    for (Long pid : postIds) {
                        List<Comment> cs = commentMapper.selectList(new QueryWrapper<Comment>().eq("post_id", pid));
                        for (Comment c : cs) {
                            if (c.getId() != null && commentLikeMapper != null) {
                                commentLikeMapper.delete(new QueryWrapper<CommentLike>().eq("comment_id", c.getId()));
                            }
                        }
                        commentMapper.delete(new QueryWrapper<Comment>().eq("post_id", pid));
                    }
                }
                if (postTagMapper != null) {
                    for (Long pid : postIds) {
                        postTagMapper.deleteByPostId(pid);
                    }
                }
                if (postLikeMapper != null) {
                    postLikeMapper.delete(new QueryWrapper<PostLike>().in("post_id", postIds));
                }
                if (postCollectMapper != null) {
                    postCollectMapper.delete(new QueryWrapper<PostCollect>().in("post_id", postIds));
                }
                postMapper.delete(new QueryWrapper<Post>().eq("user_id", userId));
            }
        }

        // 用户在他帖下的评论、点赞
        if (commentMapper != null) {
            List<Comment> myComments = commentMapper.selectList(new QueryWrapper<Comment>().eq("user_id", userId));
            for (Comment c : myComments) {
                if (c.getId() != null && commentLikeMapper != null) {
                    commentLikeMapper.delete(new QueryWrapper<CommentLike>().eq("comment_id", c.getId()));
                }
            }
            commentMapper.delete(new QueryWrapper<Comment>().eq("user_id", userId));
        }
        if (commentLikeMapper != null) {
            commentLikeMapper.delete(new QueryWrapper<CommentLike>().eq("user_id", userId));
        }
        if (postLikeMapper != null) {
            postLikeMapper.delete(new QueryWrapper<PostLike>().eq("user_id", userId));
        }
        if (postCollectMapper != null) {
            postCollectMapper.delete(new QueryWrapper<PostCollect>().eq("user_id", userId));
        }
        if (topicLikeMapper != null) {
            topicLikeMapper.delete(new QueryWrapper<TopicLike>().eq("user_id", userId));
        }
        if (topicCollectMapper != null) {
            topicCollectMapper.delete(new QueryWrapper<TopicCollect>().eq("user_id", userId));
        }
        if (userFollowMapper != null) {
            userFollowMapper.delete(new QueryWrapper<UserFollow>().eq("follower_id", userId));
            userFollowMapper.delete(new QueryWrapper<UserFollow>().eq("following_id", userId));
        }

        // 订单与明细
        if (orderMapper != null && orderItemMapper != null) {
            List<Order> orders = orderMapper.selectList(new QueryWrapper<Order>().eq("user_id", userId));
            for (Order o : orders) {
                if (o.getId() != null) {
                    orderItemMapper.delete(new QueryWrapper<OrderItem>().eq("order_id", o.getId()));
                }
            }
            orderMapper.delete(new QueryWrapper<Order>().eq("user_id", userId));
        }
        if (cartMapper != null) {
            cartMapper.delete(new QueryWrapper<Cart>().eq("user_id", userId));
        }
        if (addressMapper != null) {
            addressMapper.delete(new QueryWrapper<Address>().eq("user_id", userId));
        }
        if (productFavoriteMapper != null) {
            productFavoriteMapper.delete(new QueryWrapper<ProductFavorite>().eq("user_id", userId));
        }
        if (refundRequestMapper != null) {
            refundRequestMapper.delete(new QueryWrapper<RefundRequest>().eq("user_id", userId));
        }

        // 预约类
        if (appointmentMapper != null) {
            appointmentMapper.delete(new QueryWrapper<Appointment>().eq("user_id", userId));
        }
        if (hospitalAppointmentMapper != null) {
            hospitalAppointmentMapper.delete(new QueryWrapper<HospitalAppointment>().eq("user_id", userId));
        }
        if (groomingAppointmentMapper != null) {
            groomingAppointmentMapper.delete(new QueryWrapper<GroomingAppointment>().eq("user_id", userId));
        }
        if (doorCleaningAppointmentMapper != null) {
            doorCleaningAppointmentMapper.delete(new QueryWrapper<DoorCleaningAppointment>().eq("user_id", userId));
        }
        if (bookingMapper != null) {
            bookingMapper.delete(new QueryWrapper<Booking>().eq("user_id", userId));
        }
        if (appointmentCancellationRequestMapper != null) {
            appointmentCancellationRequestMapper.delete(new QueryWrapper<AppointmentCancellationRequest>().eq("user_id", userId));
        }
        if (appointmentChangeRequestMapper != null) {
            appointmentChangeRequestMapper.delete(new QueryWrapper<AppointmentChangeRequest>().eq("user_id", userId));
        }

        // 宠物
        if (petMapper != null) {
            petMapper.delete(new QueryWrapper<Pet>().eq("user_id", userId));
        }

        // 积分、任务、签到
        if (pointsRecordMapper != null) {
            pointsRecordMapper.delete(new QueryWrapper<PointsRecord>().eq("user_id", userId));
        }
        if (taskRecordMapper != null) {
            taskRecordMapper.delete(new QueryWrapper<TaskRecord>().eq("user_id", userId));
        }
        if (signInRecordMapper != null) {
            signInRecordMapper.delete(new QueryWrapper<SignInRecord>().eq("user_id", userId));
        }
        if (notificationMapper != null) {
            notificationMapper.delete(new QueryWrapper<Notification>().eq("user_id", userId));
        }

        log.info("用户级联数据删除完成 userId={}", userId);
    }
}
