package com.pethome.service.user;

import java.util.Map;

/**
 * 用户鉴权应用服务：登录、注册、短信验证码、微信登录/授权、账号与密码变更、手机号绑定。
 *
 * <p>密码校验统一走 {@code UserService.verifyPassword}（BCrypt + 旧 MD5 自动升级），
 * 预期内失败抛 {@code BusinessException}，保持原接口的 code 语义。</p>
 */
public interface UserAuthAppService {

    /** 账号密码登录（兼容 admin 表与 user 表） */
    Map<String, Object> login(String username, String phone, String password);

    /** 手机号注册（已配置短信服务时强制校验验证码） */
    Map<String, Object> register(String phone, String password, String nickname, String smsCode);

    /** 忘记密码：手机号 + 短信验证码重置密码，返回提示文案 */
    String forgotPassword(String phone, String smsCode, String newPassword);

    /** 当前账号是否已设置密码（微信用户视为未设置） */
    Boolean hasPassword(String authorization);

    /** 修改密码（登录后），返回提示文案 */
    String changePassword(String authorization, String oldPassword, String newPassword);

    /** 微信小程序登录 */
    Map<String, Object> wxappLogin(String code);

    /** 微信小程序授权（占位实现，保持原返回结构） */
    Map<String, Object> wxappAuthorize();

    /** 获取微信手机号，并在登录态下回写当前用户手机号 */
    Map<String, String> getPhoneNumber(String code, String authorization);

    /** 绑定手机号（登录后，绑定其它号码需短信验证码） */
    String bindPhone(String authorization, String phone, String smsCode);

    /** 解绑手机号（登录后） */
    String unbindPhone(String authorization);

    /** 验证当前账号登录密码（敏感操作前二次确认） */
    String verifyPassword(String authorization, String password);

    /** 修改账号 */
    String changeUsername(String authorization, String newUsername);
}
