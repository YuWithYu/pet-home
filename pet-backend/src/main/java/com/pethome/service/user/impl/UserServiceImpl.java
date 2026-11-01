package com.pethome.service.user.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.PointsRecord;
import com.pethome.entity.User;
import com.pethome.service.order.PointsRecordService;
import com.pethome.service.user.UserService;
import com.pethome.service.user.support.UserCredentialService;
import com.pethome.service.user.support.UserQueryService;
import com.pethome.service.user.support.UserTokenCacheService;
import com.pethome.service.user.support.UserWechatAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private com.pethome.mapper.UserMapper mpUserMapper;

    @Autowired(required = false)
    private PointsRecordService pointsRecordService;

    @Autowired(required = false)
    private UserCascadeDeletionService cascadeDeletionService;

    @Autowired
    private UserQueryService userQueryService;

    @Autowired
    private UserCredentialService userCredentialService;

    @Autowired
    private UserTokenCacheService userTokenCacheService;

    @Autowired
    private UserWechatAuthService userWechatAuthService;

    @Override
    public User register(User user) {
        // 检查昵称是否已被使用
        if (user.getNickname() != null && !user.getNickname().trim().isEmpty()) {
            if (userQueryService.isNicknameExists(user.getNickname(), null)) {
                throw new IllegalArgumentException("昵称已被使用，请选择其他昵称");
            }
        }
        
        // 确保设置创建时间和状态
        if (user.getCreateTime() == null) {
            user.setCreateTime(new Date());
        }
        if (user.getStatus() == null) {
            user.setStatus(1);  // 默认激活状态
        }
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            // 如果 email 为空，使用 phone 或 username 生成唯一 email（email 字段有唯一约束）
            String emailBase = user.getPhone() != null && !user.getPhone().isEmpty() 
                ? user.getPhone() 
                : (user.getUsername() != null ? user.getUsername() : "user");
            user.setEmail(emailBase + "@pethome.com");
        }
        mpUserMapper.insertUser(user);
        return mpUserMapper.selectByUsername(user.getUsername());
    }

    @Override
    public String register(String phone, String password, String nickname) {
        // 检查手机号是否已被注册
        User existingUser = userQueryService.getUserByPhone(phone);
        if (existingUser != null) {
            throw new IllegalArgumentException("该手机号已被注册，请使用其他手机号");
        }
        
        // 检查昵称是否已被使用
        if (nickname != null && !nickname.trim().isEmpty()) {
            if (userQueryService.isNicknameExists(nickname, null)) {
                throw new IllegalArgumentException("昵称已被使用，请选择其他昵称");
            }
        }
        
        // Simplified registration: create user entity and persist
        User user = new User();
        user.setPhone(phone);
        // 不设置username，用户不需要这个字段
        // user.setUsername(null);
        // 密码使用 BCrypt 加密（含盐，防御彩虹表）
        user.setPassword(userCredentialService.encodePassword(password));
        user.setNickname(nickname);
        user.setCreateTime(new Date());  // 设置创建时间
        user.setStatus(1);  // 设置状态为激活（1=激活，0=未激活）
        // 设置默认头像为登录页面的狗图片
        user.setAvatar("/static/images/login-dog.png");
        // 不设置email，用户不需要这个功能
        // user.setEmail(null);
        mpUserMapper.insertUser(user);

        // 注册奖励：新手任务: 注册账号 +120g
        if (user.getId() != null && pointsRecordService != null) {
            try {
                // 更新用户积分
                user.setPoints(120);
                mpUserMapper.updateById(user);

                // 创建积分记录
                PointsRecord pointsRecord = new PointsRecord();
                pointsRecord.setUserId(user.getId());
                pointsRecord.setType("earn");
                pointsRecord.setPoints(120);
                pointsRecord.setDescription("新用户注册");
                pointsRecord.setSource("register");
                pointsRecord.setCreateTime(java.time.LocalDateTime.now());
                pointsRecordService.addRecord(pointsRecord);
                logger.info("创建注册积分记录成功，用户ID: {}, 积分: 120g", user.getId());

                // 更新用户的魅力值和等级
                updateCharmAndLevel(user.getId());
            } catch (Exception e) {
                // 积分发放失败不阻断注册主流程，但需记录完整异常以便排查
                logger.error("创建注册积分记录失败, userId={}", user.getId(), e);
            }
        }

        // 生成不可预测的随机 token 并写入 Redis，避免可伪造登录态
        String token = java.util.UUID.randomUUID().toString().replace("-", "");
        userTokenCacheService.cacheLoginToken(token, user);
        return token;
    }

    @Override
    public boolean updateUser(User user) {
        // 如果更新了昵称，检查昵称是否已被其他用户使用
        if (user.getNickname() != null && !user.getNickname().trim().isEmpty() && user.getId() != null) {
            if (userQueryService.isNicknameExists(user.getNickname(), user.getId())) {
                throw new IllegalArgumentException("昵称已被使用，请选择其他昵称");
            }
        }
        
        // 调试：打印要更新的字段
        logger.info("========== 更新用户信息 ==========");
        logger.info("用户ID: {}", user.getId());
        logger.info("头像(avatar): {}", user.getAvatar());
        logger.info("背景图(backgroundImage): {}", user.getBackgroundImage());
        logger.info("昵称(nickname): {}", user.getNickname());
        logger.info("====================================");
        
        boolean result = mpUserMapper.updateById(user) > 0;
        if (result && user != null && user.getId() != null) {
            // 更新成功后清除用户信息缓存
            userTokenCacheService.clearUserCache(user.getId());
            logger.info("用户信息更新成功，已清除缓存，userId: {}", user.getId());
            
            // 验证更新结果
            User updatedUser = mpUserMapper.selectById(user.getId());
            if (updatedUser != null) {
                logger.info("更新后验证 - 头像: {}, 背景图: {}", updatedUser.getAvatar(), updatedUser.getBackgroundImage());
            }
        }
        return result;
    }
    
    @Override
    public void updateCharmAndLevel(Long userId) {
        if (userId == null || pointsRecordService == null) {
            return;
        }
        
        try {
            // 计算魅力值（所有历史积分累计总和）
            Integer charm = pointsRecordService.calculateCharm(userId);
            
            // 计算等级（每1000魅力值 = 1级）
            Integer level = (charm != null && charm > 0) ? (charm / 1000) : 0;
            
            // 更新用户信息
            User user = mpUserMapper.selectById(userId);
            if (user != null) {
                user.setCharm(charm != null ? charm : 0);
                user.setMemberLevel(level);
                mpUserMapper.updateById(user);
                
                // 清除用户缓存
                userTokenCacheService.clearUserCache(userId);
                
                logger.info("更新用户魅力值和等级成功，userId: {}, charm: {}, level: {}", userId, charm, level);
            }
        } catch (Exception e) {
            logger.error("更新用户魅力值和等级失败，userId: {}, 错误: {}", userId, e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUser(Long id) {
        if (id == null) {
            return false;
        }
        if (cascadeDeletionService != null) {
            cascadeDeletionService.deleteAllDataForUser(id);
        }
        return mpUserMapper.deleteById(id) > 0;
    }

    // ==================== 登录（委托 UserWechatAuthService） ====================

    @Override
    public String login(String username, String password) {
        return userWechatAuthService.login(username, password);
    }

    @Override
    public String loginByPhone(String phone, String password) {
        return userWechatAuthService.loginByPhone(phone, password);
    }

    @Override
    public String loginByWechat(String code) {
        return userWechatAuthService.loginByWechat(code);
    }

    @Override
    public User getUserByOpenid(String openid) {
        return userWechatAuthService.getUserByOpenid(openid);
    }

    // ==================== 凭据（委托 UserCredentialService） ====================

    @Override
    public boolean verifyPassword(String rawPassword, User user) {
        return userCredentialService.verifyPassword(rawPassword, user);
    }

    @Override
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        return userCredentialService.changePassword(userId, oldPassword, newPassword);
    }

    @Override
    public boolean resetPasswordByPhone(String phone, String newPassword) {
        return userCredentialService.resetPasswordByPhone(phone, newPassword);
    }

    @Override
    public boolean unbindPhoneByUserId(Long userId) {
        return userCredentialService.unbindPhoneByUserId(userId);
    }

    @Override
    public String changeUsername(Long userId, String newUsername) {
        return userCredentialService.changeUsername(userId, newUsername);
    }

    // ==================== 缓存（委托 UserTokenCacheService） ====================

    @Override
    public void cacheLoginToken(String token, User user) {
        userTokenCacheService.cacheLoginToken(token, user);
    }

    @Override
    public User getUserByToken(String token) {
        return userTokenCacheService.getUserByToken(token);
    }

    @Override
    public void clearUserCache(Long userId) {
        userTokenCacheService.clearUserCache(userId);
    }

    // ==================== 查询统计（委托 UserQueryService） ====================

    @Override
    public User getUserById(Long id) {
        return userQueryService.getUserById(id);
    }

    @Override
    public User getUserByUsername(String username) {
        return userQueryService.getUserByUsername(username);
    }

    @Override
    public User getUserByPhone(String phone) {
        return userQueryService.getUserByPhone(phone);
    }

    @Override
    public User getUserByNickname(String nickname) {
        return userQueryService.getUserByNickname(nickname);
    }

    @Override
    public User getUserByEmail(String email) {
        return userQueryService.getUserByEmail(email);
    }

    @Override
    public List<User> getUsersByIds(Collection<Long> ids) {
        return userQueryService.getUsersByIds(ids);
    }

    @Override
    public List<User> getRecentUsers(Long excludeUserId, boolean onlyActive, int limit) {
        return userQueryService.getRecentUsers(excludeUserId, onlyActive, limit);
    }

    @Override
    public IPage<User> getUserList(Page<User> page) {
        return userQueryService.getUserList(page);
    }

    @Override
    public IPage<User> getUserList(Page<User> page, String keyword) {
        return userQueryService.getUserList(page, keyword);
    }

    @Override
    public Map<String, Integer> getUserStats(Long userId) {
        return userQueryService.getUserStats(userId);
    }

    @Override
    public String getNicknameSafely(Long userId) {
        return userQueryService.getNicknameSafely(userId);
    }

    @Override
    public boolean isNicknameExists(String nickname, Long excludeUserId) {
        return userQueryService.isNicknameExists(nickname, excludeUserId);
    }
}
