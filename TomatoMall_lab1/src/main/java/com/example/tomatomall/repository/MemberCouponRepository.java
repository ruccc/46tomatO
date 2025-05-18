package com.example.tomatomall.repository;

import com.example.tomatomall.po.MemberCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemberCouponRepository extends JpaRepository<MemberCoupon, Integer> {

    List<MemberCoupon> findByMemberId(String memberId);

    List<MemberCoupon> findByMemberIdAndStatus(String memberId, Integer status);

    int countByMemberIdAndCouponId(String memberId, Integer couponId);

    MemberCoupon findByMemberIdAndCouponCode(String memberId, String couponCode);
}