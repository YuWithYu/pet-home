package com.pethome.service.user;

/**
 * 从请求头 / token 解析当前登录用户ID。
 *
 * <p>解析顺序：先查 Redis 登录缓存，再解析 JWT 的 subject（用户名/手机号/数字ID）。
 * 解析失败一律返回 null，不向上抛出异常，供「可选登录」类接口使用。</p>
 */
public interface UserTokenService {

    /** 从 Authorization 头（Bearer xxx）解析用户ID，无效返回 null */
    Long resolveUserId(String authHeader);

    /** 从裸 token 解析用户ID，无效返回 null */
    Long resolveUserIdByToken(String token);
}
