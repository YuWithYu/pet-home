package com.pethome.service.user.support;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.pethome.entity.PointsRecord;
import com.pethome.entity.User;
import com.pethome.service.order.PointsRecordService;
import com.pethome.service.user.UserService;
import com.pethome.util.JSONUtil;
import com.pethome.util.WechatUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

/**
 * 登录（账号/手机号/微信）相关逻辑，从 UserServiceImpl 抽取而来。
 */
@Component
public class UserWechatAuthService {

    private static final Logger logger = LoggerFactory.getLogger(UserWechatAuthService.class);

    @Autowired
    private com.pethome.mapper.UserMapper mpUserMapper;

    @Autowired(required = false)
    private PointsRecordService pointsRecordService;

    @Autowired(required = false)
    private WechatUtil wechatUtil;

    @Autowired
    private UserQueryService userQueryService;

    @Autowired
    private UserCredentialService userCredentialService;

    @Autowired
    private UserTokenCacheService userTokenCacheService;

    /**
     * 延迟注入，避免与 UserServiceImpl 形成构造期循环依赖。
     * 仅用于微信注册奖励后刷新魅力值/等级（原逻辑为调用自身公开方法）。
     */
    @Autowired(required = false)
    @Lazy
    private UserService userService;

    public String login(String username, String password) {
        User user = userQueryService.getUserByUsername(username);
        if (user == null) {
            logger.warn("登录失败：用户不存在，username: {}", username);
            return null;
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            logger.warn("登录失败：账号未激活，username: {}", username);
            return null;
        }
        if (!userCredentialService.checkPasswordAndUpgrade(password, user)) {
            logger.warn("登录失败：密码错误，username: {}", username);
            return null;
        }
        String token = UUID.randomUUID().toString().replace("-", "");
        userTokenCacheService.cacheLoginToken(token, user);
        logger.info("用户登录成功，userId: {}, username: {}", user.getId(), username);
        return token;
    }

    public String loginByPhone(String phone, String password) {
        User user = userQueryService.getUserByPhone(phone);
        if (user == null) {
            logger.warn("登录失败：用户不存在，phone: {}", phone);
            return null;
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            logger.warn("登录失败：账号未激活，phone: {}", phone);
            return null;
        }
        if (!userCredentialService.checkPasswordAndUpgrade(password, user)) {
            logger.warn("登录失败：密码错误，phone: {}", phone);
            return null;
        }
        String token = UUID.randomUUID().toString().replace("-", "");
        userTokenCacheService.cacheLoginToken(token, user);
        logger.info("用户登录成功，userId: {}, phone: {}", user.getId(), phone);
        return token;
    }

    public User getUserByOpenid(String openid) {
        if (openid == null || openid.trim().isEmpty()) {
            return null;
        }

        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.eq("openid", openid).last("limit 1");
        return mpUserMapper.selectOne(qw);
    }

