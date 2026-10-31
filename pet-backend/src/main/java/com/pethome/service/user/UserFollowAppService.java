package com.pethome.service.user;

import java.util.Map;

/**
 * 用户关注关系应用服务：关注列表 / 粉丝列表的分页与展示字段组装。
 */
public interface UserFollowAppService {

    /** 关注列表（手动分页，兼容前端 list + hasMore 结构） */
    Map<String, Object> getFollowingList(Long userId, Integer page, Integer size);

    /** 粉丝列表（含是否已关注 / 是否互关标识） */
    Map<String, Object> getFansList(Long userId, Integer page, Integer size, Long currentUserId);
}
