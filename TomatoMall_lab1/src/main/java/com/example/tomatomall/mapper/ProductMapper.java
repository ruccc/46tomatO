package com.example.tomatomall.mapper;

import com.example.tomatomall.po.*;
import com.example.tomatomall.vo.*;
import org.mapstruct.*;

import java.util.stream.Collectors;

@Mapper(componentModel = "spring",
        uses = {ProductSpecificationMapper.class, StockpileMapper.class},
        imports = {Collectors.class})
public interface ProductMapper {

    @Mapping(target = "specifications", source = "specifications")
    ProductVO toVO(Product product);

    @Mapping(target = "specifications", source = "specifications")
    @Mapping(target = "stockpile", source = "stockpile")
    ProductDetailVO toDetailVO(Product product);
}