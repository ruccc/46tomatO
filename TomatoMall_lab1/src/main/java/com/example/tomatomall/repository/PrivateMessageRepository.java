package com.example.tomatomall.repository;

import com.example.tomatomall.po.PrivateMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface PrivateMessageRepository extends JpaRepository<PrivateMessage, Long> {

    @Query("SELECT pm FROM PrivateMessage pm WHERE " +
            "(pm.senderId = :userId1 AND pm.receiverId = :userId2) OR " +
            "(pm.senderId = :userId2 AND pm.receiverId = :userId1) " +
            "ORDER BY pm.createTime DESC")
    Page<PrivateMessage> findConversation(Integer userId1, Integer userId2, Pageable pageable);

    @Query("SELECT DISTINCT pm.receiverId FROM PrivateMessage pm WHERE pm.senderId = :userId")
    List<Integer> findReceiversBySender(Integer userId);

    @Query("SELECT DISTINCT pm.senderId FROM PrivateMessage pm WHERE pm.receiverId = :userId")
    List<Integer> findSendersByReceiver(Integer userId);

    @Query("SELECT pm FROM PrivateMessage pm WHERE " +
            "(pm.senderId = :userId AND pm.receiverId = :contactId) OR " +
            "(pm.senderId = :contactId AND pm.receiverId = :userId) " +
            "ORDER BY pm.createTime DESC")
    List<PrivateMessage> findLatestMessages(Integer userId, Integer contactId, Pageable pageable);

    @Query("SELECT COUNT(pm) FROM PrivateMessage pm WHERE pm.receiverId = :userId AND pm.status = 1")
    Integer countUnreadMessages(Integer userId);

    @Query("SELECT COUNT(pm) FROM PrivateMessage pm WHERE pm.receiverId = :userId AND pm.senderId = :senderId AND pm.status = 1")
    Integer countUnreadMessagesFromUser(Integer userId, Integer senderId);

    @Transactional
    @Modifying
    @Query("UPDATE PrivateMessage pm SET pm.status = 2 WHERE pm.receiverId = :userId AND pm.senderId = :senderId AND pm.status = 1")
    void markMessagesAsRead(Integer userId, Integer senderId);
}