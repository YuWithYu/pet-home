package com.pethome.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.User;

import java.util.Map;

public interface UserService {
    User register(User user);
    String register(String phone, String password, String nickname);
    String login(String username, String password);
    String loginByPhone(String phone, String password);
    User getUserById(Long id);
    User getUserByUsername(String username);
    User getUserByPhone(String phone);
    /** 根据昵称查询用户（用于登录：支持手机号/用户名/昵称） */
    User getUserByNickname(String nickname);
    IPage<User> getUserList(Page<User> page);
    boolean updateUser(User user);
    boolean deleteUser(Long id);
    boolean changePassword(Long userId, String oldPassword, String newPassword);
    
    /**
     * 获取用户统计数据（关注数、粉丝数、动态数、获赞数）
     */
    Map<String, Integer> getUserStats(Long userId);
    
    /**
     * 缓存登录 token 和用户信息
     * @param token 登录 token
     * @param user 用户信息
     */
    void cacheLoginToken(String token, User user);
    
    /**
     * 根据 token 获取缓存的用户信息
     * @param token 登录 token
     * @return 用户信息，如果 token 无效或过期则返回 null
     */
    User getUserByToken(String token);
    
    /**
     * 清除用户信息缓存
     * @param userId 用户ID
     */
    void clearUserCache(Long userId);
    
    /**
     * 检查昵称是否已被使用
     * @param nickname 昵称
     * @param excludeUserId 排除的用户ID（用于更新时检查，注册时传null）
     * @return true表示昵称已被使用，false表示可用
     */
    boolean isNicknameExists(String nickname, Long excludeUserId);
    
    /**
     * 更新用户的魅力值和等级
     * @param userId 用户ID
     */
    void updateCharmAndLevel(Long userId);
    
    /**
     * 微信小程序登录
     * @param code 微信小程序登录code
     * @return 登录token，如果登录失败返回null
     */
    String loginByWechat(String code);
    
    /**
     * 根据openid获取用户
     * @param openid 微信openid
     * @return 用户信息，如果不存在返回null
     */
    User getUserByOpenid(String openid);

    /**
     * 忘记密码：根据手机号重置密码（需先通过短信验证码校验）
     * @param phone 手机号
     * @param newPassword 新密码（明文，内部会 MD5 加密）
     * @return 是否重置成功
     */
    boolean resetPasswordByPhone(String phone, String newPassword);

    /**
     * 解绑手机号：将指定用户的 phone 置为 null（显式更新数据库，避免 updateById 忽略 null 字段）
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean unbindPhoneByUserId(Long userId);

    /**
     * 修改账号：校验格式与占用，更新 username 并写入 username_updated_at
     * 格式：以字母或下划线开头，6-20位，仅字母数字下划线减号
     * @param userId 当前用户ID
     * @param newUsername 新账号
     * @return 成功返回 null，失败返回错误文案
     */
    String changeUsername(Long userId, String newUsername);
}
