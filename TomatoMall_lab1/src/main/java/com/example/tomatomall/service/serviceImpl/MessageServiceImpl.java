package com.example.tomatomall.service.serviceImpl;

import com.example.tomatomall.TomatoException.BusinessException;
import com.example.tomatomall.po.Account;
import com.example.tomatomall.po.PrivateMessage;
import com.example.tomatomall.repository.AccountRepository;
import com.example.tomatomall.repository.PrivateMessageRepository;
import com.example.tomatomall.service.MessageService;
import com.example.tomatomall.vo.PrivateConversationVO;
import com.example.tomatomall.vo.PrivateMessageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final PrivateMessageRepository privateMessageRepository;
    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public PrivateMessageVO sendPrivateMessage(Integer senderId, Integer receiverId, String content, String contentType) {
        accountRepository.findById(receiverId)
                .orElseThrow(() -> new BusinessException("接收用户不存在"));

        if (senderId.equals(receiverId)) {
            throw new BusinessException("不能给自己发送私信");
        }

        PrivateMessage message = new PrivateMessage();
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(content);
        message.setContentType(contentType);
        message.setStatus(1); // 未读
        message.setCreateTime(LocalDateTime.now().toString());

        PrivateMessage savedMessage = privateMessageRepository.save(message);
        return convertToVO(savedMessage, senderId);
    }

    @Override
    public Page<PrivateConversationVO> getPrivateConversations(Integer userId, int page, int size) {
        // 获取所有联系人ID（发送过消息或接收过消息的用户）
        Set<Integer> contactUserIds = new HashSet<>();
        contactUserIds.addAll(privateMessageRepository.findReceiversBySender(userId));
        contactUserIds.addAll(privateMessageRepository.findSendersByReceiver(userId));

        List<PrivateConversationVO> conversations = contactUserIds.stream()
                .map(contactId -> {
                    Account contact = accountRepository.findById(contactId).orElse(null);
                    if (contact == null) return null;

                    // 获取最新的一条消息
                    List<PrivateMessage> latestMessages = privateMessageRepository.findLatestMessages(
                            userId, contactId, PageRequest.of(0, 1));
                    if (latestMessages.isEmpty()) return null;

                    PrivateMessage latestMessage = latestMessages.get(0);
                    int unreadCount = privateMessageRepository.countUnreadMessagesFromUser(userId, contactId);

                    PrivateConversationVO vo = new PrivateConversationVO();
                    vo.setUserId(contactId);
                    vo.setUsername(contact.getName());
                    vo.setAvatar(contact.getAvatar());
                    vo.setLastMessage(latestMessage.getContent());
                    vo.setLastMessageTime(latestMessage.getCreateTime());
                    vo.setUnreadCount(unreadCount);
                    return vo;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return new PageImpl<>(conversations, PageRequest.of(page, size), conversations.size());
    }

    @Override
    public Page<PrivateMessageVO> getPrivateMessages(Integer userId, Integer contactId, int page, int size) {
        Page<PrivateMessage> messages = privateMessageRepository.findConversation(
                userId, contactId, PageRequest.of(page, size));

        return messages.map(message -> convertToVO(message, userId));
    }

    @Override
    @Transactional
    public void markMessagesAsRead(Integer userId, Integer senderId) {
        privateMessageRepository.markMessagesAsRead(userId, senderId);
    }

    @Override
    public Integer getUnreadMessageCount(Integer userId) {
        return privateMessageRepository.countUnreadMessages(userId);
    }

    @Override
    public Integer getUnreadMessageCountFromUser(Integer userId, Integer senderId) {
        return privateMessageRepository.countUnreadMessagesFromUser(userId, senderId);
    }

    private PrivateMessageVO convertToVO(PrivateMessage message, Integer currentUserId) {
        PrivateMessageVO vo = new PrivateMessageVO();
        vo.setId(message.getId());
        vo.setSenderId(message.getSenderId());
        vo.setReceiverId(message.getReceiverId());
        vo.setContent(message.getContent());
        vo.setContentType(message.getContentType());
        vo.setStatus(message.getStatus());
        vo.setCreateTime(message.getCreateTime());
        vo.setIsSender(message.getSenderId().equals(currentUserId));

        // 设置发送者和接收者信息
        Account sender = accountRepository.findById(message.getSenderId()).orElse(null);
        if (sender != null) {
            vo.setSenderName(sender.getName());
            vo.setSenderAvatar(sender.getAvatar());
        }

        Account receiver = accountRepository.findById(message.getReceiverId()).orElse(null);
        if (receiver != null) {
            vo.setReceiverName(receiver.getName());
            vo.setReceiverAvatar(receiver.getAvatar());
        }

        return vo;
    }
}