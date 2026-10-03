package com.pethome.service;

import com.pethome.entity.Message;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author linyuhong
 * @date 2019/9/4
 */
public interface IMessageService {

    int findConversationCount(@Param("id") Long id);

    List<Message> findConversations(@Param("userId") Long userId, @Param("offset") int offset, @Param("limit") int limit);

    int findLetterCount(@Param("conversationId") String conversationId);

    int findLetterUnreadCount(@Param("userId") Long userId, @Param("conversationId") String conversationId);

    List<Message> findLetters(@Param("conversationId") String conversationId, @Param("offset") int offset, @Param("limit") int limit);

    int readMessage(@Param("ids") List<Long> ids);

    int addMessage(Message message);

    Message findLatestNotice(@Param("userId") Long userId, @Param("topic") String topic);

    int findNoticeCount(@Param("userId") Long userId, @Param("topic") String topic);

    int findNoticeUnreadCount(@Param("userId") Long userId, @Param("topic") String topic);

    List<Message> findNotices(@Param("userId") Long userId, @Param("topic") String topic, @Param("offset") int offset, @Param("limit") int limit);

    /** 用户侧隐藏会话（多端同步，列表不再返回该会话） */
    void hideConversation(Long userId, String conversationId);

}
