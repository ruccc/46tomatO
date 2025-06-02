package com.example.tomatomall.repository;

import com.example.tomatomall.po.CustomerServiceMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerServiceMessageRepository extends JpaRepository<CustomerServiceMessage, Long> {

    Page<CustomerServiceMessage> findBySessionIdOrderByCreateTimeDesc(String sessionId, Pageable pageable);

    List<CustomerServiceMessage> findBySessionId(String sessionId);
}