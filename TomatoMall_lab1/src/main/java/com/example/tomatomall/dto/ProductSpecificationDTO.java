package com.example.tomatomall.dto;

import lombok.Data;
import javax.validation.constraints.*;

@Data
public class ProductSpecificationDTO {
    @NotBlank(message = "规格项不能为空")
    @Size(max = 50, message = "规格项长度不能超过50个字符")
    private String item;

    @NotBlank(message = "规格值不能为空")
    @Size(max = 100, message = "规格值长度不能超过100个字符")
    private String value;
}