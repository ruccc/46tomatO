package com.example.tomatomall.vo;

import com.example.tomatomall.po.ProductSpecification;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class ProductVO {
    private String id;
    private String title;
    private BigDecimal price;
    private double rate;
    private String description;
    private String cover;
    private String detail;
    private Set<ProductSpecification> specifications;
}