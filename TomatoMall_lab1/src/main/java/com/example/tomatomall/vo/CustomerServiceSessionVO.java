package com.example.tomatomall.vo;

import lombok.Data;

import java.util.List;

@Data
public class CustomerServiceSessionVO {
    private String sessionId;
    private Integer customerId;
    private String customerName;
    private Integer serviceId;
    private String serviceName;
    private String questionType; // order/payment/delivery/other
    private String orderId;
    private Integer status; // 0-未处理,1-处理中,2-已解决,3-已关闭
    private Integer rating; // 1-5
    private String comment;
    private String createTime;
    private String updateTime;
    private List<CustomerServiceMessageVO> messages;
}