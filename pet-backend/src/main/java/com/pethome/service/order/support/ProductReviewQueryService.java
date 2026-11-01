package com.pethome.service.order.support;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pethome.entity.ProductReview;
import com.pethome.mapper.ProductReviewMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 商品评价/销售记录/点赞/回复的只读查询。
 */
@Component
public class ProductReviewQueryService {

    private static final Logger logger = LoggerFactory.getLogger(ProductReviewQueryService.class);

    @Autowired
    private ProductReviewMapper productReviewMapper;

    @Autowired
    private com.pethome.mapper.UserMapper userMapper;

    @Autowired(required = false)
    private com.pethome.mapper.OrderMapper orderMapper;

    @Autowired(required = false)
    private com.pethome.mapper.OrderItemMapper orderItemMapper;

    @Autowired(required = false)
    private com.pethome.mapper.ReviewLikeMapper reviewLikeMapper;

    @Autowired(required = false)
    private com.pethome.mapper.ReviewReplyMapper reviewReplyMapper;

    public List<ProductReview> getProductReviews(Long productId) {
        QueryWrapper<ProductReview> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("product_id", productId);
        queryWrapper.eq("status", 1); // 只返回正常状态的评价
        queryWrapper.orderByDesc("create_time");

        List<ProductReview> reviews = productReviewMapper.selectList(queryWrapper);

        // 为每个评价设置用户信息（昵称和头像），并过滤临时路径图片
        for (ProductReview review : reviews) {
            // 过滤临时路径图片
            if (review.getImages() != null && !review.getImages().isEmpty()) {
                String imagesStr = review.getImages();
                // 如果是临时路径，清空图片字段
                if (imagesStr.startsWith("http://tmp/") || imagesStr.startsWith("https://tmp/") || imagesStr.startsWith("tmp/")) {
                    review.setImages(null);
                } else if (imagesStr.contains(",")) {
                    // 如果是逗号分隔的字符串，过滤掉临时路径
                    List<String> imageList = java.util.Arrays.asList(imagesStr.split(","));
                    List<String> validImages = imageList.stream()
                        .map(String::trim)
                        .filter(img -> !img.isEmpty()
                            && !img.startsWith("http://tmp/")
                            && !img.startsWith("https://tmp/")
                            && !img.startsWith("tmp/"))
                        .collect(java.util.stream.Collectors.toList());
                    if (validImages.isEmpty()) {
                        review.setImages(null);
                    } else {
                        review.setImages(String.join(",", validImages));
                    }
                }
            }

            if (review.getUserId() != null) {
                try {
                    // 从用户表查询用户信息
                    com.pethome.entity.User user = userMapper.selectById(review.getUserId());
                    if (user != null) {
                        // 设置用户昵称（优先使用nickname，其次是username）
                        review.setUserName(user.getNickname() != null && !user.getNickname().isEmpty()
                            ? user.getNickname()
                            : (user.getUsername() != null ? user.getUsername() : "用户" + review.getUserId()));
                        // 设置用户头像
                        if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                            review.setUserAvatar(user.getAvatar());
                        } else {
                            review.setUserAvatar("/static/images/garfield-default-avatar.png");
                        }
                    } else {
                        // 用户不存在，使用默认值
                        review.setUserName("用户" + review.getUserId());
                        review.setUserAvatar("/static/images/garfield-default-avatar.png");
                    }
                } catch (Exception e) {
                    // 查询用户信息失败，使用默认值
                    logger.warn("查询评价用户信息失败 - 用户ID: " + review.getUserId() + ", 错误: " + e.getMessage());
                    review.setUserName("用户" + review.getUserId());
                    review.setUserAvatar("/static/images/garfield-default-avatar.png");
                }
            } else {
                review.setUserName("匿名用户");
                review.setUserAvatar("/static/images/garfield-default-avatar.png");
            }

            // 设置点赞数和回复数
            if (reviewLikeMapper != null) {
                review.setLikeCount(getReviewLikeCount(review.getId()));
            } else {
                review.setLikeCount(0);
            }

            if (reviewReplyMapper != null) {
                review.setReplyCount(getReviewReplyCount(review.getId()));
                // 获取回复列表（可选，如果前端需要显示回复）
                // review.setReplies(getReviewReplies(review.getId()));
            } else {
                review.setReplyCount(0);
            }
        }

