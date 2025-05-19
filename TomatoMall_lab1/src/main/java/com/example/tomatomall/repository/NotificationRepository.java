package com.example.tomatomall.repository;

import com.example.tomatomall.po.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUserIdAndTypeOrderByCreateTimeDesc(Integer userId, String type, Pageable pageable);

    Page<Notification> findByUserIdAndIsReadOrderByCreateTimeDesc(Integer userId, Boolean isRead, Pageable pageable);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.isRead = false")
    Integer countUnreadNotifications(Integer userId);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.isRead = false AND n.type = :type")
    Integer countUnreadNotificationsByType(Integer userId, String type);

    @Transactional
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.id IN :ids")
    void markAsRead(List<Long> ids);

    Page<Notification> findByUserIdOrderByCreateTimeDesc(Integer userId, Pageable pageable);

    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND (n.type = :type OR :type IS NULL) " +
            "AND (n.isRead = :isRead OR :isRead IS NULL) ORDER BY n.createTime DESC")
    Page<Notification> findByUserIdAndTypeAndIsRead(Integer userId, String type, Boolean isRead, Pageable pageable);

    Page<Notification> findByUserIdAndTypeAndIsReadOrderByCreateTimeDesc(Integer userId, String type, Boolean isRead, PageRequest of);
}