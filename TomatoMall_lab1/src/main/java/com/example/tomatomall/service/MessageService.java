package com.example.tomatomall.service;

import com.example.tomatomall.vo.PrivateConversationVO;
import com.example.tomatomall.vo.PrivateMessageVO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface MessageService {
    PrivateMessageVO sendPrivateMessage(Integer senderId, Integer receiverId, String content, String contentType);

    Page<PrivateConversationVO> getPrivateConversations(Integer userId, int page, int size);

    Page<PrivateMessageVO> getPrivateMessages(Integer userId, Integer contactId, int page, int size);

    void markMessagesAsRead(Integer userId, Integer senderId);

    Integer getUnreadMessageCount(Integer userId);

    Integer getUnreadMessageCountFromUser(Integer userId, Integer senderId);
}