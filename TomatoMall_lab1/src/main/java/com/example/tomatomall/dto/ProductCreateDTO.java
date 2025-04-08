package com.example.tomatomall.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class ProductCreateDTO {
    @NotBlank(message = "商品标题不能为空")
    private String title;

    @NotNull(message = "商品价格不能为空")
    @DecimalMin(value = "0.01", message = "商品价格必须大于0")
    private BigDecimal price;

    @DecimalMin(value = "0", message = "评分不能小于0")
    @DecimalMax(value = "10", message = "评分不能大于10")
    private Double rate = 0.0;

    private String description;
    private String cover;
    private String detail;
    
    @Min(value = 0, message = "库存数量不能小于0")
    private Integer stockAmount=0;

    @Valid
    private List<ProductSpecificationDTO> specifications=new ArrayList<>();

    @Data
    @NoArgsConstructor
    public static class ProductSpecificationDTO {
        @NotBlank private String item;
        @NotBlank private String value;
    }
}