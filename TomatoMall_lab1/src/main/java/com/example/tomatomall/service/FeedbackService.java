package com.example.tomatomall.service;

import com.example.tomatomall.vo.FeedbackVO;
import org.springframework.data.domain.Page;

public interface FeedbackService {
    FeedbackVO submitFeedback(Integer userId, String type, String content, String contact);

    Page<FeedbackVO> getUserFeedbacks(Integer userId, int page, int size);

    Page<FeedbackVO> getAllFeedbacks(Integer status, int page, int size);

    FeedbackVO processFeedback(Long feedbackId);
}