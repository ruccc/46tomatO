package com.example.tomatomall.repository;

import com.example.tomatomall.po.CustomerServiceSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerServiceSessionRepository extends JpaRepository<CustomerServiceSession, String> {

    @Query("SELECT css FROM CustomerServiceSession css WHERE css.customerId = :customerId ORDER BY css.updateTime DESC")
    Page<CustomerServiceSession> findByCustomerId(Integer customerId, Pageable pageable);

    @Query("SELECT css FROM CustomerServiceSession css WHERE css.serviceId = :serviceId AND css.status IN (0, 1) ORDER BY css.updateTime DESC")
    Page<CustomerServiceSession> findActiveSessionsByServiceId(Integer serviceId, Pageable pageable);

    Optional<CustomerServiceSession> findByCustomerIdAndStatusIn(Integer customerId, List<Integer> statuses);

    @Query("SELECT css FROM CustomerServiceSession css WHERE css.sessionId = :sessionId AND (css.customerId = :userId OR css.serviceId = :userId)")
    Optional<CustomerServiceSession> findByIdAndUserId(String sessionId, Integer userId);
}