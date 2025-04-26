package com.example.tomatomall.vo;

import com.example.tomatomall.po.Order;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Getter
@Setter
@NoArgsConstructor
public class OrderVO {
    private Integer orderId;

    private Integer userId;


    private BigDecimal totalAmount;


    private String paymentMethod;


    private String status;


    private LocalDateTime createTime;

public Order toPO(){
    Order order= new Order();
    order.setOrderId(orderId);
    order.setUserId(userId);
    order.setTotalAmount(totalAmount);
    order.setPaymentMethod(paymentMethod);
    order.setStatus(status);
    order.setCreateTime(createTime);
    return order;
}
}
