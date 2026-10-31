package com.pethome.service.community.impl;

/**
 * 会话归属校验（安全修复共用）。
 *
 * <p>conversationId 各数字分段中必须包含登录用户 ID（作为参与方）。支持格式：
 * 用户会话 min_max / 客服会话 service_{storeId}_{userId} / outlet_{store}_{userId} / platform_{userId}。</p>
 */
final class MessageConversationGuard {

    private MessageConversationGuard() {
    }

    static boolean owns(String conversationId, Long userId) {
        if (conversationId == null || userId == null) {
            return false;
        }
        for (String part : conversationId.split("_")) {
            try {
                if (userId.equals(Long.parseLong(part.trim()))) {
                    return true;
                }
            } catch (NumberFormatException ignored) {
                // 非数字分段跳过
            }
        }
        return false;
    }
}
