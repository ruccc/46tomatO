package com.example.tomatomall.mapper;

import com.example.tomatomall.po.ProductSpecification;
import com.example.tomatomall.vo.ProductSpecificationVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductSpecificationMapper {
    @Mapping(target = "productId", source = "product.id")
    ProductSpecificationVO toVO(ProductSpecification spec);
}