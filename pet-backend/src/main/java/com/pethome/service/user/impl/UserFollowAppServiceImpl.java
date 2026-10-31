package com.pethome.service.user.impl;

import com.pethome.entity.User;
import com.pethome.entity.UserFollow;
import com.pethome.service.user.UserFollowAppService;
import com.pethome.service.user.UserFollowService;
import com.pethome.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserFollowAppServiceImpl implements UserFollowAppService {

    private static final String DEFAULT_AVATAR = "/static/images/garfield-default-avatar.png";
    private static final String PHONE_PATTERN = "^1[3-9]\\d{9}$";

    @Autowired
    private UserFollowService userFollowService;

    @Autowired
    private UserService userService;

    @Override
    public Map<String, Object> getFollowingList(Long userId, Integer page, Integer size) {
        List<UserFollow> allFollows = userFollowService.getFollowingList(userId);

        // 手动分页
        int start = (page - 1) * size;
        int end = Math.min(start + size, allFollows.size());
        List<UserFollow> pagedFollows = allFollows.subList(Math.min(start, allFollows.size()), end);

        // 转换为前端格式并检查是否已关注
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (UserFollow follow : pagedFollows) {
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", follow.getFollowingId());
            // 使用昵称，如果没有则使用用户名
            String displayName = follow.getFollowingName() != null ? follow.getFollowingName() : "";
            // 如果displayName是手机号格式，尝试从数据库获取昵称
            if (displayName.matches(PHONE_PATTERN)) {
                User user = userService.getUserById(follow.getFollowingId());
                if (user != null && user.getNickname() != null && !user.getNickname().isEmpty()) {
                    displayName = user.getNickname();
                }
            }
            userInfo.put("username", displayName);
            userInfo.put("avatarUrl", normalizeAvatar(follow.getFollowingAvatar()));
            userInfo.put("avatar", normalizeAvatar(follow.getFollowingAvatar())); // 同时提供avatar字段以兼容前端
            // 获取用户信息以获取signature
            User followUser = userService.getUserById(follow.getFollowingId());
            userInfo.put("signature", followUser != null && followUser.getSignature() != null ? followUser.getSignature() : "");
            userInfo.put("bio", followUser != null && followUser.getSignature() != null ? followUser.getSignature() : ""); // bio 使用 signature 的值

            // 获取粉丝数
            userInfo.put("fansCount", userFollowService.getFollowerCount(follow.getFollowingId()));

            // 关注列表中的用户都是已关注的，所以这里始终为 true
            userInfo.put("isFollowing", true);

            resultList.add(userInfo);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("list", resultList);
        response.put("hasMore", end < allFollows.size());
        return response;
    }

    @Override
    public Map<String, Object> getFansList(Long userId, Integer page, Integer size, Long currentUserId) {
        List<UserFollow> allFans = userFollowService.getFollowerList(userId);

        // 手动分页
        int start = (page - 1) * size;
        int end = Math.min(start + size, allFans != null ? allFans.size() : 0);
        List<UserFollow> pagedFans = (allFans != null && !allFans.isEmpty())
                ? allFans.subList(Math.min(start, allFans.size()), end)
                : new ArrayList<>();

        // 转换为前端格式并检查是否已关注
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (UserFollow follow : pagedFans) {
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", follow.getFollowerId());
            // 使用昵称，如果没有则使用用户名
            String displayName = follow.getFollowerName() != null ? follow.getFollowerName() : "";
            // 如果displayName是手机号格式，尝试从数据库获取昵称
            if (displayName.matches(PHONE_PATTERN)) {
                User user = userService.getUserById(follow.getFollowerId());
                if (user != null && user.getNickname() != null && !user.getNickname().isEmpty()) {
                    displayName = user.getNickname();
                }
            }
            userInfo.put("username", displayName);
            String avatarUrl = normalizeAvatar(follow.getFollowerAvatar());
            userInfo.put("avatarUrl", avatarUrl);
            userInfo.put("avatar", avatarUrl); // 同时提供avatar字段以兼容前端
            // 获取用户信息以获取signature
            User followUser = userService.getUserById(follow.getFollowerId());
            userInfo.put("signature", followUser != null && followUser.getSignature() != null ? followUser.getSignature() : "");
            userInfo.put("bio", followUser != null && followUser.getSignature() != null ? followUser.getSignature() : ""); // bio 使用 signature 的值

            // 获取该粉丝的粉丝数
            userInfo.put("fansCount", userFollowService.getFollowerCount(follow.getFollowerId()));

            // 判断当前用户是否已关注列表中的用户（用于显示"回关"或"互相关注"）
            boolean isFollowing = false;
            boolean isMutualFollow = false; // 是否互相关注
            if (currentUserId != null && currentUserId != 0) {
                isFollowing = userFollowService.isFollowing(currentUserId, follow.getFollowerId());
                boolean fanIsFollowingMe = userFollowService.isFollowing(follow.getFollowerId(), currentUserId);
                isMutualFollow = isFollowing && fanIsFollowingMe;
            }
            userInfo.put("isFollowing", isFollowing);
            userInfo.put("isMutualFollow", isMutualFollow); // 添加互相关注标识

            resultList.add(userInfo);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("list", resultList);
        response.put("hasMore", allFans != null && end < allFans.size());
        return response;
    }

    /** 头像为空或非法（临时标识符）时回退默认头像 */
    private String normalizeAvatar(String avatar) {
        String avatarUrl = avatar != null && !avatar.isEmpty() ? avatar : DEFAULT_AVATAR;
        if (avatarUrl.length() < 10
                || (!avatarUrl.startsWith("http://") && !avatarUrl.startsWith("https://") && !avatarUrl.startsWith("/"))) {
            avatarUrl = DEFAULT_AVATAR;
        }
        return avatarUrl;
    }
}
