package com.example.tomatomall.service.serviceImpl;

import com.example.tomatomall.TomatoException.BusinessException;
import com.example.tomatomall.po.Account;
import com.example.tomatomall.po.PrivateMessage;
import com.example.tomatomall.repository.AccountRepository;
import com.example.tomatomall.repository.PrivateMessageRepository;
import com.example.tomatomall.service.MessageService;
import com.example.tomatomall.vo.PrivateMessageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MessageServiceImpl implements MessageService {

    private final PrivateMessageRepository privateMessageRepository;
    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public PrivateMessageVO sendPrivateMessage(Integer senderId, Integer receiverId, String content, String contentType) {
        // 验证接收者是否存在
        accountRepository.findById(receiverId)
                .orElseThrow(() -> new BusinessException("接收用户不存在"));

        // 不能给自己发送消息
        if (senderId.equals(receiverId)) {
            throw new BusinessException("不能给自己发送私信");
        }

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
    public Page<PrivateMessageVO> getPrivateMessages(Integer userId, Integer contactId, int page, int size) {
        Page<PrivateMessage> messages = privateMessageRepository.findConversation(
                userId, contactId, PageRequest.of(page, size));
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
    public List<PrivateMessageVO> getLatestMessages(Integer userId, Integer contactId, int page, int size) {
        List<PrivateMessage> messages = privateMessageRepository.findLatestMessages(
                userId, contactId, PageRequest.of(page, size));
        return messages.stream()
                .map(message -> convertToVO(message, userId))
                .collect(Collectors.toList());
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