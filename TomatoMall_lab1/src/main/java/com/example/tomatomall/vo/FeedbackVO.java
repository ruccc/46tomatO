package com.example.tomatomall.vo;

import lombok.Data;

@Data
public class FeedbackVO {
    private Long id;
    private Integer userId;
    private String type; // suggestion/complaint/bug
    private String content;
    private String contact;
    private Integer status; // 0-未处理,1-已处理
    private String createTime;
}