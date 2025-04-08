package com.example.tomatomall.vo;

import lombok.Data;

@Data
public class StockpileVO {
    private String id;
    private Integer amount;
    private Integer frozen;
    private String productId;
}