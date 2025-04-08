package com.example.tomatomall.dto;

import lombok.Data;
import javax.validation.constraints.Min;

@Data
public class StockpileAdjustDTO {
    @Min(value = 0, message = "调整数量不能小于0")
    private int amount;

    @Min(value = 0, message = "冻结数量不能小于0")
    private int frozen;
}