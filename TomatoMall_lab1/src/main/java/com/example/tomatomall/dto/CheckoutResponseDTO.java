package com.example.tomatomall.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Getter
@Setter
public class CheckoutResponseDTO {
    int orderId;
    String username;
    BigDecimal totalAmount;
    String paymentMethod;
    LocalDateTime createTime;
    String status;

}
