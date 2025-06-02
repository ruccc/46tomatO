package com.example.tomatomall.service;

import com.example.tomatomall.vo.CommentVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentService {
    CommentVO addComment(String bookId, Integer userId, String content, Integer rating);
    Page<CommentVO> getCommentsByBookId(String bookId, Pageable pageable);
    Double getAverageRating(String bookId);
    Long getCommentCount(String bookId);
    boolean hasUserCommented(String bookId, Integer userId);
}