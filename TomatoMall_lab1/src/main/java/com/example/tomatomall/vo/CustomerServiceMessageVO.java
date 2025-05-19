package com.example.tomatomall.vo;

import lombok.Data;

@Data
public class CustomerServiceMessageVO {
    private Long id;
    private String sessionId;
    private String senderType; // customer/service
    private String content;
    private String contentType; // text/image/link
    private String createTime;
}