        return reviews;
    }

    public java.util.List<java.util.Map<String, Object>> getProductSalesRecords(Long productId) {
        try {
            if (orderItemMapper == null || orderMapper == null) {
                logger.warn("OrderItemMapper或OrderMapper未注入，返回空列表");
                return new java.util.ArrayList<>();
            }

            // 查询该商品的所有订单项（只查询已支付、已发货、已完成的订单）
            List<com.pethome.entity.OrderItem> orderItems = orderItemMapper.selectByProductId(productId);

            if (orderItems == null || orderItems.isEmpty()) {
                return new java.util.ArrayList<>();
            }

            // 构建销售记录列表
            java.util.List<java.util.Map<String, Object>> salesRecords = new java.util.ArrayList<>();

            for (com.pethome.entity.OrderItem item : orderItems) {
                // 查询订单信息
                com.pethome.entity.Order order = orderMapper.selectById(item.getOrderId());
                if (order == null) {
                    continue;
                }

                // 查询用户信息
                com.pethome.entity.User user = null;
                String customerName = "未知用户";
                if (order.getUserId() != null) {
                    try {
                        user = userMapper.selectById(order.getUserId());
                        if (user != null) {
                            // 优先使用nickname，其次使用username，最后使用"用户{id}"
                            if (user.getNickname() != null && !user.getNickname().trim().isEmpty()) {
                                customerName = user.getNickname();
                            } else if (user.getUsername() != null && !user.getUsername().trim().isEmpty()) {
                                customerName = user.getUsername();
                            } else {
                                customerName = "用户" + user.getId();
                            }
                        } else {
                            logger.warn("用户不存在 - 用户ID: {}", order.getUserId());
                            customerName = "用户" + order.getUserId();
                        }
                    } catch (Exception e) {
                        logger.error("查询用户信息失败 - 用户ID: {}, 错误: {}", order.getUserId(), e.getMessage());
                        customerName = "用户" + order.getUserId();
                    }
                } else {
                    logger.warn("订单用户ID为空 - 订单ID: {}", order.getId());
                }

                // 计算金额（单价 * 数量）
                java.math.BigDecimal amount = item.getPrice().multiply(new java.math.BigDecimal(item.getQuantity()));
                if (item.getSpecPrice() != null) {
                    amount = item.getSpecPrice().multiply(new java.math.BigDecimal(item.getQuantity()));
                }

                // 构建销售记录
                java.util.Map<String, Object> record = new java.util.HashMap<>();
                record.put("orderId", order.getOrderNo() != null ? order.getOrderNo() : "ORDER" + order.getId());
                record.put("customer", customerName);
                record.put("quantity", item.getQuantity());
                record.put("amount", amount);
                record.put("date", order.getCreateTime() != null ? order.getCreateTime().toString() : "");

                salesRecords.add(record);
            }

            return salesRecords;
        } catch (Exception e) {
            logger.error("获取商品销售记录失败: " + e.getMessage(), e);
            return new java.util.ArrayList<>();
        }
    }

    public boolean isReviewLiked(Long reviewId, Long userId) {
        try {
            if (reviewLikeMapper == null || reviewId == null || userId == null) {
                return false;
            }

            com.pethome.entity.ReviewLike like = reviewLikeMapper.findByReviewIdAndUserId(reviewId, userId);
            return like != null;
        } catch (Exception e) {
            logger.error("检查点赞状态失败: " + e.getMessage(), e);
            return false;
        }
    }

    public Integer getReviewLikeCount(Long reviewId) {
        try {
            if (reviewLikeMapper == null || reviewId == null) {
                return 0;
            }

            Integer count = reviewLikeMapper.countByReviewId(reviewId);
            return count != null ? count : 0;
        } catch (Exception e) {
            logger.error("获取点赞数失败: " + e.getMessage(), e);
            return 0;
        }
    }

    public java.util.List<com.pethome.entity.ReviewReply> getReviewReplies(Long reviewId) {
        try {
            if (reviewReplyMapper == null || reviewId == null) {
                return new java.util.ArrayList<>();
            }

            List<com.pethome.entity.ReviewReply> replies = reviewReplyMapper.findByReviewId(reviewId);

            // 填充用户信息
            if (replies != null && !replies.isEmpty() && userMapper != null) {
                for (com.pethome.entity.ReviewReply reply : replies) {
                    if (reply.getUserId() != null) {
                        com.pethome.entity.User user = userMapper.selectById(reply.getUserId());
                        if (user != null) {
                            reply.setUserName(user.getNickname() != null ? user.getNickname() :
                                             (user.getUsername() != null ? user.getUsername() : "用户" + reply.getUserId()));
                            reply.setUserAvatar(user.getAvatar());
                        }
                    }
                }
            }

            return replies != null ? replies : new java.util.ArrayList<>();
        } catch (Exception e) {
            logger.error("获取回复列表失败: " + e.getMessage(), e);
            return new java.util.ArrayList<>();
        }
    }

    public Integer getReviewReplyCount(Long reviewId) {
        try {
            if (reviewReplyMapper == null || reviewId == null) {
                return 0;
            }

            Integer count = reviewReplyMapper.countByReviewId(reviewId);
            return count != null ? count : 0;
        } catch (Exception e) {
            logger.error("获取回复数失败: " + e.getMessage(), e);
            return 0;
        }
    }
}
