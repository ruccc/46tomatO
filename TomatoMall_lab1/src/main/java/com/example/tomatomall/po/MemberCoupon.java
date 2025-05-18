package com.example.tomatomall.po;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "member_coupon")
public class MemberCoupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "member_id", nullable = false)
    private String memberId;

    @Column(name = "coupon_id", nullable = false)
    private Integer couponId;

    @Column(name = "coupon_code", nullable = false)
    private String couponCode;

    @Column(name = "coupon_name", nullable = false)
    private String couponName;

    @Column(name = "coupon_type", nullable = false)
    private Integer couponType; // 1-满减券 2-折扣券 3-无门槛券

    @Column(name = "amount", nullable = false)
    private Double amount;

    @Column(name = "min_point")
    private Double minPoint; // 使用门槛

    @Column(name = "start_time", nullable = false)
    private String startTime;

    @Column(name = "end_time", nullable = false)
    private String endTime;

    @Column(name = "use_time")
    private String useTime;

    @Column(name = "status", nullable = false)
    private Integer status; // 0-未使用 1-已使用 2-已过期

    @Column(name = "create_time", nullable = false)
    private String createTime;

    @Column(name = "update_time")
    private String updateTime;
}