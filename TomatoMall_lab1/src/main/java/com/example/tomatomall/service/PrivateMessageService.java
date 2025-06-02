// PrivateMessageService.java
package com.example.tomatomall.service;

import com.example.tomatomall.vo.PrivateMessageVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PrivateMessageService {
    /**
     * 发送私信
     */
    PrivateMessageVO sendMessage(Integer senderId, Integer receiverId, String content, String contentType);

    /**
     * 获取与某个用户的对话
     */
    Page<PrivateMessageVO> getConversation(Integer userId, Integer contactId, Pageable pageable);

    /**
     * 获取用户的所有联系人列表
     */
    List<Integer> getContacts(Integer userId);

    /**
     * 获取与联系人的最新消息
     */
    List<PrivateMessageVO> getLatestMessages(Integer userId, Integer contactId, Pageable pageable);

    /**
     * 获取用户未读消息总数
     */
    Integer getUnreadMessageCount(Integer userId);

    /**
     * 获取来自特定用户的未读消息数
     */
    Integer getUnreadMessageCountFromUser(Integer userId, Integer senderId);

    /**
     * 标记消息为已读
     */
    void markMessagesAsRead(Integer userId, Integer senderId);
}