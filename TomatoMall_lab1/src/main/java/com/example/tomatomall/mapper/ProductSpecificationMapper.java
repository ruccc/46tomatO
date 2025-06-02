package com.example.tomatomall.mapper;

import com.example.tomatomall.po.ProductSpecification;
import com.example.tomatomall.vo.ProductSpecificationVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface ProductSpecificationMapper {

    @Mapping(target = "product", source = "product")
    ProductSpecificationVO toVO(ProductSpecification spec);

    Set<ProductSpecificationVO> toVOSet(Set<ProductSpecification> specifications);

    @Named("mapSpecifications")
    default Set<ProductSpecificationVO> mapSpecifications(Set<ProductSpecification> specs) {
        if (specs == null) {
            return new HashSet<>();
        }
        return toVOSet(specs);
    }
}