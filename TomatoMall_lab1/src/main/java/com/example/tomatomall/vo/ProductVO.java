package com.example.tomatomall.vo;

import com.example.tomatomall.po.Product;
import com.example.tomatomall.po.ProductSpecification;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ProductVO {
    private String id;
    private String title;
    private BigDecimal price;
    private double rate;
    private String description;
    private String cover;
    private String detail;
    private List<ProductSpecification> specifications;

    public Product toPO() {
        Product product = new Product();
        product.setId(id);
        product.setTitle(title);
        product.setPrice(price);
        product.setRate(rate);
        product.setDescription(description);
        product.setCover(cover);
        product.setDetail(detail);
        product.setSpecifications(specifications);
        return product;
    }
}