package com.example.tomatomall.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class MemberVO {
    private String id;
    private Integer accountId;
    private String memberNo;
    private String name;
    private String email;
    private String phone;
    private String address;
    private Integer points;
    private Integer level;
    private Integer growthValue;
    private String birthday;
    private String gender;
    private Integer status;
    private String registerTime;
    private String lastLoginTime;

    // 会员等级信息
    private MemberLevelVO levelInfo;

    // 会员优惠券列表
    private List<MemberCouponVO> coupons;

    // 会员积分记录
    private List<PointRecordVO> pointRecords;
}