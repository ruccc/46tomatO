package com.example.tomatomall.vo;

import lombok.Data;

@Data
public class MemberLevelVO {
    private Integer id;
    private String levelName;
    private Integer growthPoint;
    private Double discount;
    private String description;
}