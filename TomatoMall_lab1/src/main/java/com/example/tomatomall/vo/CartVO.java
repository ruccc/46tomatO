package com.example.tomatomall.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class CartVO {
    private String cartItemId;
    private Integer userId;
    private String productId;
    private String title;
    private BigDecimal price;
    private String description;
    private String cover;
    private String detail;
    private Integer quantity;
}