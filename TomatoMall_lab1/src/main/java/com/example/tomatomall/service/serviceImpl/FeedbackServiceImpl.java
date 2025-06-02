package com.example.tomatomall.service.serviceImpl;

import com.example.tomatomall.po.Feedback;
import com.example.tomatomall.repository.FeedbackRepository;
import com.example.tomatomall.service.FeedbackService;
import com.example.tomatomall.vo.FeedbackVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackRepository feedbackRepository;

    @Override
    @Transactional
    public FeedbackVO submitFeedback(Integer userId, String type, String content, String contact) {
        Feedback feedback = new Feedback();
        feedback.setUserId(userId);
        feedback.setType(type);
        feedback.setContent(content);
        feedback.setContact(contact);
        feedback.setStatus(0); // 未处理
        feedback.setCreateTime(LocalDateTime.now().toString());

        Feedback savedFeedback = feedbackRepository.save(feedback);
        return convertToVO(savedFeedback);
    }

    @Override
    public Page<FeedbackVO> getUserFeedbacks(Integer userId, int page, int size) {
        Page<Feedback> feedbacks = feedbackRepository.findByUserIdOrderByCreateTimeDesc(
                userId, PageRequest.of(page, size));
        return feedbacks.map(this::convertToVO);
    }

    @Override
    public Page<FeedbackVO> getAllFeedbacks(Integer status, int page, int size) {
        Page<Feedback> feedbacks;
        if (status != null) {
            feedbacks = feedbackRepository.findByStatusOrderByCreateTimeDesc(
                    status, PageRequest.of(page, size));
        } else {
            feedbacks = feedbackRepository.findAll(PageRequest.of(page, size));
        }
        return feedbacks.map(this::convertToVO);
    }

    @Override
    @Transactional
    public FeedbackVO processFeedback(Long feedbackId) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new RuntimeException("反馈不存在"));

        feedback.setStatus(1); // 已处理
        feedbackRepository.save(feedback);

        return convertToVO(feedback);
    }

    private FeedbackVO convertToVO(Feedback feedback) {
        FeedbackVO vo = new FeedbackVO();
        vo.setId(feedback.getId());
        vo.setUserId(feedback.getUserId());
        vo.setType(feedback.getType());
        vo.setContent(feedback.getContent());
        vo.setContact(feedback.getContact());
        vo.setStatus(feedback.getStatus());
        vo.setCreateTime(feedback.getCreateTime());
        return vo;
    }
}