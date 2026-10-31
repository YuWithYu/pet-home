package com.pethome.controller.user;

import com.pethome.common.Result;
import com.pethome.common.SafeCall;
import com.pethome.entity.User;
import com.pethome.service.user.UserAuthAppService;
import com.pethome.service.user.UserContentAppService;
import com.pethome.service.user.UserFollowAppService;
import com.pethome.service.user.UserProfileAppService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import java.util.Map;

/**
 * 用户管理接口。
 *
 * <p>表现层职责：解析身份参数 → 调用应用服务 → 包装响应。
 * 业务编排、异常处理、脱敏与越权防护均下沉到 service.user 下的应用服务。</p>
 */
@RestController
@RequestMapping("/api/user")  // 统一使用单数路径，对应数据库user表
@Tag(name = "用户管理")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserProfileAppService userProfileAppService;

    @Autowired
    private UserFollowAppService userFollowAppService;

    @Autowired
    private UserContentAppService userContentAppService;

    @Autowired
    private UserAuthAppService userAuthAppService;

    @GetMapping("/current")
    @Operation(summary = "获取当前用户信息（支持 user 和 admin 表）")
    public Result<Map<String, Object>> getCurrentUser(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam(required = false, value = "userId") Long userId,
            @RequestParam(required = false, value = "username") String username,
            HttpServletRequest request) {
        return SafeCall.of(() -> Result.success(
                userProfileAppService.getCurrentUser(token, userId, username, loginUserId(request))),
                "获取用户信息失败，请稍后重试", log);
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录（兼容 user 和 admin 表）")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> loginData) {
        return SafeCall.of(() -> Result.success(userAuthAppService.login(
                loginData.get("username"), loginData.get("phone"), loginData.get("password"))),
                "登录失败，请稍后重试", log);
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public Result<Map<String, Object>> register(@RequestBody Map<String, String> registerData) {
        return SafeCall.of(() -> Result.success(userAuthAppService.register(
                registerData.get("phone"), registerData.get("password"),
                registerData.get("nickname"), registerData.get("smsCode"))),
                "注册失败，请稍后重试", log);
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "忘记密码：通过手机号+短信验证码重置密码")
    public Result<String> forgotPassword(@RequestBody Map<String, String> body) {
        return SafeCall.of(() -> Result.success(userAuthAppService.forgotPassword(
                body != null ? body.get("phone") : null,
                body != null ? body.get("smsCode") : null,
                body != null ? body.get("newPassword") : null)),
                "重置失败，请稍后重试", log);
    }

    @GetMapping("/has-password")
    @Operation(summary = "检查当前账号是否已设置密码（微信登录用户可能无密码）")
    public Result<Boolean> hasPassword(
            @RequestHeader(value = "Authorization", required = false) String token) {
        return SafeCall.of(() -> Result.success(userAuthAppService.hasPassword(token)),
                "查询失败", log);
    }

    @PostMapping("/change-password")
    @Operation(summary = "修改密码（登录后）")
    public Result<String> changePassword(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody Map<String, String> body) {
        return SafeCall.of(() -> Result.success(userAuthAppService.changePassword(
                token, body != null ? body.get("oldPassword") : null,
                body != null ? body.get("newPassword") : null)),
                "修改失败，请稍后重试", log);
    }

    @PostMapping("/delete")
    @Operation(summary = "注销账号")
    public Result<String> deleteAccount(
            @RequestHeader(value = "Authorization", required = false) String token) {
        return SafeCall.of(() -> Result.success(userProfileAppService.deleteAccount(token)),
                "注销失败，请稍后重试", log);
    }

    @PostMapping("/update")
    @Operation(summary = "更新用户信息")
    public Result<User> updateUser(@RequestBody User user, HttpServletRequest request) {
        boolean admin = isAdmin(request);
        Long currentUserId = loginUserId(request);
        // 安全修复：写用户主数据（积分/会员等级/魅力值/状态/密码）仅限 admin/store_admin；
        // staff（店员）与 C 端用户只能修改自己的安全字段（昵称/头像/签名等）
        if (!admin) {
            if (com.pethome.security.RequestIdentity.isStaff(request)) {
                return Result.error(403, "店员无权修改用户资料");
            }
            if (currentUserId == null) {
                return Result.error(401, "用户未登录或token无效");
            }
            if (user != null && user.getId() != null && !user.getId().equals(currentUserId)) {
                return Result.error(403, "只能修改自己的资料");
            }
        }
        return SafeCall.of(() -> Result.success(userProfileAppService.updateUser(
                user, currentUserId, admin)),
                "更新用户信息失败，请稍后重试", log);
    }

    @GetMapping("/stats")
    @Operation(summary = "获取用户统计信息")
    public Result<Map<String, Object>> getUserStats(
            @RequestParam(required = false) Long userId,
            @RequestHeader(value = "Authorization", required = false) String token) {
        return SafeCall.of(() -> Result.success(userProfileAppService.getUserStats(userId, token)),
                "获取用户统计失败，请稍后重试", log);
    }

    @PostMapping("/wxapp/login")
    @Operation(summary = "微信小程序登录")
    public Result<Map<String, Object>> wxappLogin(@RequestBody Map<String, String> loginData) {
        return SafeCall.of(() -> Result.success(userAuthAppService.wxappLogin(loginData.get("code"))),
                "微信登录失败，请稍后重试", log);
    }

    @PostMapping("/wxapp/authorize")
    @Operation(summary = "微信小程序授权")
    public Result<Map<String, Object>> wxappAuthorize(@RequestBody Map<String, String> authData) {
        return SafeCall.of(() -> Result.success(userAuthAppService.wxappAuthorize()),
                "微信授权失败，请稍后重试", log);
    }

    @GetMapping("/detail")
    @Operation(summary = "获取用户详情")
    public Result<User> getUserDetail(@RequestParam(required = false) String token,
                                      @RequestParam(required = false) Long userId,
                                      HttpServletRequest request) {
        return SafeCall.of(() -> Result.success(userProfileAppService.getUserDetail(
                token, userId, loginUserId(request), isStaffOrAdmin(request))),
                "获取用户详情失败，请稍后重试", log);
    }

    @GetMapping("/amount")
    @Operation(summary = "获取用户资产")
    public Result<Map<String, Object>> getUserAmount(
            @RequestParam(required = false) String token) {
        return SafeCall.of(() -> Result.success(userProfileAppService.getUserAmount()),
                "获取用户资产失败，请稍后重试", log);
    }

    @GetMapping("/list")
    @Operation(summary = "获取用户列表（分页）")
    public Result<Map<String, Object>> getUserList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            HttpServletRequest request) {
        return SafeCall.of(() -> Result.success(userProfileAppService.getUserList(
                page, size, keyword, isStaffOrAdmin(request))),
                "获取用户列表失败，请稍后重试", log);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取用户信息")
    public Result<User> getUserById(@PathVariable Long id, HttpServletRequest request) {
        return SafeCall.of(() -> Result.success(userProfileAppService.getUserById(
                id, loginUserId(request), isStaffOrAdmin(request))),
                "获取用户信息失败，请稍后重试", log);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新用户信息")
    public Result<User> updateUserById(@PathVariable Long id, @RequestBody User user,
                                       HttpServletRequest request) {
        // 安全修复：写用户主数据仅限 admin/store_admin，staff 只能查看
        return SafeCall.of(() -> Result.success(userProfileAppService.updateUserById(
                id, user, isAdmin(request))),
                "更新用户信息失败，请稍后重试", log);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新用户状态（1正常，0禁用）")
    public Result<Boolean> updateUserStatus(@PathVariable Long id, @RequestParam Integer status,
                                            HttpServletRequest request) {
        // 安全修复：禁用/启用账号仅限 admin/store_admin，staff 只能查看
        return SafeCall.of(() -> Result.success(userProfileAppService.updateUserStatus(
                id, status, isAdmin(request))),
                "更新用户状态失败，请稍后重试", log);
    }

    @GetMapping("/{userId}/following")
    @Operation(summary = "获取用户关注列表")
    public Result<Map<String, Object>> getFollowingList(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) Long currentUserId) {
        return SafeCall.of(() -> Result.success(
                userFollowAppService.getFollowingList(userId, page, size)),
                "获取关注列表失败，请稍后重试", log);
    }

    @GetMapping("/{userId}/fans")
    @Operation(summary = "获取用户粉丝列表")
    public Result<Map<String, Object>> getFansList(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) Long currentUserId) {
        return SafeCall.of(() -> Result.success(
                userFollowAppService.getFansList(userId, page, size, currentUserId)),
                "获取粉丝列表失败，请稍后重试", log);
    }

    @GetMapping("/{userId}/posts")
    @Operation(summary = "获取用户动态列表")
    public Result<Map<String, Object>> getUserPosts(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        return SafeCall.of(() -> Result.success(
                userContentAppService.getUserPosts(userId, page, size)),
                "获取动态列表失败，请稍后重试", log);
    }

    @PostMapping("/getPhoneNumber")
    @Operation(summary = "获取微信手机号")
    public Result<Map<String, String>> getPhoneNumber(
            @RequestBody Map<String, String> requestData,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return SafeCall.of(() -> Result.success(userAuthAppService.getPhoneNumber(
                requestData.get("code"), authHeader)),
                "获取手机号失败，请稍后重试", log);
    }

    @PostMapping("/bind-phone")
    @Operation(summary = "绑定手机号（使用其它号码时需短信验证码，需登录）")
    public Result<String> bindPhone(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, String> body) {
        return SafeCall.of(() -> Result.success(userAuthAppService.bindPhone(
                authHeader, body != null ? body.get("phone") : null,
                body != null ? body.get("smsCode") : null)),
                "绑定失败，请稍后重试", log);
    }

    @PostMapping("/unbind-phone")
    @Operation(summary = "解绑手机号（需登录）。解绑后该手机号可被其他账号绑定；当前账号可再绑定其他手机号。")
    public Result<String> unbindPhone(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return SafeCall.of(() -> Result.success(userAuthAppService.unbindPhone(authHeader)),
                "解绑失败，请稍后重试", log);
    }

    @PostMapping("/verify-password")
    @Operation(summary = "验证当前账号登录密码（用于修改账号等敏感操作前）")
    public Result<String> verifyPassword(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody(required = false) Map<String, String> body) {
        return SafeCall.of(() -> Result.success(userAuthAppService.verifyPassword(
                authHeader, body != null ? body.get("password") : null)),
                "验证失败，请稍后重试", log);
    }

    @PostMapping("/change-username")
    @Operation(summary = "修改账号（格式：字母或下划线开头，6-20位，仅字母数字下划线减号）")
    public Result<String> changeUsername(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, String> body) {
        return SafeCall.of(() -> Result.success(userAuthAppService.changeUsername(
                authHeader, body != null ? body.get("newUsername") : null)),
                "修改失败，请稍后重试", log);
    }

    @GetMapping("/points/records")
    @Operation(summary = "获取用户积分记录")
    public Result<Map<String, Object>> getPointsRecords(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false, defaultValue = "earn") String type,
            HttpServletRequest request) {
        return SafeCall.of(() -> Result.success(userProfileAppService.getPointsRecords(
                token, userId, type, isStaffOrAdmin(request))),
                "获取积分记录失败，请稍后重试", log);
    }

    @GetMapping("/points/statistics")
    @Operation(summary = "获取用户积分统计")
    public Result<Map<String, Object>> getPointsStatistics(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam(required = false) Long userId,
            HttpServletRequest request) {
        return SafeCall.of(() -> Result.success(userProfileAppService.getPointsStatistics(
                token, userId, isStaffOrAdmin(request))),
                "获取积分统计失败，请稍后重试", log);
    }

    @PostMapping("/update/charm-level")
    @Operation(summary = "更新用户的魅力值和等级")
    public Result<Map<String, Object>> updateCharmAndLevel(
            @RequestHeader(value = "Authorization", required = false) String token) {
        return SafeCall.of(() -> Result.success("更新成功",
                userProfileAppService.updateCharmAndLevel(token)),
                "更新失败，请稍后重试", log);
    }

    /** 从拦截器写入的登录态属性解析当前用户 ID（普通用户 Redis token） */
    private Long loginUserId(HttpServletRequest request) {
        Object attr = request.getAttribute("userId");
        return attr instanceof Number ? ((Number) attr).longValue() : null;
    }

    /** staff/admin 判定：RoleInterceptor 在管理端登录后写入 adminId attribute */
    private boolean isStaffOrAdmin(HttpServletRequest request) {
        return request.getAttribute("adminId") instanceof Number;
    }

    /** admin/store_admin 判定（不含 staff），用于写用户主数据等特权操作 */
    private boolean isAdmin(HttpServletRequest request) {
        return com.pethome.security.RequestIdentity.isAdmin(request);
    }
}
