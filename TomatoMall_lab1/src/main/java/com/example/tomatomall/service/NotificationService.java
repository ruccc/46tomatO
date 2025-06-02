package com.example.tomatomall.service;

import com.example.tomatomall.vo.NotificationVO;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface NotificationService {
    void sendNotification(Integer userId, String type, String title, String content, String relatedId);

    Page<NotificationVO> getNotifications(Integer userId, String type, Boolean isRead, int page, int size);

    void markNotificationsAsRead(List<Long> notificationIds);

    Map<String, Integer> getUnreadNotificationCounts(Integer userId);
}