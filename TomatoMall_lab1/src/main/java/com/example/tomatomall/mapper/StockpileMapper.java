package com.example.tomatomall.mapper;

import com.example.tomatomall.po.Stockpile;
import com.example.tomatomall.vo.StockpileVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StockpileMapper {

    @Mapping(target = "productId", source = "product.id")
    StockpileVO toVO(Stockpile stockpile);
}