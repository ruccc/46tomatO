package com.example.tomatomall.service;

import com.example.tomatomall.vo.MemberCouponVO;

import java.util.List;

public interface MemberCouponService {
    List<MemberCouponVO> getMemberCoupons(String memberId);

    List<MemberCouponVO> getAvailableCoupons(String memberId);

    MemberCouponVO addCouponToMember(String memberId, Integer couponId);

    void useCoupon(String memberId, String couponCode);

    void returnCoupon(String memberId, String couponCode);
}