package com.example.tomatomall.vo;

import lombok.Data;

@Data
public class PrivateConversationVO {
    private Integer userId;
    private String username;
    private String avatar;
    private String lastMessage;
    private String lastMessageTime;
    private Integer unreadCount;
}