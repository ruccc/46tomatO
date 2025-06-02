package com.example.tomatomall.service.serviceImpl;

import com.example.tomatomall.TomatoException.BusinessException;
import com.example.tomatomall.po.MemberCoupon;
import com.example.tomatomall.repository.MemberCouponRepository;
import com.example.tomatomall.service.MemberCouponService;
import com.example.tomatomall.vo.MemberCouponVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class MemberCouponServiceImpl implements MemberCouponService {

    private final MemberCouponRepository memberCouponRepository;

    public MemberCouponServiceImpl(MemberCouponRepository memberCouponRepository) {
        this.memberCouponRepository = memberCouponRepository;
    }

    @Override
    public List<MemberCouponVO> getMemberCoupons(String memberId) {
        return memberCouponRepository.findByMemberId(memberId).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<MemberCouponVO> getAvailableCoupons(String memberId) {
        return memberCouponRepository.findByMemberIdAndStatus(memberId, 0).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public MemberCouponVO addCouponToMember(String memberId, Integer couponId) {
        // 检查是否已经领取过该优惠券
        if (memberCouponRepository.countByMemberIdAndCouponId(memberId, couponId) > 0) {
            throw new BusinessException("您已经领取过该优惠券");
        }

        // 实际项目中这里应该从优惠券服务获取优惠券详情
        MemberCoupon coupon = new MemberCoupon();
        coupon.setMemberId(memberId);
        coupon.setCouponId(couponId);
        coupon.setCouponCode(generateCouponCode());
        coupon.setCouponName("优惠券名称"); // 应从优惠券服务获取
        coupon.setCouponType(1); // 应从优惠券服务获取
        coupon.setAmount(10.0); // 应从优惠券服务获取
        coupon.setMinPoint(100.0); // 应从优惠券服务获取
        coupon.setStartTime(LocalDateTime.now().toString());
        coupon.setEndTime(LocalDateTime.now().plusDays(30).toString());
        coupon.setStatus(0);
        coupon.setCreateTime(LocalDateTime.now().toString());

        MemberCoupon savedCoupon = memberCouponRepository.save(coupon);
        return convertToVO(savedCoupon);
    }

    @Override
    public void useCoupon(String memberId, String couponCode) {
        MemberCoupon coupon = memberCouponRepository.findByMemberIdAndCouponCode(memberId, couponCode);
        if (coupon == null) {
            throw new BusinessException("优惠券不存在");
        }
        if (coupon.getStatus() != 0) {
            throw new BusinessException("优惠券不可用");
        }

        coupon.setStatus(1);
        coupon.setUseTime(LocalDateTime.now().toString());
        coupon.setUpdateTime(LocalDateTime.now().toString());
        memberCouponRepository.save(coupon);
    }

    @Override
    public void returnCoupon(String memberId, String couponCode) {
        MemberCoupon coupon = memberCouponRepository.findByMemberIdAndCouponCode(memberId, couponCode);
        if (coupon == null) {
            throw new BusinessException("优惠券不存在");
        }
        if (coupon.getStatus() != 1) {
            throw new BusinessException("优惠券状态不正确");
        }

        coupon.setStatus(0);
        coupon.setUseTime(null);
        coupon.setUpdateTime(LocalDateTime.now().toString());
        memberCouponRepository.save(coupon);
    }

    private MemberCouponVO convertToVO(MemberCoupon coupon) {
        MemberCouponVO vo = new MemberCouponVO();
        vo.setId(coupon.getId());
        vo.setMemberId(coupon.getMemberId());
        vo.setCouponId(coupon.getCouponId());
        vo.setCouponCode(coupon.getCouponCode());
        vo.setCouponName(coupon.getCouponName());
        vo.setCouponType(coupon.getCouponType());
        vo.setAmount(coupon.getAmount());
        vo.setMinPoint(coupon.getMinPoint());
        vo.setStartTime(coupon.getStartTime());
        vo.setEndTime(coupon.getEndTime());
        vo.setUseTime(coupon.getUseTime());
        vo.setStatus(coupon.getStatus());
        vo.setCreateTime(coupon.getCreateTime());
        vo.setUpdateTime(coupon.getUpdateTime());

        // 设置状态描述
        switch (coupon.getStatus()) {
            case 0: vo.setStatusDesc("未使用"); break;
            case 1: vo.setStatusDesc("已使用"); break;
            case 2: vo.setStatusDesc("已过期"); break;
            default: vo.setStatusDesc("未知状态");
        }

        // 设置类型描述
        switch (coupon.getCouponType()) {
            case 1: vo.setTypeDesc("满减券"); break;
            case 2: vo.setTypeDesc("折扣券"); break;
            case 3: vo.setTypeDesc("无门槛券"); break;
            default: vo.setTypeDesc("未知类型");
        }

        return vo;
    }

    private String generateCouponCode() {
        return "CP" + System.currentTimeMillis() + (int)(Math.random() * 1000);
    }
}