package com.pethome.service.admin;

import java.util.Map;

/**
 * 后台登录与当前账号信息应用服务。
 *
 * <p>收敛登录防爆破、密码迁移、权限装配等逻辑，控制器只负责接收凭据与包装响应。</p>
 */
public interface AdminAuthAppService {

    /** 账号密码登录，返回 token 与账号信息 */
    Map<String, Object> login(String username, String password);

    /** 根据 Bearer Token 获取当前登录账号信息（含权限列表） */
    Map<String, Object> getCurrentAdminInfo(String authorization);

    /** 当前账号更新头像，返回更新后的头像地址 */
    String updateMyAvatar(String authorization, String avatar);

    /** 生成密码密文（迁移/测试用） */
    String encryptPassword(String rawPassword);
}
