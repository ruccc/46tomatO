package com.example.tomatomall.mapper;

import com.example.tomatomall.po.*;
import com.example.tomatomall.vo.*;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        uses = {ProductSpecificationMapper.class, StockpileMapper.class})
public interface ProductMapper {

    @Mapping(target = "specifications", source = "specifications")
    @Mapping(target = "stockpile", source = "stockpile")
    ProductDetailVO toDetailVO(Product product);

    @Mapping(target = "specifications", source = "specifications")
    ProductVO toVO(Product product);
}
