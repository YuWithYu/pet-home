package com.pethome.service.impl;

import com.pethome.common.Const;
import com.pethome.dao.MessageMapper;
import com.pethome.entity.Message;
import com.pethome.service.IMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import org.springframework.util.StringUtils;

/**
 * @author linyuhong
 * @date 2019/9/4
 */
@Service
public class MessageServiceImpl implements IMessageService {

    @Autowired
    private MessageMapper messageMapper;

    @Override
    public int findConversationCount(Long id) {
        return messageMapper.selectConversationCount(id);
    }

    @Override
    public List<Message> findConversations(Long userId, int offset, int limit) {
        return messageMapper.selectConversations(userId, offset, limit);
    }

    @Override
    public int findLetterCount(String conversationId) {
        return messageMapper.selectLetterCount(conversationId);
    }

    @Override
    public int findLetterUnreadCount(Long userId, String conversationId) {
        return messageMapper.selectLetterUnreadCount(userId, conversationId);
    }

    @Override
    public List<Message> findLetters(String conversationId, int offset, int limit) {
        return messageMapper.selectLetters(conversationId, offset, limit);
    }

    @Override
    public int readMessage(List<Long> ids) {
        return messageMapper.updateStatus(ids, Const.status.READ);
    }

    @Override
    public int addMessage(Message message) {
        return messageMapper.insertMessage(message);
    }

    @Override
    public Message findLatestNotice(Long userId, String topic) {
        return messageMapper.selectLatestNotice(userId, topic);
    }

    @Override
    public int findNoticeCount(Long userId, String topic) {
        return messageMapper.selectNoticeCount(userId, topic);
    }

    @Override
    public int findNoticeUnreadCount(Long userId, String topic) {
        return messageMapper.selectNoticeUnreadCount(userId, topic);
    }

    @Override
    public List<Message> findNotices(Long userId, String topic, int offset, int limit) {
        return messageMapper.selectNotices(userId, topic, offset, limit);
    }

    @Override
    public void hideConversation(Long userId, String conversationId) {
        if (userId == null || !StringUtils.hasText(conversationId)) {
            return;
        }
        messageMapper.insertHiddenConversation(userId, conversationId.trim());
    }
}
