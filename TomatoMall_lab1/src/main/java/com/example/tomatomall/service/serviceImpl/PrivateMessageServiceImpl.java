package com.example.tomatomall.service.serviceImpl;

import com.example.tomatomall.po.PrivateMessage;
import com.example.tomatomall.repository.PrivateMessageRepository;
import com.example.tomatomall.service.AccountService;
import com.example.tomatomall.service.PrivateMessageService;
import com.example.tomatomall.vo.AccountVO;
import com.example.tomatomall.vo.PrivateMessageVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class PrivateMessageServiceImpl implements PrivateMessageService {

    private final PrivateMessageRepository privateMessageRepository;
    private final AccountService accountService; // 假设有AccountService可以获取用户信息

    @Autowired
    public PrivateMessageServiceImpl(PrivateMessageRepository privateMessageRepository, AccountService accountService) {
        this.privateMessageRepository = privateMessageRepository;
        this.accountService = accountService;
    }

    @Override
    @Transactional
    public PrivateMessageVO sendMessage(Integer senderId, Integer receiverId, String content, String contentType) {
        PrivateMessage message = new PrivateMessage();
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(content);
        message.setContentType(contentType);
        message.setStatus(1); // 1表示未读
        message.setCreateTime(LocalDateTime.now().toString());

        PrivateMessage savedMessage = privateMessageRepository.save(message);
        return convertToVO(savedMessage, senderId);
    }

    @Override
    public Page<PrivateMessageVO> getConversation(Integer userId, Integer contactId, Pageable pageable) {
        Page<PrivateMessage> messages = privateMessageRepository.findConversation(userId, contactId, pageable);
        return messages.map(message -> convertToVO(message, userId));
    }

    @Override
    public List<Integer> getContacts(Integer userId) {
        List<Integer> senders = privateMessageRepository.findSendersByReceiver(userId);
        List<Integer> receivers = privateMessageRepository.findReceiversBySender(userId);

        // 合并并去重
        senders.addAll(receivers);
        return senders.stream().distinct().collect(Collectors.toList());
    }

    @Override
    public List<PrivateMessageVO> getLatestMessages(Integer userId, Integer contactId, Pageable pageable) {
        List<PrivateMessage> messages = privateMessageRepository.findLatestMessages(userId, contactId, pageable);
        return messages.stream()
                .map(message -> convertToVO(message, userId))
                .collect(Collectors.toList());
    }

    @Override
    public Integer getUnreadMessageCount(Integer userId) {
        return privateMessageRepository.countUnreadMessages(userId);
    }

    @Override
    public Integer getUnreadMessageCountFromUser(Integer userId, Integer senderId) {
        return privateMessageRepository.countUnreadMessagesFromUser(userId, senderId);
    }

    @Override
    @Transactional
    public void markMessagesAsRead(Integer userId, Integer senderId) {
        privateMessageRepository.markMessagesAsRead(userId, senderId);
    }

    private PrivateMessageVO convertToVO(PrivateMessage message, Integer currentUserId) {
        PrivateMessageVO vo = new PrivateMessageVO();
        BeanUtils.copyProperties(message, vo);

        // 设置发送者和接收者信息
        if (message.getSenderId() != null) {
            AccountVO sender = accountService.getAccountById(message.getSenderId());
            if (sender != null) {
                vo.setSenderName(sender.getName());
                vo.setSenderAvatar(sender.getAvatar());
            }
        }

        if (message.getReceiverId() != null) {
            AccountVO receiver = accountService.getAccountById(message.getReceiverId());
            if (receiver != null) {
                vo.setReceiverName(receiver.getName());
                vo.setReceiverAvatar(receiver.getAvatar());
            }
        }

        // 设置当前用户是否是发送者
        if (message.getSenderId() != null) {
            vo.setIsSender(message.getSenderId().equals(currentUserId));
        }

        return vo;
    }
}