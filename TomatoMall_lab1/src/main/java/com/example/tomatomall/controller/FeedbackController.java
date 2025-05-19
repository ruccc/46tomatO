package com.example.tomatomall.controller;

import com.example.tomatomall.service.FeedbackService;
import com.example.tomatomall.util.Result;
import com.example.tomatomall.vo.FeedbackVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping
    public Result<FeedbackVO> submitFeedback(
            @RequestAttribute Integer userId,
            @RequestParam String type,
            @RequestParam String content,
            @RequestParam(required = false) String contact) {
        try {
            FeedbackVO feedback = feedbackService.submitFeedback(userId, type, content, contact);
            return Result.success(feedback);
        } catch (Exception e) {
            log.error("提交反馈失败", e);
            return Result.fail(500, "提交反馈失败");
        }
    }

    @GetMapping("/user")
    public Result<Page<FeedbackVO>> getUserFeedbacks(
            @RequestAttribute Integer userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Page<FeedbackVO> feedbacks = feedbackService.getUserFeedbacks(userId, page, size);
            return Result.success(feedbacks);
        } catch (Exception e) {
            log.error("获取用户反馈列表失败", e);
            return Result.fail(500, "获取用户反馈列表失败");
        }
    }

    @GetMapping
    public Result<Page<FeedbackVO>> getAllFeedbacks(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Page<FeedbackVO> feedbacks = feedbackService.getAllFeedbacks(status, page, size);
            return Result.success(feedbacks);
        } catch (Exception e) {
            log.error("获取所有反馈列表失败", e);
            return Result.fail(500, "获取所有反馈列表失败");
        }
    }

    @PostMapping("/{feedbackId}/process")
    public Result<FeedbackVO> processFeedback(@PathVariable Long feedbackId) {
        try {
            FeedbackVO feedback = feedbackService.processFeedback(feedbackId);
            return Result.success(feedback);
        } catch (Exception e) {
            log.error("处理反馈失败", e);
            return Result.fail(500, "处理反馈失败");
        }
    }
}