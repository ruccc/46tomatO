package com.example.tomatomall.mapper;

import com.example.tomatomall.po.Stockpile;
import com.example.tomatomall.vo.StockpileVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface StockpileMapper {

    @Mapping(target = "productId", source = "product.id")
    StockpileVO toVO(Stockpile stockpile);

    @Named("mapStockpile")
    default StockpileVO mapStockpile(Stockpile stockpile) {
        if (stockpile == null) {
            return null;
        }
        return toVO(stockpile);
    }
}