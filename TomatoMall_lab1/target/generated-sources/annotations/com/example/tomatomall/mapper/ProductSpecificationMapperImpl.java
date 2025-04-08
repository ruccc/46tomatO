package com.example.tomatomall.mapper;

import com.example.tomatomall.po.Product;
import com.example.tomatomall.po.ProductSpecification;
import com.example.tomatomall.vo.ProductSpecificationVO;
import javax.annotation.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-04-07T23:59:19+0800",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 1.8.0_442 (Amazon.com Inc.)"
)
@Component
public class ProductSpecificationMapperImpl implements ProductSpecificationMapper {

    @Override
    public ProductSpecificationVO toVO(ProductSpecification spec) {
        if ( spec == null ) {
            return null;
        }

        ProductSpecificationVO productSpecificationVO = new ProductSpecificationVO();

        productSpecificationVO.setProductId( specProductId( spec ) );
        productSpecificationVO.setId( spec.getId() );
        productSpecificationVO.setItem( spec.getItem() );
        productSpecificationVO.setValue( spec.getValue() );

        return productSpecificationVO;
    }

    private String specProductId(ProductSpecification productSpecification) {
        if ( productSpecification == null ) {
            return null;
        }
        Product product = productSpecification.getProduct();
        if ( product == null ) {
            return null;
        }
        String id = product.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
