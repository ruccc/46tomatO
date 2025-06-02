package com.example.tomatomall.vo;

import lombok.Data;

@Data
public class NotificationVO {
    private Long id;
    private Integer userId;
    private String type; // order/system/promotion
    private String title;
    private String content;
    private String relatedId;
    private Boolean isRead;
    private String createTime;
}