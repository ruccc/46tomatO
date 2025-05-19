package com.example.tomatomall.service;

import com.example.tomatomall.vo.CustomerServiceSessionVO;

import java.util.List;

public interface CustomerServiceService {
    CustomerServiceSessionVO createCustomerServiceSession(Integer customerId, String questionType, String content, String orderId);

    List<CustomerServiceSessionVO> getCustomerServiceSessions(Integer userId, int page, int size);

    CustomerServiceSessionVO getCustomerServiceSession(String sessionId, Integer userId);

    CustomerServiceSessionVO sendCustomerServiceMessage(String sessionId, Integer userId, String content, String contentType);

    CustomerServiceSessionVO rateCustomerService(String sessionId, Integer rating, String comment);

    void closeCustomerServiceSession(String sessionId, Integer userId);
}