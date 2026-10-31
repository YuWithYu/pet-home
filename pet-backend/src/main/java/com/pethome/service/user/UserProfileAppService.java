package com.pethome.service.user;

import com.pethome.entity.User;

import java.util.Map;

/**
 * 用户资料应用服务。
 *
 * <p>收敛「当前用户信息、资料读写、脱敏展示、用户列表、积分与魅力值」等编排逻辑，
 * 控制器只负责取身份参数与包装响应。预期内失败统一抛 {@code BusinessException}，
 * 保持原接口的 code 语义（401 未登录 / 403 无权限 / 1 需登录 / 500 业务失败）。</p>
 */
public interface UserProfileAppService {

    /** 当前登录用户信息（兼容 user 与 admin 表） */
    Map<String, Object> getCurrentUser(String authorization, Long userId, String username, Long loginUserId);

    /** 用户详情：本人或管理端返回完整资料，查他人返回脱敏后的公开字段 */
    User getUserDetail(String token, Long userId, Long loginUserId, boolean staffOrAdmin);

    /** 用户资产（占位实现，保持原返回结构） */
    Map<String, Object> getUserAmount();

    /** 用户列表（分页，含状态文案；管理端能力） */
    Map<String, Object> getUserList(Integer page, Integer size, String keyword, boolean staffOrAdmin);

    /** 按 ID 获取用户信息（越权场景脱敏） */
    User getUserById(Long id, Long currentUserId, boolean staffOrAdmin);

    /** 更新用户信息：普通用户仅能改自己的安全字段，管理端可编辑指定用户 */
    User updateUser(User user, Long loginUserId, boolean staffOrAdmin);

    /** 管理端按 ID 更新用户信息（状态归一化为 0/1） */
    User updateUserById(Long id, User user, boolean staffOrAdmin);

    /** 管理端启用/禁用账号 */
    boolean updateUserStatus(Long id, Integer status, boolean staffOrAdmin);

    /** 注销账号 */
    String deleteAccount(String authorization);

    /** 关注/粉丝/动态/获赞统计 */
    Map<String, Object> getUserStats(Long userId, String authorization);

    /** 积分流水（type: earn 获取 / spend 消耗） */
    Map<String, Object> getPointsRecords(String authorization, Long userId, String type, boolean staffOrAdmin);

    /** 积分统计（余额与累计） */
    Map<String, Object> getPointsStatistics(String authorization, Long userId, boolean staffOrAdmin);

    /** 重算魅力值与等级 */
    Map<String, Object> updateCharmAndLevel(String authorization);
}
