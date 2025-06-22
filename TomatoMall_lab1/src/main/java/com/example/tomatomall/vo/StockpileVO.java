package com.example.tomatomall.vo;

import com.example.tomatomall.po.Stockpile;
import lombok.Data;

@Data
public class StockpileVO {
    private String id;
    private Integer amount;
    private Integer frozen;
    private String productId;

    public Stockpile toPO() {
        Stockpile stockpile = new Stockpile();
        stockpile.setId(id);
        stockpile.setAmount(amount);
        stockpile.setFrozen(frozen);
        // 注意：product 需要额外设置，因为 StockpileVO 只存储了 productId
        return stockpile;
    }
}