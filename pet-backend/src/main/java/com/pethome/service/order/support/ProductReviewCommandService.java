package com.pethome.service.order.support;

import com.pethome.entity.ProductReview;
import com.pethome.mapper.ProductReviewMapper;
import com.pethome.util.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品评价写入：创建评价、切换点赞、创建回复。
 */
@Component
public class ProductReviewCommandService {

    private static final Logger logger = LoggerFactory.getLogger(ProductReviewCommandService.class);

    @Autowired
    private ProductReviewMapper productReviewMapper;

    @Autowired
    private com.pethome.mapper.UserMapper userMapper;

    @Autowired(required = false)
    private com.pethome.mapper.ReviewLikeMapper reviewLikeMapper;

    @Autowired(required = false)
    private com.pethome.mapper.ReviewReplyMapper reviewReplyMapper;

    public ProductReview createProductReview(ProductReview review) {
        try {
            logger.info("开始创建评价 - 商品ID: {}, 用户ID: {}, 评分: {}",
                review.getProductId(), review.getUserId(), review.getRating());

            // 验证必填字段
            if (review.getProductId() == null) {
                throw new IllegalArgumentException("商品ID不能为空");
            }
            if (review.getUserId() == null) {
                throw new IllegalArgumentException("用户ID不能为空");
            }
            if (review.getRating() == null || review.getRating() < 1 || review.getRating() > 5) {
                throw new IllegalArgumentException("评分必须在1-5之间");
            }
            if (review.getComment() == null || review.getComment().trim().isEmpty()) {
                throw new IllegalArgumentException("评价内容不能为空");
            }

            // 设置创建时间和更新时间
            LocalDateTime now = LocalDateTime.now();
            review.setCreateTime(now);
            review.setUpdateTime(now);

            // 如果状态为空，设置为正常状态
            if (review.getStatus() == null) {
                review.setStatus(1);
            }

            // 处理图片字段：如果是数组，转换为逗号分隔的字符串，并过滤临时路径
            if (review.getImages() != null && !review.getImages().trim().isEmpty()) {
                String imagesStr = review.getImages().trim();
                List<String> imageList = null;

                if (imagesStr.startsWith("[")) {
                    try {
                        // 如果是JSON数组格式，解析后转换为逗号分隔
                        imageList = JSONUtil.parseArray(imagesStr, String.class);
                        logger.info("解析JSON数组格式图片，数量: {}", imageList != null ? imageList.size() : 0);
                    } catch (Exception e) {
                        // 如果解析失败，尝试按逗号分隔
                        logger.warn("解析评价图片JSON失败，尝试按逗号分隔: " + e.getMessage());
                        imageList = java.util.Arrays.asList(imagesStr.split(","));
                    }
                } else {
                    // 如果是逗号分隔的字符串，按逗号拆分
                    imageList = java.util.Arrays.asList(imagesStr.split(","));
                    logger.info("按逗号分隔图片字符串，原始字符串: {}, 拆分后数量: {}", imagesStr, imageList.size());
                }

                // 过滤临时路径和空值
                if (imageList != null && !imageList.isEmpty()) {
                    imageList = imageList.stream()
                        .filter(img -> img != null && !img.trim().isEmpty()
                            && !img.trim().startsWith("http://tmp/")
                            && !img.trim().startsWith("https://tmp/")
                            && !img.trim().startsWith("tmp/"))
                        .map(img -> img.trim()) // 去除空格
                        .collect(java.util.stream.Collectors.toList());

                    logger.info("过滤临时路径后图片数量: {}", imageList.size());

                    if (!imageList.isEmpty()) {
                        String filteredImages = String.join(",", imageList);
                        review.setImages(filteredImages);
                        logger.info("最终保存的图片字段: {}", filteredImages);
                    } else {
                        review.setImages(null);
                        logger.info("所有图片都是临时路径，设置为null");
                    }
                } else {
                    review.setImages(null);
                    logger.info("图片列表为空，设置为null");
                }
            } else {
                review.setImages(null);
                logger.info("图片字段为空或null，设置为null");
            }

            if (review.getVideos() != null && !review.getVideos().trim().isEmpty()) {
                String vStr = review.getVideos().trim();
                java.util.List<String> vList = java.util.Arrays.asList(vStr.split(","));
                vList = vList.stream()
                    .filter(v -> v != null && !v.trim().isEmpty()
                        && !v.trim().startsWith("http://tmp/")
                        && !v.trim().startsWith("https://tmp/")
                        && !v.trim().startsWith("tmp/"))
                    .map(String::trim)
                    .collect(java.util.stream.Collectors.toList());
                review.setVideos(vList.isEmpty() ? null : String.join(",", vList));
            } else {
                review.setVideos(null);
            }

            logger.info("准备插入评价 - 商品ID: {}, 用户ID: {}, 评分: {}, 内容: {}, 图片: {}, 视频: {}",
                review.getProductId(), review.getUserId(), review.getRating(),
                review.getComment(), review.getImages(), review.getVideos());

            // 插入评价到数据库
            int result = productReviewMapper.insert(review);

            logger.info("插入评价结果 - 返回值: {}, 评价ID: {}", result, review.getId());

            if (result > 0) {
                logger.info("创建评价成功 - 评价ID: {}, 商品ID: {}, 用户ID: {}",
                    review.getId(), review.getProductId(), review.getUserId());

                // 更新商品销量（如果有销量字段，可以通过订单统计，这里先不更新）
                // 注意：如果需要更新销量，可以通过统计已完成订单数量来更新
                // 或者添加sale字段到Product实体，然后在这里更新

                return review;
            } else {
                logger.error("创建评价失败 - 插入数据库返回0");
                throw new RuntimeException("插入评价到数据库失败");
            }
        } catch (IllegalArgumentException e) {
            logger.error("创建评价参数错误: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("创建评价异常: " + e.getMessage(), e);
            throw new RuntimeException("创建评价失败: " + e.getMessage(), e);
        }
    }

    // ========== 评论点赞相关方法 ==========

    public boolean toggleReviewLike(Long reviewId, Long userId) {
        try {
            if (reviewLikeMapper == null) {
                logger.warn("ReviewLikeMapper未注入，无法执行点赞操作");
                return false;
            }

            if (reviewId == null || userId == null) {
                throw new IllegalArgumentException("评论ID和用户ID不能为空");
            }

            // 检查是否已点赞
            com.pethome.entity.ReviewLike existingLike = reviewLikeMapper.findByReviewIdAndUserId(reviewId, userId);

            if (existingLike != null) {
                // 已点赞，执行取消点赞
                int result = reviewLikeMapper.deleteById(existingLike.getId());
                logger.info("取消点赞 - 评论ID: {}, 用户ID: {}, 结果: {}", reviewId, userId, result > 0);
                return false; // 返回false表示已取消点赞
            } else {
                // 未点赞，执行点赞
                com.pethome.entity.ReviewLike newLike = new com.pethome.entity.ReviewLike(reviewId, userId);
                int result = reviewLikeMapper.insert(newLike);
                logger.info("点赞成功 - 评论ID: {}, 用户ID: {}, 结果: {}", reviewId, userId, result > 0);
                return result > 0; // 返回true表示已点赞
            }
        } catch (Exception e) {
            logger.error("切换点赞状态失败: " + e.getMessage(), e);
            throw new RuntimeException("切换点赞状态失败: " + e.getMessage(), e);
        }
    }

    // ========== 评论回复相关方法 ==========

    public com.pethome.entity.ReviewReply createReviewReply(Long reviewId, Long userId, String content) {
        try {
            if (reviewReplyMapper == null) {
                logger.warn("ReviewReplyMapper未注入，无法创建回复");
                throw new RuntimeException("回复功能未启用");
            }

            if (reviewId == null || userId == null) {
                throw new IllegalArgumentException("评论ID和用户ID不能为空");
            }

            if (content == null || content.trim().isEmpty()) {
                throw new IllegalArgumentException("回复内容不能为空");
            }

            // 验证评论是否存在
            ProductReview review = productReviewMapper.selectById(reviewId);
            if (review == null) {
                throw new IllegalArgumentException("评论不存在");
            }

            // 创建回复
            com.pethome.entity.ReviewReply reply = new com.pethome.entity.ReviewReply(reviewId, userId, content.trim());
            int result = reviewReplyMapper.insert(reply);

            if (result > 0) {
                logger.info("创建回复成功 - 回复ID: {}, 评论ID: {}, 用户ID: {}", reply.getId(), reviewId, userId);

                // 填充用户信息
                com.pethome.entity.User user = userMapper.selectById(userId);
                if (user != null) {
                    reply.setUserName(user.getNickname() != null ? user.getNickname() :
                                     (user.getUsername() != null ? user.getUsername() : "用户" + userId));
                    reply.setUserAvatar(user.getAvatar());
                }

                return reply;
            } else {
                throw new RuntimeException("创建回复失败");
            }
        } catch (IllegalArgumentException e) {
            logger.error("创建回复参数错误: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("创建回复异常: " + e.getMessage(), e);
            throw new RuntimeException("创建回复失败: " + e.getMessage(), e);
        }
    }
}
