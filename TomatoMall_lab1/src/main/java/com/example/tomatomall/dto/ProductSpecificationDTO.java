package com.example.tomatomall.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ProductSpecificationDTO {

    @NotBlank
    private String item;

    @NotBlank
    private String value;

    public ProductSpecificationDTO() {
    }
}