    public String loginByWechat(String code) {
        if (code == null || code.trim().isEmpty()) {
            String errorMsg = "微信登录失败：code不能为空";
            logger.warn(errorMsg);
            throw new RuntimeException(errorMsg);
        }

        if (wechatUtil == null) {
            String errorMsg = "微信工具类未注入，无法进行微信登录。请检查WechatUtil是否正确配置为Spring Bean（需要@Component注解）";
            logger.error(errorMsg);
            throw new RuntimeException(errorMsg);
        }

        logger.info("开始微信登录流程，code: {}", code.substring(0, Math.min(10, code.length())) + "...");

        try {
            // 1. 通过code获取openid和session_key
            logger.info("调用微信API获取openid...");
            JsonNode wechatResult;
            try {
                wechatResult = wechatUtil.getOpenidByCode(code);
            } catch (RuntimeException e) {
                // WechatUtil已经抛出了详细的异常信息，直接重新抛出
                throw e;
            } catch (Exception e) {
                logger.error("调用微信API获取openid时发生异常", e);
                throw new RuntimeException("调用微信API失败: " + e.getMessage(), e);
            }

            if (wechatResult == null) {
                logger.error("微信登录失败：无法获取openid，code: {}。可能原因：1) 微信配置错误(app-id或secret) 2) code已过期 3) 网络问题", code);
                throw new RuntimeException("无法获取openid，请检查微信配置和网络连接");
            }

            logger.info("成功获取微信API响应，包含字段: {}", wechatResult.fieldNames());

            String openid = JSONUtil.getText(wechatResult, "openid");
            // String sessionKey = JSONUtil.getText(wechatResult, "session_key"); // 暂时不使用，保留以备将来使用
            String unionid = JSONUtil.getText(wechatResult, "unionid"); // 可选

            if (openid == null || openid.isEmpty()) {
                String errorMsg = "微信API返回的openid为空。响应内容: " + wechatResult.toString();
                logger.error("微信登录失败：" + errorMsg);
                throw new RuntimeException(errorMsg);
            }

            logger.info("微信登录获取openid成功，openid: {} (长度: {})", openid, openid.length());

            // 2. 根据openid查询用户是否存在
            User user = getUserByOpenid(openid);

            if (user == null) {
                // 3. 用户不存在，创建新用户
                logger.info("微信用户不存在，创建新用户，openid: {}", openid);
                user = new User();
                user.setOpenid(openid);
                if (unionid != null && !unionid.isEmpty()) {
                    user.setUnionid(unionid);
                }
                String defaultNickname = "微信用户" + openid.substring(0, Math.min(8, openid.length()));
                user.setNickname(defaultNickname);
                // 设置username，使用nickname作为username（username字段可能有唯一约束，但微信用户通常不会冲突）
                user.setUsername("wx_" + openid.substring(0, Math.min(16, openid.length())));
                // 微信登录用户不设置密码，留空；用户可在「修改密码」页直接设置，无需原密码
                user.setPassword("");
                user.setAvatar("/static/images/garfield-default-avatar.png");
                user.setCreateTime(new Date());
                user.setStatus(1); // 激活状态
                user.setRole("user");
                user.setPoints(0);
                user.setMemberLevel(1);
                user.setCharm(0);

                // 生成唯一的email（email字段有唯一约束）
                user.setEmail("wx_" + openid + "@pethome.com");

                // 插入数据库 - 使用MyBatis-Plus的insert方法，可以自动插入所有字段包括openid
                try {
                    logger.info("准备插入微信用户到数据库，openid: {}, username: {}, nickname: {}",
                        openid, user.getUsername(), user.getNickname());

                    // 使用MyBatis-Plus的insert方法，可以自动处理所有字段（包括openid）
                    int insertResult = mpUserMapper.insert(user);
                    logger.info("微信用户插入数据库成功，insertResult: {}, userId: {}", insertResult, user.getId());

                    // MyBatis-Plus的insert会自动设置user.getId()，所以可以直接使用
                    if (user.getId() != null && user.getId() > 0) {
                        logger.info("插入成功，用户ID: {}, openid: {}", user.getId(), user.getOpenid());
                        // 重新查询确保获取完整信息（包括数据库默认值等）
                        User insertedUser = mpUserMapper.selectById(user.getId());
                        if (insertedUser != null) {
                            user = insertedUser;
                            logger.info("重新查询用户成功，userId: {}, openid: {}", user.getId(), user.getOpenid());
                        } else {
                            logger.warn("插入后通过ID查询不到用户，userId: {}", user.getId());
                            // 尝试通过openid查询
                            user = getUserByOpenid(openid);
                            if (user == null) {
                                throw new RuntimeException("插入用户成功但查询不到，userId: " + user.getId() + ", openid: " + openid);
                            }
                        }
                    } else {
                        logger.warn("插入后user对象没有ID，通过openid查询");
                        // 插入后没有ID，通过openid查询
                        user = getUserByOpenid(openid);
                        if (user == null) {
                            throw new RuntimeException("插入用户后查询不到，openid: " + openid);
                        }
                    }
                } catch (Exception e) {
                    logger.error("插入微信用户到数据库失败，openid: {}, 错误: {}", openid, e.getMessage(), e);
                    // 检查是否是字段不存在错误
                    String errorMsg = e.getMessage();
                    if (errorMsg != null && (errorMsg.contains("openid") || errorMsg.contains("Unknown column"))) {
                        errorMsg = "数据库中没有openid字段，请执行迁移SQL: database/add_wechat_fields_to_user.sql。原始错误: " + errorMsg;
                        logger.error(errorMsg);
                        throw new RuntimeException(errorMsg);
                    }
                    throw new RuntimeException("插入用户到数据库失败: " + errorMsg, e);
                }

                // 最终验证用户是否存在
                if (user == null || user.getId() == null) {
                    String errorMsg = "创建微信用户失败，插入后查询不到用户，openid: " + openid;
                    logger.error(errorMsg);
                    throw new RuntimeException(errorMsg);
                }

                logger.info("微信用户创建成功，userId: {}, openid: {}", user.getId(), openid);

                logger.info("创建微信用户成功，userId: {}, openid: {}", user.getId(), openid);

                // 注册奖励：新手任务: 注册账号 +120g
                if (pointsRecordService != null) {
                    try {
                        // 更新用户积分
                        user.setPoints(120);
                        mpUserMapper.updateById(user);

                        // 创建积分记录
                        PointsRecord pointsRecord = new PointsRecord();
                        pointsRecord.setUserId(user.getId());
                        pointsRecord.setType("earn");
                        pointsRecord.setPoints(120);
                        pointsRecord.setDescription("新用户注册（微信）");
                        pointsRecord.setSource("wechat_register");
                        pointsRecord.setCreateTime(java.time.LocalDateTime.now());
                        pointsRecordService.addRecord(pointsRecord);
                        logger.info("创建微信注册积分记录成功，用户ID: {}, 积分: 120g", user.getId());

                        // 更新用户的魅力值和等级
                        if (userService != null) {
                            userService.updateCharmAndLevel(user.getId());
                        }
                    } catch (Exception e) {
                        logger.error("创建微信注册积分记录失败", e);
                    }
                }
            } else {
                logger.info("微信用户已存在，直接登录，userId: {}, openid: {}", user.getId(), openid);

                // 账号被禁用则不允许登录
                if (user.getStatus() == null || user.getStatus() != 1) {
                    logger.warn("微信用户账号已被封禁，拒绝登录，userId: {}, status: {}", user.getId(), user.getStatus());
                    throw new RuntimeException("该账号已被封禁");
                }

                // 更新unionid（如果之前没有）
                if (unionid != null && !unionid.isEmpty() && (user.getUnionid() == null || user.getUnionid().isEmpty())) {
                    user.setUnionid(unionid);
                    mpUserMapper.updateById(user);
                }
            }

            // 4. 生成token
            String token = "token_" + user.getId() + "_" + System.currentTimeMillis();

            // 5. 缓存token和用户信息
            userTokenCacheService.cacheLoginToken(token, user);

            logger.info("微信登录成功，userId: {}, openid: {}, token: {}", user.getId(), openid, token.substring(0, Math.min(20, token.length())) + "...");

            return token;

        } catch (RuntimeException e) {
            // 重新抛出RuntimeException，保留详细错误信息
            throw e;
        } catch (Exception e) {
            String errorMsg = "微信登录过程中发生未预期的异常: " + e.getMessage();
            logger.error("微信登录失败，code: {}", code, e);
            throw new RuntimeException(errorMsg, e);
        }
    }
}
