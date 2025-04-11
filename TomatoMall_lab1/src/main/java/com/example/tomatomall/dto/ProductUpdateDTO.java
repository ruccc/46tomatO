package com.example.tomatomall.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Set;

@Data
public class ProductUpdateDTO {
    @NotBlank(message = "商品ID不能为空")
    private String id;

    @NotBlank(message = "商品标题不能为空")
    private String title;

    @NotNull(message = "商品价格不能为空")
    private BigDecimal price;

    private Double rate;
    private String description;
    private String cover;
    private String detail;

    private Set<ProductSpecificationDTO> specifications;
}