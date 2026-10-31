package com.pethome.service.admin;

/**
 * 管理员侧的 C 端用户管理应用服务。
 */
public interface AdminUserAppService {

    /** 管理员创建 C 端用户（免短信验证码），返回新用户ID */
    Long createUser(String phone, String password, String nickname);

    /** 删除 C 端用户并清理缓存 */
    boolean deleteUser(Long userId);
}
