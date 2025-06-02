package com.example.tomatomall.vo;

import lombok.Data;

@Data
public class MemberCouponVO {
    private Integer id;
    private String memberId;
    private Integer couponId;
    private String couponCode;
    private String couponName;
    private Integer couponType;
    private Double amount;
    private Double minPoint;
    private String startTime;
    private String endTime;
    private String useTime;
    private Integer status;
    private String createTime;
    private String updateTime;

    // 优惠券状态描述
    private String statusDesc;

    // 优惠券类型描述
    private String typeDesc;
}