package com.pethome.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.JsonNode;
import com.pethome.entity.User;
import com.pethome.entity.PointsRecord;
import com.pethome.service.UserService;
import com.pethome.service.RedisCacheService;
import com.pethome.service.PointsRecordService;
import com.pethome.util.JSONUtil;
import com.pethome.util.WechatUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    
    // Redis 缓存 key 前缀
    private static final String LOGIN_TOKEN_PREFIX = "login:token:";
    private static final String USER_INFO_PREFIX = "user:info:";
    // 缓存过期时间
    private static final long LOGIN_TOKEN_EXPIRE_HOURS = 1; // 登录 token 1小时过期
    private static final long USER_INFO_EXPIRE_MINUTES = 30; // 用户信息 30分钟过期

    @Autowired
    private com.pethome.mapper.UserMapper mpUserMapper;
    
    @Autowired
    private RedisCacheService redisCacheService;
    
    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;
    
    @Autowired(required = false)
    private PointsRecordService pointsRecordService;
    
    @Autowired(required = false)
    private WechatUtil wechatUtil;

    @Autowired(required = false)
    private UserCascadeDeletionService cascadeDeletionService;

    /**
     * BCrypt 密码编码器（项目已在 PasswordEncoderConfig 中注册 Bean）
     * 用于新密码加密与登录校验，替代旧的 MD5Encoder。
     * 兼容期：登录时若数据库为旧 MD5 密码，匹配后自动升级为 BCrypt。
     */
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 校验密码：兼容旧 MD5 密码，匹配后自动升级为 BCrypt
     * @return true 表示密码正确
     */
    private boolean checkPasswordAndUpgrade(String rawPassword, User user) {
        if (user == null) return false;
        String dbPwd = user.getPassword();
        if (dbPwd == null || dbPwd.trim().isEmpty()) return false;

        // 1. 优先用 BCrypt 校验（BCrypt 密文以 $2a$/$2b$/$2y$ 开头）
        if (dbPwd.startsWith("$2a$") || dbPwd.startsWith("$2b$") || dbPwd.startsWith("$2y$")) {
            return passwordEncoder.matches(rawPassword, dbPwd);
        }

        // 2. 兼容旧 MD5 密码（不加盐）：MD5 是 32 位十六进制
        if (dbPwd.length() == 32) {
            String md5Input = DigestUtils.md5DigestAsHex(rawPassword.getBytes());
            if (md5Input.equalsIgnoreCase(dbPwd)) {
                // 自动升级为 BCrypt
                try {
                    user.setPassword(passwordEncoder.encode(rawPassword));
                    mpUserMapper.updateById(user);
                    logger.info("用户密码已从 MD5 升级为 BCrypt, userId={}", user.getId());
                } catch (Exception e) {
                    logger.error("密码升级失败, userId={}", user.getId(), e);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 加密密码：统一使用 BCrypt
     */
    private String encodePassword(String rawPassword) {
        if (rawPassword == null) return null;
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * 对外暴露的密码校验入口（供 Controller 调用）。
     * 委托给 {@link #checkPasswordAndUpgrade}，避免 Controller 自行处理加密细节，
     * 也避免再出现 MD5/明文 fallback 等不安全实现。
     */
    @Override
    public boolean verifyPassword(String rawPassword, User user) {
        if (rawPassword == null) return false;
        return checkPasswordAndUpgrade(rawPassword, user);
    }

    @Override
    public User register(User user) {
        // 检查昵称是否已被使用
        if (user.getNickname() != null && !user.getNickname().trim().isEmpty()) {
            if (isNicknameExists(user.getNickname(), null)) {
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
        User existingUser = getUserByPhone(phone);
        if (existingUser != null) {
            throw new IllegalArgumentException("该手机号已被注册，请使用其他手机号");
        }
        
        // 检查昵称是否已被使用
        if (nickname != null && !nickname.trim().isEmpty()) {
            if (isNicknameExists(nickname, null)) {
                throw new IllegalArgumentException("昵称已被使用，请选择其他昵称");
            }
        }
        
        // Simplified registration: create user entity and persist
        User user = new User();
        user.setPhone(phone);
        // 不设置username，用户不需要这个字段
        // user.setUsername(null);
        // 密码使用 BCrypt 加密（含盐，防御彩虹表）
        user.setPassword(encodePassword(password));
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
        String token = UUID.randomUUID().toString().replace("-", "");
        cacheLoginToken(token, user);
        return token;
    }

    @Override
    public String login(String username, String password) {
        User user = getUserByUsername(username);
        if (user == null) {
            logger.warn("登录失败：用户不存在，username: {}", username);
            return null;
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            logger.warn("登录失败：账号未激活，username: {}", username);
            return null;
        }
        if (!checkPasswordAndUpgrade(password, user)) {
            logger.warn("登录失败：密码错误，username: {}", username);
            return null;
        }
        String token = UUID.randomUUID().toString().replace("-", "");
        cacheLoginToken(token, user);
        logger.info("用户登录成功，userId: {}, username: {}", user.getId(), username);
        return token;
    }

    @Override
    public String loginByPhone(String phone, String password) {
        User user = getUserByPhone(phone);
        if (user == null) {
            logger.warn("登录失败：用户不存在，phone: {}", phone);
            return null;
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            logger.warn("登录失败：账号未激活，phone: {}", phone);
            return null;
        }
        if (!checkPasswordAndUpgrade(password, user)) {
            logger.warn("登录失败：密码错误，phone: {}", phone);
            return null;
        }
        String token = UUID.randomUUID().toString().replace("-", "");
        cacheLoginToken(token, user);
        logger.info("用户登录成功，userId: {}, phone: {}", user.getId(), phone);
        return token;
    }
    
    @Override
    public void cacheLoginToken(String token, User user) {
        if (stringRedisTemplate == null || token == null || user == null) {
            logger.warn("缓存登录 token 失败：参数为空或 StringRedisTemplate 未注入");
            return;
        }
        
        try {
            String loginKey = LOGIN_TOKEN_PREFIX + token;
            String userInfoKey = USER_INFO_PREFIX + user.getId();
            
            // 缓存登录 token -> 用户信息
            String userJson = JSONUtil.toJSONString(user);
            stringRedisTemplate.opsForValue().set(loginKey, userJson, LOGIN_TOKEN_EXPIRE_HOURS, TimeUnit.HOURS);
            logger.info("成功缓存登录 token，key: {}, userId: {}, 过期时间: {} 小时", loginKey, user.getId(), LOGIN_TOKEN_EXPIRE_HOURS);
            
            // 同时缓存用户信息（用于快速查询）
            stringRedisTemplate.opsForValue().set(userInfoKey, userJson, USER_INFO_EXPIRE_MINUTES, TimeUnit.MINUTES);
            logger.info("成功缓存用户信息，key: {}, userId: {}, 过期时间: {} 分钟", userInfoKey, user.getId(), USER_INFO_EXPIRE_MINUTES);
            
        } catch (Exception e) {
            logger.error("缓存登录 token 失败，userId: {}, 错误: {}", user.getId(), e.getMessage(), e);
        }
    }
    
    @Override
    public User getUserByToken(String token) {
        if (stringRedisTemplate == null) {
            logger.warn("getUserByToken - StringRedisTemplate 未注入，无法从缓存获取用户信息");
            return null;
        }

        if (!StringUtils.hasText(token)) {
            logger.debug("getUserByToken - token 为空");
            return null;
        }

        try {
            String loginKey = LOGIN_TOKEN_PREFIX + token;
            String userJson = stringRedisTemplate.opsForValue().get(loginKey);

            if (StringUtils.hasText(userJson)) {
                User cachedUser = JSONUtil.parseObject(userJson, User.class);
                logger.debug("getUserByToken - 命中缓存, userId={}", cachedUser != null ? cachedUser.getId() : null);

                // 关键修复：每次从 token 获取用户时都实时校验账号状态，避免“后台禁用后旧 token 仍可用”
                if (cachedUser == null || cachedUser.getId() == null) {
                    stringRedisTemplate.delete(loginKey);
                    return null;
                }
                User latestUser = mpUserMapper.selectById(cachedUser.getId());
                if (latestUser == null || latestUser.getStatus() == null || latestUser.getStatus() != 1) {
                    String userInfoKey = USER_INFO_PREFIX + cachedUser.getId();
                    stringRedisTemplate.delete(loginKey);
                    stringRedisTemplate.delete(userInfoKey);
                    logger.warn("检测到账号已禁用或不存在，令牌失效，userId: {}, status: {}",
                            cachedUser.getId(), latestUser != null ? latestUser.getStatus() : null);
                    return null;
                }

                // 续期 token，并返回最新用户信息（避免使用缓存中的旧状态）
                stringRedisTemplate.expire(loginKey, LOGIN_TOKEN_EXPIRE_HOURS, TimeUnit.HOURS);
                return latestUser;
            }

            logger.debug("getUserByToken - token 无效或已过期");
            return null;
        } catch (Exception e) {
            // 不打印 token 内容，避免敏感信息泄露
            logger.error("getUserByToken - 从缓存获取用户信息失败", e);
            return null;
        }
    }
    
    @Override
    public void clearUserCache(Long userId) {
        if (stringRedisTemplate == null || userId == null) {
            return;
        }
        
        try {
            String userInfoKey = USER_INFO_PREFIX + userId;
            Boolean deleted = stringRedisTemplate.delete(userInfoKey);
            if (Boolean.TRUE.equals(deleted)) {
                logger.info("成功清除用户信息缓存，userId: {}, key: {}", userId, userInfoKey);
            } else {
                logger.info("用户信息缓存不存在，userId: {}, key: {}", userId, userInfoKey);
            }
        } catch (Exception e) {
            logger.error("清除用户信息缓存失败，userId: {}, 错误: {}", userId, e.getMessage(), e);
        }
    }

    @Override
    public User getUserById(Long id) {
        if (id == null) {
            return null;
        }
        
        // 优先从 Redis 缓存获取
        if (stringRedisTemplate != null) {
            try {
                String userInfoKey = USER_INFO_PREFIX + id;
                String userJson = stringRedisTemplate.opsForValue().get(userInfoKey);
                if (StringUtils.hasText(userJson)) {
                    logger.info("从 Redis 缓存获取用户信息，userId: {}, key: {}", id, userInfoKey);
                    return JSONUtil.parseObject(userJson, User.class);
                }
            } catch (Exception e) {
                logger.warn("从 Redis 获取用户信息失败，降级到数据库查询，userId: {}, 错误: {}", id, e.getMessage());
            }
        }
        
        // 从数据库查询
        User user = mpUserMapper.selectById(id);
        
        if (user == null) {
            logger.warn("数据库中未找到用户，userId: {}", id);
        } else {
            logger.info("从数据库查询到用户，userId: {}, username: {}, nickname: {}, status: {}", 
                id, user.getUsername(), user.getNickname(), user.getStatus());
        }
        
        // 写入 Redis 缓存
        if (user != null && stringRedisTemplate != null) {
            try {
                String userInfoKey = USER_INFO_PREFIX + id;
                String userJson = JSONUtil.toJSONString(user);
                stringRedisTemplate.opsForValue().set(userInfoKey, userJson, USER_INFO_EXPIRE_MINUTES, TimeUnit.MINUTES);
                logger.info("成功写入用户信息到 Redis 缓存，userId: {}, key: {}, 过期时间: {} 分钟", id, userInfoKey, USER_INFO_EXPIRE_MINUTES);
            } catch (Exception e) {
                logger.error("写入用户信息到 Redis 缓存失败，userId: {}, 错误: {}", id, e.getMessage(), e);
            }
        }
        
        return user;
    }

    @Override
    public User getUserByUsername(String username) {
        // 使用MyBatis-Plus的查询，自动包含所有字段
        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.eq("username", username).last("limit 1");
        return mpUserMapper.selectOne(qw);
    }

    @Override
    public User getUserByPhone(String phone) {
        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.eq("phone", phone).last("limit 1");
        return mpUserMapper.selectOne(qw);
    }

    @Override
    public User getUserByNickname(String nickname) {
        if (nickname == null || nickname.trim().isEmpty()) {
            return null;
        }
        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.eq("nickname", nickname.trim()).last("limit 1");
        return mpUserMapper.selectOne(qw);
    }

    @Override
    public IPage<User> getUserList(Page<User> page) {
        return mpUserMapper.selectPage(page, null);
    }

    @Override
    public IPage<User> getUserList(Page<User> page, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return mpUserMapper.selectPage(page, null);
        }
        String kw = keyword.trim();
        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.and(w -> w.like("nickname", kw)
                .or().like("phone", kw)
                .or().like("username", kw));
        return mpUserMapper.selectPage(page, qw);
    }

    @Override
    public boolean updateUser(User user) {
        // 如果更新了昵称，检查昵称是否已被其他用户使用
        if (user.getNickname() != null && !user.getNickname().trim().isEmpty() && user.getId() != null) {
            if (isNicknameExists(user.getNickname(), user.getId())) {
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
            clearUserCache(user.getId());
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
                clearUserCache(userId);
                
                logger.info("更新用户魅力值和等级成功，userId: {}, charm: {}, level: {}", userId, charm, level);
            }
        } catch (Exception e) {
            logger.error("更新用户魅力值和等级失败，userId: {}, 错误: {}", userId, e.getMessage(), e);
        }
    }
    
    @Override
    public boolean isNicknameExists(String nickname, Long excludeUserId) {
        if (nickname == null || nickname.trim().isEmpty()) {
            return false; // 空昵称不检查
        }
        Integer count = mpUserMapper.checkNicknameExists(nickname.trim(), excludeUserId);
        return count != null && count > 0;
    }

    @Override
    public boolean resetPasswordByPhone(String phone, String newPassword) {
        if (phone == null || phone.trim().isEmpty() || newPassword == null || newPassword.trim().isEmpty()) {
            return false;
        }
        String p = phone.trim();
        long count = mpUserMapper.selectCount(new QueryWrapper<User>().eq("phone", p));
        if (count > 1) {
            throw new IllegalArgumentException("该手机号已绑定多个账号，无法通过忘记密码重置。请使用当前登录账号在「修改密码」中用原密码修改，或联系客服处理。");
        }
        User user = getUserByPhone(p);
        if (user == null) {
            logger.warn("忘记密码：手机号未注册，phone: {}", phone);
            return false;
        }
        if (newPassword.length() < 6) {
            throw new IllegalArgumentException("密码长度不能少于6位");
        }
        user.setPassword(encodePassword(newPassword.trim()));
        boolean ok = mpUserMapper.updateById(user) > 0;
        if (ok && user.getId() != null) {
            clearUserCache(user.getId());
        }
        return ok;
    }

    @Override
    public boolean unbindPhoneByUserId(Long userId) {
        if (userId == null) return false;
        UpdateWrapper<User> uw = new UpdateWrapper<>();
        uw.eq("id", userId).set("phone", null);
        int rows = mpUserMapper.update(null, uw);
        if (rows > 0) {
            clearUserCache(userId);
            return true;
        }
        return false;
    }

    private static final java.util.regex.Pattern USERNAME_PATTERN = java.util.regex.Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_-]{5,19}$");

    @Override
    public String changeUsername(Long userId, String newUsername) {
        if (userId == null) return "请先登录";
        if (newUsername == null || (newUsername = newUsername.trim()).isEmpty()) return "账号不能为空";
        if (!USERNAME_PATTERN.matcher(newUsername).matches()) {
            return "账号须以字母或下划线开头，长度6-20位，仅可使用字母、数字、下划线、减号";
        }
        User other = getUserByUsername(newUsername);
        if (other != null && !other.getId().equals(userId)) return "该账号已被占用";
        User user = getUserById(userId);
        if (user == null) return "用户不存在";
        user.setUsername(newUsername);
        user.setUsernameUpdatedAt(new java.util.Date());
        boolean ok = mpUserMapper.updateById(user) > 0;
        if (ok) clearUserCache(userId);
        return ok ? null : "修改失败";
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

    @Override
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        if (userId == null || newPassword == null) return false;
        if (newPassword.trim().length() < 6) {
            throw new IllegalArgumentException("新密码长度不能少于6位");
        }
        User user = mpUserMapper.selectById(userId);
        if (user == null) return false;
        String dbPwd = user.getPassword();
        boolean noPassword = (dbPwd == null || dbPwd.trim().isEmpty());
        // 微信用户：即使历史遗留了随机密码（用户不知），也允许首次设置时原密码可空
        boolean isWechatUser = (user.getOpenid() != null && !user.getOpenid().trim().isEmpty());
        boolean canSetWithoutOld = noPassword || (isWechatUser && (oldPassword == null || oldPassword.trim().isEmpty()));
        if (canSetWithoutOld) {
            // 微信等第三方登录用户无密码，或微信用户首次设置（历史随机密码用户不知），可直接设置新密码
            user.setPassword(encodePassword(newPassword.trim()));
        } else {
            if (oldPassword == null || oldPassword.trim().isEmpty()) {
                throw new IllegalArgumentException("该账号已设置过密码，请输入原密码。若忘记请使用「忘记原密码」通过手机验证码重置。");
            }
            // 校验原密码：兼容旧 MD5 密码，匹配后自动升级为 BCrypt
            // 不再允许明文密码对比，杜绝 dbPwd 为明文也能通过校验的旁路
            boolean match = checkPasswordAndUpgrade(oldPassword.trim(), user);
            if (!match) {
                throw new IllegalArgumentException("原密码错误");
            }
            user.setPassword(encodePassword(newPassword.trim()));
        }
        boolean ok = mpUserMapper.updateById(user) > 0;
        if (ok) clearUserCache(userId);
        return ok;
    }
    
    @Override
    public Map<String, Integer> getUserStats(Long userId) {
        Map<String, Integer> cachedStats = redisCacheService.getUserStats(userId);
        if (cachedStats != null && !cachedStats.isEmpty()) {
            Integer cachedFansCount = cachedStats.get("fansCount");
            if (cachedFansCount != null && cachedFansCount == 0) {
                Integer actualFansCount = mpUserMapper.countFans(userId);
                if (actualFansCount != null && actualFansCount > 0) {
                    redisCacheService.clearUserStats(userId);
                    cachedStats = null;
                }
            }
            if (cachedStats != null && !cachedStats.isEmpty()) {
                return cachedStats;
            }
        }

        Map<String, Integer> stats = new HashMap<>();

        Integer followCount = mpUserMapper.countFollow(userId);
        stats.put("followCount", followCount != null ? followCount : 0);

        Integer fansCount = mpUserMapper.countFans(userId);
        stats.put("fansCount", fansCount != null ? fansCount : 0);
        stats.put("fans", fansCount != null ? fansCount : 0);

        Integer postCount = mpUserMapper.countPost(userId);
        stats.put("postCount", postCount != null ? postCount : 0);

        Integer likeCount = mpUserMapper.countLikes(userId);
        stats.put("likeCount", likeCount != null ? likeCount : 0);

        redisCacheService.setUserStats(userId, stats);

        return stats;
    }
    
    @Override
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
                        updateCharmAndLevel(user.getId());
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
            cacheLoginToken(token, user);
            
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
    
    @Override
    public User getUserByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }
        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.eq("email", email).last("limit 1");
        return mpUserMapper.selectOne(qw);
    }

    @Override
    public List<User> getUsersByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        return mpUserMapper.selectBatchIds(ids);
    }

    @Override
    public List<User> getRecentUsers(Long excludeUserId, boolean onlyActive, int limit) {
        QueryWrapper<User> qw = new QueryWrapper<>();
        if (onlyActive) {
            qw.eq("status", 1);
        }
        if (excludeUserId != null) {
            qw.ne("id", excludeUserId);
        }
        qw.orderByDesc("id").last("LIMIT " + limit);
        return mpUserMapper.selectList(qw);
    }

    @Override
    public User getUserByOpenid(String openid) {
        if (openid == null || openid.trim().isEmpty()) {
            return null;
        }
        
        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.eq("openid", openid).last("limit 1");
        return mpUserMapper.selectOne(qw);
    }
}


