package com.example.tomatomall.mapper;

import com.example.tomatomall.po.Product;
import com.example.tomatomall.po.ProductSpecification;
import com.example.tomatomall.vo.ProductDetailVO;
import com.example.tomatomall.vo.ProductSpecificationVO;
import com.example.tomatomall.vo.ProductVO;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-04-08T00:02:05+0800",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 1.8.0_442 (Amazon.com Inc.)"
)
@Component
public class ProductMapperImpl implements ProductMapper {

    @Autowired
    private ProductSpecificationMapper productSpecificationMapper;
    @Autowired
    private StockpileMapper stockpileMapper;

    @Override
    public ProductVO toVO(Product product) {
        if ( product == null ) {
            return null;
        }

        ProductVO productVO = new ProductVO();

        List<ProductSpecification> list = product.getSpecifications();
        if ( list != null ) {
            productVO.setSpecifications( new ArrayList<ProductSpecification>( list ) );
        }
        productVO.setId( product.getId() );
        productVO.setTitle( product.getTitle() );
        productVO.setPrice( product.getPrice() );
        if ( product.getRate() != null ) {
            productVO.setRate( product.getRate() );
        }
        productVO.setDescription( product.getDescription() );
        productVO.setCover( product.getCover() );
        productVO.setDetail( product.getDetail() );

        return productVO;
    }

    @Override
    public ProductDetailVO toDetailVO(Product product) {
        if ( product == null ) {
            return null;
        }

        ProductDetailVO productDetailVO = new ProductDetailVO();

        productDetailVO.setSpecifications( productSpecificationListToProductSpecificationVOList( product.getSpecifications() ) );
        productDetailVO.setStockpile( stockpileMapper.toVO( product.getStockpile() ) );
        productDetailVO.setId( product.getId() );
        productDetailVO.setTitle( product.getTitle() );
        productDetailVO.setPrice( product.getPrice() );
        if ( product.getRate() != null ) {
            productDetailVO.setRate( product.getRate() );
        }
        productDetailVO.setDescription( product.getDescription() );
        productDetailVO.setCover( product.getCover() );
        productDetailVO.setDetail( product.getDetail() );

        return productDetailVO;
    }

    protected List<ProductSpecificationVO> productSpecificationListToProductSpecificationVOList(List<ProductSpecification> list) {
        if ( list == null ) {
            return null;
        }

        List<ProductSpecificationVO> list1 = new ArrayList<ProductSpecificationVO>( list.size() );
        for ( ProductSpecification productSpecification : list ) {
            list1.add( productSpecificationMapper.toVO( productSpecification ) );
        }

        return list1;
    }
}
