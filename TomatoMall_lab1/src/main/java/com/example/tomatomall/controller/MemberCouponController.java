package com.example.tomatomall.controller;

import com.example.tomatomall.TomatoException.BusinessException;
import com.example.tomatomall.service.MemberCouponService;
import com.example.tomatomall.util.Result;
import com.example.tomatomall.vo.MemberCouponVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/member-coupons")
@RequiredArgsConstructor
public class MemberCouponController {
    private final MemberCouponService memberCouponService;

    @GetMapping("/{memberId}")
    public Result<List<MemberCouponVO>> getMemberCoupons(@PathVariable String memberId) {
        try {
            List<MemberCouponVO> coupons = memberCouponService.getMemberCoupons(memberId);
            return Result.success(coupons);
        } catch (Exception e) {
            log.error("获取会员优惠券失败", e);
            return Result.fail(500, "服务器内部错误");
        }
    }

    @GetMapping("/{memberId}/available")
    public Result<List<MemberCouponVO>> getAvailableCoupons(@PathVariable String memberId) {
        try {
            List<MemberCouponVO> coupons = memberCouponService.getAvailableCoupons(memberId);
            return Result.success(coupons);
        } catch (Exception e) {
            log.error("获取可用优惠券失败", e);
            return Result.fail(500, "服务器内部错误");
        }
    }

    @PostMapping("/{memberId}/add")
    public Result<MemberCouponVO> addCouponToMember(
            @PathVariable String memberId,
            @RequestParam Integer couponId) {
        try {
            MemberCouponVO coupon = memberCouponService.addCouponToMember(memberId, couponId);
            return Result.success(coupon);
        } catch (BusinessException e) {
            return Result.fail(400, e.getMessage());
        } catch (Exception e) {
            log.error("添加优惠券失败", e);
            return Result.fail(500, "服务器内部错误");
        }
    }

    @PostMapping("/{memberId}/use")
    public Result<String> useCoupon(
            @PathVariable String memberId,
            @RequestParam String couponCode) {
        try {
            memberCouponService.useCoupon(memberId, couponCode);
            return Result.success("使用优惠券成功");
        } catch (BusinessException e) {
            return Result.fail(400, e.getMessage());
        } catch (Exception e) {
            log.error("使用优惠券失败", e);
            return Result.fail(500, "服务器内部错误");
        }
    }

    @PostMapping("/{memberId}/return")
    public Result<String> returnCoupon(
            @PathVariable String memberId,
            @RequestParam String couponCode) {
        try {
            memberCouponService.returnCoupon(memberId, couponCode);
            return Result.success("退还优惠券成功");
        } catch (BusinessException e) {
            return Result.fail(400, e.getMessage());
        } catch (Exception e) {
            log.error("退还优惠券失败", e);
            return Result.fail(500, "服务器内部错误");
        }
    }
}