package com.example.tomatomall.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CheckoutRequestDTO {
    public List<String> cartItemIds;
    public Object shipping_address;
    public String payment_method;
    
    // 添加会员折扣相关字段
    public Integer memberLevel;              // 会员等级
    public BigDecimal originalAmount;        // 原价总额
    public BigDecimal discountAmount;        // 折扣金额
    public BigDecimal finalAmount;           // 实付金额（折扣后）
}
