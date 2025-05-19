package com.example.tomatomall.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PrivateMessageVO {
    private Long id;
    private Integer senderId;
    private String senderName;
    private String senderAvatar;
    private Integer receiverId;
    private String receiverName;
    private String receiverAvatar;
    private String content;
    private String contentType; // text/image/link
    private Integer status; // 1-未读,2-已读
    private String createTime;
    private Boolean isSender; // 当前用户是否是发送者
}