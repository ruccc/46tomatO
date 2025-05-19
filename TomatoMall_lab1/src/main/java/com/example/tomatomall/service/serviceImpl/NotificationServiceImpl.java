package com.example.tomatomall.service.serviceImpl;

import com.example.tomatomall.po.Notification;
import com.example.tomatomall.repository.NotificationRepository;
import com.example.tomatomall.service.NotificationService;
import com.example.tomatomall.vo.NotificationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    @Transactional
    public void sendNotification(Integer userId, String type, String title, String content, String relatedId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setRelatedId(relatedId);
        notification.setIsRead(false);
        notification.setCreateTime(LocalDateTime.now().toString());

        notificationRepository.save(notification);
    }

    @Override
    public Page<NotificationVO> getNotifications(Integer userId, String type, Boolean isRead, int page, int size) {
        Page<Notification> notifications;
        if (type != null && isRead != null) {
            notifications = notificationRepository.findByUserIdAndTypeAndIsReadOrderByCreateTimeDesc(
                    userId, type, isRead, PageRequest.of(page, size));
        } else if (type != null) {
            notifications = notificationRepository.findByUserIdAndTypeOrderByCreateTimeDesc(
                    userId, type, PageRequest.of(page, size));
        } else if (isRead != null) {
            notifications = notificationRepository.findByUserIdAndIsReadOrderByCreateTimeDesc(
                    userId, isRead, PageRequest.of(page, size));
        } else {
            notifications = notificationRepository.findByUserIdOrderByCreateTimeDesc(
                    userId, PageRequest.of(page, size));
        }

        return notifications.map(this::convertToVO);
    }

    @Override
    @Transactional
    public void markNotificationsAsRead(List<Long> notificationIds) {
        notificationRepository.markAsRead(notificationIds);
    }

    @Override
    public Map<String, Integer> getUnreadNotificationCounts(Integer userId) {
        Map<String, Integer> counts = new HashMap<>();
        counts.put("total", notificationRepository.countUnreadNotifications(userId));
        counts.put("order", notificationRepository.countUnreadNotificationsByType(userId, "order"));
        counts.put("system", notificationRepository.countUnreadNotificationsByType(userId, "system"));
        counts.put("promotion", notificationRepository.countUnreadNotificationsByType(userId, "promotion"));
        return counts;
    }

    private NotificationVO convertToVO(Notification notification) {
        NotificationVO vo = new NotificationVO();
        vo.setId(notification.getId());
        vo.setUserId(notification.getUserId());
        vo.setType(notification.getType());
        vo.setTitle(notification.getTitle());
        vo.setContent(notification.getContent());
        vo.setRelatedId(notification.getRelatedId());
        vo.setIsRead(notification.getIsRead());
        vo.setCreateTime(notification.getCreateTime());
        return vo;
    }
}