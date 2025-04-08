package com.example.tomatomall.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductDetailVO {
    private String id;
    private String title;
    private BigDecimal price;
    private double rate;
    private String description;
    private String cover;
    private String detail;
    private List<ProductSpecificationVO> specifications;
    private StockpileVO stockpile;
}