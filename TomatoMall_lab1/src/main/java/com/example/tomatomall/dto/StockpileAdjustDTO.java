package com.example.tomatomall.dto;

import lombok.Data;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class StockpileAdjustDTO {
    @NotNull(message = "调整数量不能为空")
    private Integer amount;

    @Min(value = 0, message = "冻结数量不能小于0")
    private Integer frozen = 0;
}