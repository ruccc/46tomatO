package com.example.tomatomall.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
@Getter
@Setter
public class PaymentResponseDTO {
    String paymentForm;
    int orderId;
    BigDecimal totalAmount;
    String paymentMethod;
}
