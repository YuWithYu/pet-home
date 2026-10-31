package com.pethome.service.user;

import java.util.Map;

/**
 * 用户内容应用服务：个人主页动态列表。
 */
public interface UserContentAppService {

    /** 用户动态列表（分页，仅保留前端需要的 id/content/image） */
    Map<String, Object> getUserPosts(Long userId, Integer page, Integer size);
}
