package com.example.tomatomall.dto;

import lombok.Data;
import javax.validation.Valid;
import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class ProductUpdateDTO {
    @NotBlank(message = "商品ID不能为空")
    private String id;

    @NotBlank(message = "商品标题不能为空")
    private String title;

    @NotNull(message = "商品价格不能为空")
    @DecimalMin(value = "0.01", message = "商品价格必须大于0")
    private BigDecimal price;

    @DecimalMin(value = "0", message = "评分不能小于0")
    @DecimalMax(value = "10", message = "评分不能大于10")
    private Double rate;

    private String description;
    private String cover;
    private String detail;

    @Valid
    private List<ProductSpecificationDTO> specifications=new ArrayList<>();
}