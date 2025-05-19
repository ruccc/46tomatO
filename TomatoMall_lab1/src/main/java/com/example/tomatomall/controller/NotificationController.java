package com.example.tomatomall.controller;

import com.example.tomatomall.service.NotificationService;
import com.example.tomatomall.util.Result;
import com.example.tomatomall.vo.NotificationVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public Result<Page<NotificationVO>> getNotifications(
            @RequestAttribute Integer userId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean isRead,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Page<NotificationVO> notifications = notificationService.getNotifications(userId, type, isRead, page, size);
            return Result.success(notifications);
        } catch (Exception e) {
            log.error("获取通知列表失败", e);
            return Result.fail(500, "获取通知列表失败");
        }
    }

    @PostMapping("/read")
    public Result<Void> markNotificationsAsRead(
            @RequestAttribute Integer userId,
            @RequestBody List<Long> notificationIds) {
        try {
            notificationService.markNotificationsAsRead(notificationIds);
            return Result.success();
        } catch (Exception e) {
            log.error("标记通知为已读失败", e);
            return Result.fail(500, "标记通知为已读失败");
        }
    }

    @GetMapping("/unread-count")
    public Result<Map<String, Integer>> getUnreadNotificationCounts(@RequestAttribute Integer userId) {
        try {
            Map<String, Integer> counts = notificationService.getUnreadNotificationCounts(userId);
            return Result.success(counts);
        } catch (Exception e) {
            log.error("获取未读通知数量失败", e);
            return Result.fail(500, "获取未读通知数量失败");
        }
    }
}