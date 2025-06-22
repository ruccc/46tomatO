package com.example.tomatomall.vo;

import com.example.tomatomall.po.Product;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductDetailVO {
    private String id;
    private String title;
    private BigDecimal price;
    private double rate;
    private String description;
    private String cover;
    private String detail;
    private List<ProductSpecificationVO> specifications;
    private StockpileVO stockpile;

    public Product toPO() {
        Product product = new Product();
        product.setId(id);
        product.setTitle(title);
        product.setPrice(price);
        product.setRate(rate);
        product.setDescription(description);
        product.setCover(cover);
        product.setDetail(detail);
        // 注意：specifications 和 stockpile 需要额外处理
        return product;
    }
}