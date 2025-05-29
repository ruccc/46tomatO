package com.example.tomatomall.service.serviceImpl;

import com.example.tomatomall.TomatoException.BusinessException;
import com.example.tomatomall.po.Account;
import com.example.tomatomall.po.CustomerServiceMessage;
import com.example.tomatomall.po.CustomerServiceSession;
import com.example.tomatomall.repository.AccountRepository;
import com.example.tomatomall.repository.CustomerServiceMessageRepository;
import com.example.tomatomall.repository.CustomerServiceSessionRepository;
import com.example.tomatomall.service.CustomerServiceService;
import com.example.tomatomall.vo.CustomerServiceMessageVO;
import com.example.tomatomall.vo.CustomerServiceSessionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerServiceServiceImpl implements CustomerServiceService {

    private final CustomerServiceSessionRepository sessionRepository;
    private final CustomerServiceMessageRepository messageRepository;
    private final AccountRepository accountRepository;

//    @Override
//    @Transactional
//    public CustomerServiceSessionVO createCustomerServiceSession(Integer customerId, String questionType, String content, String orderId) {
//        // 检查用户是否已有未解决的会话
//        List<Integer> activeStatuses = new ArrayList<>(); activeStatuses.add(0);activeStatuses.add(1);// 未处理和处理中
//        if (sessionRepository.findByCustomerIdAndStatusIn(customerId, activeStatuses).isPresent()) {
//            throw new BusinessException("您已有未解决的客服会话");
//        }
//
//        // 分配客服 (简单实现：随机分配)
//        List<Account> services = accountRepository.findByRole("service");
//        if (services.isEmpty()) {
//            throw new BusinessException("暂无可用客服");
//        }
//        Account service = services.get((int)(Math.random() * services.size()));
//
//        // 创建会话
//        CustomerServiceSession session = new CustomerServiceSession();
//        session.setSessionId(generateSessionId());
//        session.setCustomerId(customerId);
//        session.setServiceId(service.getId());
//        session.setQuestionType(questionType);
//        session.setOrderId(orderId);
//        session.setStatus(0); // 未处理
//        String now = LocalDateTime.now().toString();
//        session.setCreateTime(now);
//        session.setUpdateTime(now);
//
//        CustomerServiceSession savedSession = sessionRepository.save(session);
//
//        // 发送第一条消息
//        CustomerServiceMessage message = new CustomerServiceMessage();
//        message.setSessionId(savedSession.getSessionId());
//        message.setSenderType("customer");
//        message.setContent(content);
//        message.setContentType("text");
//        message.setCreateTime(now);
//        messageRepository.save(message);
//
//        return convertToVO(savedSession);
//    }

    @Override
    public List<CustomerServiceSessionVO> getCustomerServiceSessions(Integer userId, int page, int size) {
        Page<CustomerServiceSession> sessions = userId.toString().startsWith("1") ? // 简单判断是否是客服ID
                sessionRepository.findActiveSessionsByServiceId(userId, PageRequest.of(page, size)) :
                sessionRepository.findByCustomerId(userId, PageRequest.of(page, size));

        return sessions.getContent().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public CustomerServiceSessionVO getCustomerServiceSession(String sessionId, Integer userId) {
        CustomerServiceSession session = sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new BusinessException("会话不存在或无权访问"));

        List<CustomerServiceMessage> messages = messageRepository.findBySessionId(sessionId);

        CustomerServiceSessionVO vo = convertToVO(session);
        vo.setMessages(messages.stream()
                .map(this::convertMessageToVO)
                .collect(Collectors.toList()));

        return vo;
    }

    @Override
    @Transactional
    public CustomerServiceSessionVO sendCustomerServiceMessage(String sessionId, Integer userId, String content, String contentType) {
        CustomerServiceSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException("会话不存在"));

        // 检查用户是否有权限发送消息
        if (!session.getCustomerId().equals(userId) && !session.getServiceId().equals(userId)) {
            throw new BusinessException("无权发送消息到此会话");
        }

        // 更新会话状态和时间
        if (session.getStatus() == 0) {
            session.setStatus(1); // 处理中
        }
        String now = LocalDateTime.now().toString();
        session.setUpdateTime(now);
        sessionRepository.save(session);

        // 创建消息
        CustomerServiceMessage message = new CustomerServiceMessage();
        message.setSessionId(sessionId);
        message.setSenderType(session.getCustomerId().equals(userId) ? "customer" : "service");
        message.setContent(content);
        message.setContentType(contentType);
        message.setCreateTime(now);
        messageRepository.save(message);

        return getCustomerServiceSession(sessionId, userId);
    }

    @Override
    @Transactional
    public CustomerServiceSessionVO rateCustomerService(String sessionId, Integer rating, String comment) {
        CustomerServiceSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException("会话不存在"));

        if (session.getStatus() != 2) {
            throw new BusinessException("只有已解决的会话可以评价");
        }

        session.setRating(rating);
        session.setComment(comment);
        session.setUpdateTime(LocalDateTime.now().toString());
        sessionRepository.save(session);

        return convertToVO(session);
    }

    @Override
    @Transactional
    public void closeCustomerServiceSession(String sessionId, Integer userId) {
        CustomerServiceSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException("会话不存在"));

        if (!session.getCustomerId().equals(userId) && !session.getServiceId().equals(userId)) {
            throw new BusinessException("无权关闭此会话");
        }

        session.setStatus(3); // 已关闭
        session.setUpdateTime(LocalDateTime.now().toString());
        sessionRepository.save(session);
    }

    private CustomerServiceSessionVO convertToVO(CustomerServiceSession session) {
        CustomerServiceSessionVO vo = new CustomerServiceSessionVO();
        vo.setSessionId(session.getSessionId());
        vo.setCustomerId(session.getCustomerId());

        accountRepository.findById(session.getCustomerId()).ifPresent(customer -> vo.setCustomerName(customer.getName()));

        vo.setServiceId(session.getServiceId());
        accountRepository.findById(session.getServiceId()).ifPresent(service -> vo.setServiceName(service.getName()));

        vo.setQuestionType(session.getQuestionType());
        vo.setOrderId(session.getOrderId());
        vo.setStatus(session.getStatus());
        vo.setRating(session.getRating());
        vo.setComment(session.getComment());
        vo.setCreateTime(session.getCreateTime());
        vo.setUpdateTime(session.getUpdateTime());

        return vo;
    }

    private CustomerServiceMessageVO convertMessageToVO(CustomerServiceMessage message) {
        CustomerServiceMessageVO vo = new CustomerServiceMessageVO();
        vo.setId(message.getId());
        vo.setSessionId(message.getSessionId());
        vo.setSenderType(message.getSenderType());
        vo.setContent(message.getContent());
        vo.setContentType(message.getContentType());
        vo.setCreateTime(message.getCreateTime());
        return vo;
    }

    private String generateSessionId() {
        return "CS" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
    }
}