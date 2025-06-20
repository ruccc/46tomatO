package com.example.tomatomall.service;

import com.example.tomatomall.vo.PrivateMessageVO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface MessageService {
    PrivateMessageVO sendPrivateMessage(Integer senderId, Integer receiverId, String content, String contentType);

    Page<PrivateMessageVO> getPrivateMessages(Integer userId, Integer contactId, int page, int size);

    List<Integer> getContacts(Integer userId);

    List<PrivateMessageVO> getLatestMessages(Integer userId, Integer contactId, int page, int size);

    void markMessagesAsRead(Integer userId, Integer senderId);

    Integer getUnreadMessageCount(Integer userId);

    Integer getUnreadMessageCountFromUser(Integer userId, Integer senderId);
}