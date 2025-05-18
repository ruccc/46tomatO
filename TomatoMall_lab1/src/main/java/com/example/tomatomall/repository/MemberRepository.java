package com.example.tomatomall.repository;

import com.example.tomatomall.po.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, String> {
    Optional<Member> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<Member> findByAccountId(String id);
    boolean existsByAccountId(String id);
}