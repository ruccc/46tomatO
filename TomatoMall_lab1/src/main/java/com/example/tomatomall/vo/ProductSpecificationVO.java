package com.example.tomatomall.vo;

import com.example.tomatomall.po.Product;
import com.example.tomatomall.po.ProductSpecification;
import lombok.Data;

@Data
public class ProductSpecificationVO {
    private String id;
    private String item;
    private String value;
    private Product product;

    public ProductSpecification toPO(){
        ProductSpecification productSpecification = new ProductSpecification();
        productSpecification.setId(id);
        productSpecification.setItem(item);
        productSpecification.setValue(value);
        productSpecification.setProduct(product);
        return productSpecification;
    }
}