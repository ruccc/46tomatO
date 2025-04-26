package com.example.tomatomall.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CartResponseDTO {
    private String cartItemId;
    private String productId;
    private String title;
    private BigDecimal price;
    private String description;
    private String cover;
    private String detail;
    private int quantity;
    private boolean isExistingItem;
}