package com.example.tomatomall.repository;

import com.example.tomatomall.po.Feedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    Page<Feedback> findByUserIdOrderByCreateTimeDesc(Integer userId, Pageable pageable);

    Page<Feedback> findByStatusOrderByCreateTimeDesc(Integer status, Pageable pageable);
}