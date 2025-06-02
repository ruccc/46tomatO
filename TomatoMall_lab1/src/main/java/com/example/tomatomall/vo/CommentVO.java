package com.example.tomatomall.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CommentVO {
    private Long id;
    private String bookId;
    private String bookTitle;
    private String bookCover;
    private Integer userId;
    private String userName;
    private String userAvatar;
    private String content;
    private Integer rating;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}