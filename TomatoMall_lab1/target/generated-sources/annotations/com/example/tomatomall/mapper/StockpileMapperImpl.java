package com.example.tomatomall.mapper;

import com.example.tomatomall.po.Product;
import com.example.tomatomall.po.Stockpile;
import com.example.tomatomall.vo.StockpileVO;
import javax.annotation.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-04-07T23:59:19+0800",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 1.8.0_442 (Amazon.com Inc.)"
)
@Component
public class StockpileMapperImpl implements StockpileMapper {

    @Override
    public StockpileVO toVO(Stockpile stockpile) {
        if ( stockpile == null ) {
            return null;
        }

        StockpileVO stockpileVO = new StockpileVO();

        stockpileVO.setProductId( stockpileProductId( stockpile ) );
        stockpileVO.setId( stockpile.getId() );
        stockpileVO.setAmount( stockpile.getAmount() );
        stockpileVO.setFrozen( stockpile.getFrozen() );

        return stockpileVO;
    }

    private String stockpileProductId(Stockpile stockpile) {
        if ( stockpile == null ) {
            return null;
        }
        Product product = stockpile.getProduct();
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
