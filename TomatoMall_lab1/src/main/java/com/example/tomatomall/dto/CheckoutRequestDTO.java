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
}
